package project;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import project.enums.Role;
import project.model.TrainingSlot;
import project.model.User;
import project.model.Workout;

import project.repository.trainingSlot.TrainingRepository;
import project.repository.user.UserRepository;
import project.repository.workout.WorkoutRepository;

import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest

public class TrainingSlotRepositoryIntegrationTest {
    @Autowired
    private TrainingRepository trainingRepository;

    @Autowired
    private WorkoutRepository workoutRepository;

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void findsTrainingSlotForUpdate() {
        Workout workout = workoutRepository.saveAndFlush(
                new Workout("Strength", "Weights", 25)
        );

        User coach = userRepository.saveAndFlush(
                User.builder()
                        .name("Sergei")
                        .last_name("Coach")
                        .email("Sergei.coach@test.com")
                        .pass("test-password")
                        .role(Role.COACH)
                        .balance(0)
                        .build()
        );

        TrainingSlot slot = TrainingSlot.builder()
                .workout(workout)
                .coach(coach)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .capacity(10)
                .build();

        TrainingSlot savedSlot = trainingRepository.saveAndFlush(slot);
        entityManager.clear();

        Optional<TrainingSlot> foundSlot =
                trainingRepository.findByIdForUpdate(savedSlot.getId());

        assertThat(foundSlot).isPresent();

        TrainingSlot actual = foundSlot.orElseThrow();
        assertThat(actual.getId()).isEqualTo(savedSlot.getId());
        assertThat(actual.getStartTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(actual.getEndTime()).isEqualTo(LocalTime.of(11, 0));
        assertThat(actual.getCapacity()).isEqualTo(10);
        assertThat(actual.getWorkout().getName()).isEqualTo("Strength");
        assertThat(actual.getCoach().getEmail())
                .isEqualTo("Sergei.coach@test.com");
    }

    @Test
    void returnsEmptyWhenTrainingSlotIdIsNotFound() {
        Optional<TrainingSlot> foundSlot =
                trainingRepository.findByIdForUpdate(Long.MAX_VALUE);

        assertThat(foundSlot).isEmpty();
    }

    @Test
    void deletesTrainingSlot() {
        Workout workout = workoutRepository.saveAndFlush(
                new Workout("Strength", "Weights", 25)
        );

        User coach = userRepository.saveAndFlush(
                User.builder()
                        .name("Sergei")
                        .last_name("Coach")
                        .email("delete.coach@test.com")
                        .pass("test-password")
                        .role(Role.COACH)
                        .balance(0)
                        .build()
        );

        TrainingSlot slot = TrainingSlot.builder()
                .workout(workout)
                .coach(coach)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .capacity(10)
                .build();

        TrainingSlot savedSlot = trainingRepository.saveAndFlush(slot);

        trainingRepository.deleteById(savedSlot.getId());
        trainingRepository.flush();
        entityManager.clear();

        assertThat(trainingRepository.findById(savedSlot.getId())).isEmpty();
    }
}
