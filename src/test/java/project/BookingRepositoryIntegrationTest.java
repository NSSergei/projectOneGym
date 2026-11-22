package project;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import project.enums.BookingStatus;
import project.enums.Role;
import project.model.Booking;
import project.model.TrainingSlot;
import project.model.User;
import project.model.Workout;
import project.repository.booking.BookingRepository;
import project.repository.trainingSlot.TrainingRepository;
import project.repository.user.UserRepository;
import project.repository.workout.WorkoutRepository;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BookingRepositoryIntegrationTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TrainingRepository trainingRepository;

    @Autowired
    private WorkoutRepository workoutRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void findsBookingsByStatus() {
        TrainingSlot slot = createTrainingSlot();
        User client = createUser("client@example.com", Role.CLIENT);
        User anotherClient = createUser("another@example.com", Role.CLIENT);

        Booking createdBooking =
                saveBooking(client, slot, BookingStatus.CREATED);
        saveBooking(anotherClient, slot, BookingStatus.CANCELLED);
        entityManager.clear();

        List<Booking> foundBookings =
                bookingRepository.findByBookingStatus(BookingStatus.CREATED);

        assertThat(foundBookings)
                .extracting(Booking::getId)
                .containsExactly(createdBooking.getId());
    }

    @Test
    void findsBookingByUserIdAndTrainingSlotId() {
        TrainingSlot slot = createTrainingSlot();
        User client = createUser("client@example.com", Role.CLIENT);
        Booking savedBooking =
                saveBooking(client, slot, BookingStatus.CREATED);
        entityManager.clear();

        Optional<Booking> foundBooking =
                bookingRepository.findByUserIdAndTrainingSlotId(
                        client.getId(),
                        slot.getId()
                );

        assertThat(foundBooking).isPresent();
        assertThat(foundBooking.orElseThrow().getId())
                .isEqualTo(savedBooking.getId());

        Optional<Booking> missingBooking =
                bookingRepository.findByUserIdAndTrainingSlotId(
                        Long.MAX_VALUE,
                        slot.getId()
                );

        assertThat(missingBooking).isEmpty();
    }

    @Test
    void countsBookingsForSlotWithSelectedStatuses() {
        TrainingSlot slot = createTrainingSlot();

        saveBooking(
                createUser("created@example.com", Role.CLIENT),
                slot,
                BookingStatus.CREATED
        );
        saveBooking(
                createUser("pending@example.com", Role.CLIENT),
                slot,
                BookingStatus.PENDING
        );
        saveBooking(
                createUser("cancelled@example.com", Role.CLIENT),
                slot,
                BookingStatus.CANCELLED
        );
        entityManager.clear();

        long count = bookingRepository.countByTrainingSlotIdAndBookingStatusIn(
                slot.getId(),
                List.of(BookingStatus.CREATED, BookingStatus.PENDING)
        );

        assertThat(count).isEqualTo(2);
    }

    @Test
    void deletesBooking() {
        TrainingSlot slot = createTrainingSlot();
        User client = createUser("delete@example.com", Role.CLIENT);
        Booking savedBooking =
                saveBooking(client, slot, BookingStatus.CREATED);

        bookingRepository.deleteById(savedBooking.getId());
        bookingRepository.flush();
        entityManager.clear();

        assertThat(bookingRepository.findById(savedBooking.getId())).isEmpty();
    }

    private TrainingSlot createTrainingSlot() {
        Workout workout = workoutRepository.saveAndFlush(
                new Workout("Strength", "Weights", 25)
        );

        User coach = createUser("coach@example.com", Role.COACH);

        TrainingSlot slot = TrainingSlot.builder()
                .workout(workout)
                .coach(coach)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .capacity(10)
                .build();

        return trainingRepository.saveAndFlush(slot);
    }

    private User createUser(String email, Role role) {
        User user = User.builder()
                .name("Test")
                .last_name("User")
                .email(email)
                .pass("test-password")
                .role(role)
                .balance(0)
                .build();

        return userRepository.saveAndFlush(user);
    }

    private Booking saveBooking(
            User user,
            TrainingSlot slot,
            BookingStatus status
    ) {
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setTrainingSlot(slot);
        booking.setBookingStatus(status);
        booking.setLocalDateTime(
                LocalDateTime.of(2026, 1, 10, 10, 0)
        );

        return bookingRepository.saveAndFlush(booking);
    }
}