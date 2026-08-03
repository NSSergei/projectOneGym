package project.service;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import project.dto.BookingCreateRequest;
import project.dto.BookingResponseDto;
import project.enums.BookingStatus;
import project.exception.ValidationException;
import project.model.Booking;
import project.model.User;
import project.model.Workout;
import project.security.user.CustomUserDetails;
import project.storage.booking.BookingRepository;
import project.storage.user.UserRepository;
import project.storage.workout.WorkoutRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor //работает только с final
public class BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final WorkoutRepository workoutRepository;

    public List<BookingResponseDto> getAllBookings() {
        return bookingRepository.findAll()
                .stream()
                .map(this::toBookingResponseDto)
                .toList();
    }
    //User id нужно получать из Authentication
    //Workout id получаем из запроса (передача в аргументы)
    public BookingCreateRequest registerForWorkout(Long workoutId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        Long userId = userDetails.getId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Workout workout = workoutRepository.findById(workoutId)
                .orElseThrow(() -> new RuntimeException("Workout not found"));

        Booking booking = new Booking();

        booking.setUser(user);
        booking.setWorkout(workout);
        booking.setBookingStatus(BookingStatus.ACTIVE);
        booking.setLocalDateTime(LocalDateTime.now());

        bookingRepository.save(booking);

        return toBookingCreateDto(booking);
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

        return toBookingResponseDto(booking);
    }

    public List<BookingResponseDto> findByBookingStatus(BookingStatus bookingStatus) {
        return bookingRepository.findByBookingStatus(bookingStatus)
                .stream()
                .map(this::toBookingResponseDto)
                .toList();
    }

    public BookingResponseDto toBookingResponseDto(Booking booking) {
        return new BookingResponseDto(booking.getId(), booking.getUser().getId(), booking.getWorkout().getId(),
                booking.getBookingStatus());
    }

    public BookingCreateRequest toBookingCreateDto(Booking booking) {
        return new BookingCreateRequest(booking.getId(), booking.getUser().getId(), booking.getWorkout().getId(),
                booking.getLocalDateTime());
    }
}
