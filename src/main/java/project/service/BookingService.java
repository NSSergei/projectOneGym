package project.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.dto.BookingCreateRequestDto;
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
import project.repository.booking.BookingRepository;
import project.repository.trainingSlot.TrainingRepository;
import project.repository.user.UserRepository;
import java.util.UUID;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor //работает только с final
public class BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final TrainingRepository trainingRepository;
    private final RabbitTemplate rabbitTemplate;
    private final BookingMetrics bookingMetrics;

    /**
     * Возвращает список записей на тренировки.
     * @return  список записей в формате DTO ответа
     */
    public List<BookingResponseDto> getAllBookings() {
        log.info("method getAllBookings start");
        List<BookingResponseDto> bookings = bookingRepository.findAll()
                .stream()
                .map(this::toBookingResponseDto)
                .toList();

        log.debug("Получен список бронирований: count={}", bookings.size());
        return bookings;
    }
    //User id нужно получать из Authentication
    //Workout id получаем из запроса (передача в аргументы)

    /**
     * Регистрирует пользователя на тренировочный слот.
    * @param trainingSlotId идентификатор тренировочного слота
    * @return DTO с данными созданной записи
     */
    @Transactional
    public BookingCreateRequestDto registerForWorkout(Long trainingSlotId) {
        log.info("method registerForWorkout start");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        Long userId = userDetails.getId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        log.info("user is find: id={}", user.getId());
        if (!Role.CLIENT.equals(user.getRole())) {
            throw new ValidationException("Role must be CLIENT");
        }

        TrainingSlot trainingSlot = trainingRepository.findByIdForUpdate(trainingSlotId)
                .orElseThrow(() -> new NotFoundException("Training not found"));

        log.info("trainingSlot is find: id={}", trainingSlot.getId());

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

        log.info("capacity is ok");

        Booking booking = new Booking();

        booking.setUser(user);
        booking.setTrainingSlot(trainingSlot);
        booking.setBookingStatus(BookingStatus.CREATED);
        LocalDateTime createdAt = LocalDateTime.now();
        booking.setLocalDateTime(createdAt);

        bookingRepository.save(booking);
        log.info("booking was saved bookingId={}, userId={}, trainingSlotId={}",booking.getId(),user.getId(),
                trainingSlot.getId());

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

    /**
     * Отменяет запись пользователя на тренировочный слот.
     * @param trainingSlotId идентификатор тренировочного слота
     * @param reason причина отмены
     */
    @Transactional
    public void cancelBookingStatus(Long trainingSlotId, String reason) {
        log.info("cancelBookingStatus start");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        Long userId = userDetails.getId();

        log.debug("Начинается отмена бронирования: userId={}, trainingSlotId={}",
                userId, trainingSlotId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        log.debug("Пользователь найден: userId={}", userId);

        TrainingSlot trainingSlot = trainingRepository.findById(trainingSlotId)
                .orElseThrow(() -> new NotFoundException("Training not found"));
        log.debug("trainingSlot: trainingSlotId={}", trainingSlotId);

        Booking booking = bookingRepository.findByUserIdAndTrainingSlotId(userId, trainingSlotId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));
        log.debug("Booking найден: BookingId={}", booking.getId());

        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new ValidationException("Booking is already cancelled");
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);

        log.info("Бронирование отменено: bookingId={}, userId={}, trainingSlotId={}",
                booking.getId(), userId, trainingSlotId);

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

        log.info("cancelBookingStatus finished");
    }

    /**
     * Удаляет запись по идентификатору.
     * @param id идентификатор записи
     */
    public void deleteBookingById(long id) {
        bookingRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Id not found"));

        bookingRepository.deleteById(id);
        log.info("Бронирование удалено: bookingId={}", id);
    }

    //сделать методы создания, отмены(не удаление), и получения записей getMyBooking.

    /**
     * Обновляет запись на тренировку.
     * @param booking запись с обновлёнными данными
     * @return обновлённая запись в формате DTO ответа
     */
    public BookingResponseDto updateBooking(Booking booking) {
        bookingRepository.findById(booking.getId())
                .orElseThrow(() -> new ValidationException("Booking not found"));

        Booking savedBook = bookingRepository.save(booking);

        log.info("Бронирование обновлено: bookingId={}, userId={}, trainingSlotId={}, status={}",
                savedBook.getId(),
                savedBook.getUser().getId(),
                savedBook.getTrainingSlot().getId(),
                savedBook.getBookingStatus());

        return toBookingResponseDto(booking);
    }

    /**
     * Изменяет статус записи на тренировку.
     * @param id идентификатор записи
     * @param bookingStatus новый статус записи
     * @return запись с обновлённым статусом в формате DTO ответа
     */
    @Transactional
    public BookingResponseDto changeBookingStatus(long id, BookingStatus bookingStatus) {
        log.info("changeBookingStatus start");

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Booking not found"));
        log.info("bookings id is:{}", booking.getId());

        BookingStatus oldStatus = booking.getBookingStatus();
        if(oldStatus.equals(bookingStatus)) {
            throw new ValidationException("Similar status");
        }

        log.info("The old={} and new={} statuses are not the same",oldStatus, bookingStatus);

        booking.setBookingStatus(bookingStatus);

        log.info("Status was change to:{}",booking.getBookingStatus());

        bookingRepository.save(booking);

        BookingStatusChangedEvent bookingStatusChangedEvent = new BookingStatusChangedEvent(
                UUID.randomUUID().toString(),
                booking.getId(),
                booking.getUser().getId(),
                booking.getTrainingSlot().getWorkout().getId(),
                oldStatus,
                bookingStatus,
                LocalDateTime.now()
        );

        rabbitTemplate.convertAndSend(
                "booking.exchange",
                "booking.status.changed",
                bookingStatusChangedEvent);

        return toBookingResponseDto(booking);
    }

    /**
     * Возвращает список записей с указанным статусом.
     * @param bookingStatus статус записи
     * @return список записей с указанным статусом в формате DTO ответа
     */
    public List<BookingResponseDto> findByBookingStatus(BookingStatus bookingStatus) {
        log.info("findByBookingStatus start");
        List<BookingResponseDto> bookings = bookingRepository.findByBookingStatus(bookingStatus)
                .stream()
                .map(this::toBookingResponseDto)
                .toList();
        log.debug("bookings size is:{}",bookings.size());
        return bookings;
    }

    /**
     * Преобразует запись на тренировку в DTO ответа.
     * @param booking запись на тренировку для преобразования
     * @return DTO ответа с данными записи
     */
    public BookingResponseDto toBookingResponseDto(Booking booking) {
        return new BookingResponseDto(booking.getId(), booking.getUser().getId(), booking.getTrainingSlot().getId(),
                booking.getBookingStatus());
    }

    /**
     * Преобразует запись на тренировку в DTO запроса.
     * @param booking запись, данные которой преобразуются
     * @return DTO запроса с данными записи
     */
    public BookingCreateRequestDto toBookingCreateDto(Booking booking) {
        return new BookingCreateRequestDto(booking.getId(), booking.getUser().getId(), booking.getTrainingSlot().getId(),
                booking.getLocalDateTime());
    }
}
