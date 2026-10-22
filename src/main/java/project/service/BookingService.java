package project.service;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import project.dto.BookingCreateRequest;
import project.dto.BookingResponseDto;
import project.enums.BookingStatus;
import project.enums.Role;
import project.exception.ValidationException;
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

    public List<BookingResponseDto> getAllBookings() {
        return bookingRepository.findAll()
                .stream()
                .map(this::toBookingResponseDto)
                .toList();
    }
    //User id нужно получать из Authentication
    //Workout id получаем из запроса (передача в аргументы)
    public BookingCreateRequest registerForWorkout(Long trainingSlotId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        Long userId = userDetails.getId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!Role.CLIENT.equals(user.getRole())) {
            throw new ValidationException("Role must be CLIENT");
        }

        TrainingSlot trainingSlot = trainingRepository.findById(trainingSlotId)
                .orElseThrow(() -> new RuntimeException("Training not found"));

        Booking booking =  bookingRepository.existsByUserIdAndTrainingSlotId(userId, trainingSlotId)
                        .orElseThrow(() -> new ValidationException("Booking not found"));


        booking.setUser(user);
        booking.setTrainingSlot(trainingSlot);
        booking.setBookingStatus(BookingStatus.CREATED);
        booking.setLocalDateTime(LocalDateTime.now());

        bookingRepository.save(booking);

        BookingCreatedEvent bookingEvent = new BookingCreatedEvent(
                UUID.randomUUID().toString(), //?
                booking.getId(),
                user.getId(),
                trainingSlot.getId(),
                LocalDateTime.now()
        );

        rabbitTemplate.convertAndSend(
                "booking.exchange",
                "booking.created",
                bookingEvent);

        return toBookingCreateDto(booking);
    }

    public void cancelBookingStatus(Long trainingSlotId, String reason) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        Long userId = userDetails.getId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ValidationException("User not found"));

        TrainingSlot trainingSlot = trainingRepository.findById(trainingSlotId)
                .orElseThrow(() -> new ValidationException("Training not found"));

        Booking booking = bookingRepository.existsByUserIdAndTrainingSlotId(userId, trainingSlotId)
                .orElseThrow(() -> new ValidationException("Booking not found"));

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
    }

    public void deleteBookingById(long id) {
        bookingRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Id not found"));

        bookingRepository.deleteBookingById(id);
    }

    //сделать методы создания, отмены(не удаление), и получения записей getMyBooking.
    public BookingResponseDto updateBooking(Booking booking) {
        bookingRepository.findById(booking.getId())
                .orElseThrow(() -> new ValidationException("Booking not found"));

        bookingRepository.save(booking);

        return toBookingResponseDto(booking);
    }

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
