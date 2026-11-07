package project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.dto.BookingCreateRequest;
import project.dto.BookingResponseDto;
import project.enums.BookingStatus;
import project.enums.Role;
import project.exception.ValidationException;
import project.exception.NotFoundException;
import project.messaging.BookingCancelledEvent;
import project.messaging.BookingCreatedEvent;
import project.messaging.BookingStatusChangedEvent;
import project.model.Booking;
import project.model.TrainingSlot;
import project.model.User;
import project.security.user.CustomUserDetails;
import project.storage.booking.BookingRepository;
import project.storage.trainingSlot.TrainingRepository;
import project.storage.user.UserRepository;
import java.util.UUID;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor //работает только с final
public class BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final TrainingRepository trainingRepository;
    private final RabbitTemplate rabbitTemplate;
    private final BookingMetrics bookingMetrics;

    public List<BookingResponseDto> getAllBookings() {
        return bookingRepository.findAll()
                .stream()
                .map(this::toBookingResponseDto)
                .toList();
    }
    //User id нужно получать из Authentication
    //Workout id получаем из запроса (передача в аргументы)
    @Transactional
    public BookingCreateRequest registerForWorkout(Long trainingSlotId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        Long userId = userDetails.getId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!Role.CLIENT.equals(user.getRole())) {
            throw new ValidationException("Role must be CLIENT");
        }

        TrainingSlot trainingSlot = trainingRepository.findByIdForUpdate(trainingSlotId)
                .orElseThrow(() -> new NotFoundException("Training not found"));

        if (bookingRepository.findByUserIdAndTrainingSlotId(userId, trainingSlotId).isPresent()) {
            bookingMetrics.recordRejected();
            throw new ValidationException("User already has a booking for this training slot");
        }

        long activeBookings = bookingRepository.countByTrainingSlotIdAndBookingStatusIn(
                trainingSlotId, List.of(BookingStatus.CREATED, BookingStatus.PENDING));
        if (activeBookings >= trainingSlot.getCapacity()) {
            bookingMetrics.recordRejected();
            throw new ValidationException("Training slot is full");
        }

        Booking booking = new Booking();

        booking.setUser(user);
        booking.setTrainingSlot(trainingSlot);
        booking.setBookingStatus(BookingStatus.CREATED);
        LocalDateTime createdAt = LocalDateTime.now();
        booking.setLocalDateTime(createdAt);

        bookingRepository.save(booking);

        BookingCreatedEvent bookingEvent = new BookingCreatedEvent(
                UUID.randomUUID().toString(), //?
                booking.getId(),
                user.getId(),
                trainingSlot.getId(),
                createdAt
        );

        rabbitTemplate.convertAndSend(
                "booking.exchange",
                "booking.created",
                bookingEvent);
        bookingMetrics.recordCreated();

        return toBookingCreateDto(booking);
    }

    @Transactional
    public void cancelBookingStatus(Long trainingSlotId, String reason) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        Long userId = userDetails.getId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        TrainingSlot trainingSlot = trainingRepository.findById(trainingSlotId)
                .orElseThrow(() -> new NotFoundException("Training not found"));

        Booking booking = bookingRepository.findByUserIdAndTrainingSlotId(userId, trainingSlotId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new ValidationException("Booking is already cancelled");
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);

        bookingRepository.save(booking);

        BookingCancelledEvent bookingCancelledEvent = new BookingCancelledEvent(
                UUID.randomUUID().toString(),
                booking.getId(),
                userId,
                trainingSlot.getWorkout().getId(),
                LocalDateTime.now(),
                reason);

        rabbitTemplate.convertAndSend(
                "booking.exchange",
                "booking.cancelled",
                bookingCancelledEvent);
        bookingMetrics.recordCancelled();
    }

    public void deleteBookingById(long id) {
        bookingRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Id not found"));

        bookingRepository.deleteById(id);
    }

    //сделать методы создания, отмены(не удаление), и получения записей getMyBooking.
    public BookingResponseDto updateBooking(Booking booking) {
        bookingRepository.findById(booking.getId())
                .orElseThrow(() -> new ValidationException("Booking not found"));

        bookingRepository.save(booking);

        return toBookingResponseDto(booking);
    }

    @Transactional
    public BookingResponseDto changeBookingStatus(long id, BookingStatus bookingStatus) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Booking not found"));

        if(booking.getBookingStatus().equals(bookingStatus)) {
            throw new ValidationException("Similar status");
        }

        booking.setBookingStatus(bookingStatus);

        bookingRepository.save(booking);

        BookingStatusChangedEvent bookingStatusChangedEvent = new BookingStatusChangedEvent(
                UUID.randomUUID().toString(),
                booking.getId(),
                booking.getUser().getId(),
                booking.getTrainingSlot().getWorkout().getId(),
                booking.getBookingStatus(),
                bookingStatus,
                LocalDateTime.now()
        );

        rabbitTemplate.convertAndSend(
                "booking.exchange",
                "booking.status.changed",
                bookingStatusChangedEvent);

        return toBookingResponseDto(booking);
    }

    public List<BookingResponseDto> findByBookingStatus(BookingStatus bookingStatus) {
        return bookingRepository.findByBookingStatus(bookingStatus)
                .stream()
                .map(this::toBookingResponseDto)
                .toList();
    }

    public BookingResponseDto toBookingResponseDto(Booking booking) {
        return new BookingResponseDto(booking.getId(), booking.getUser().getId(), booking.getTrainingSlot().getId(),
                booking.getBookingStatus());
    }

    public BookingCreateRequest toBookingCreateDto(Booking booking) {
        return new BookingCreateRequest(booking.getId(), booking.getUser().getId(), booking.getTrainingSlot().getId(),
                booking.getLocalDateTime());
    }
}
