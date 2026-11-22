package project;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import project.model.Workout;
import project.repository.workout.WorkoutRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class WorkoutRepositoryIntegrationTest {

    @Autowired
    private WorkoutRepository workoutRepository;

    @PersistenceContext
    private EntityManager entityManager;


    @Test
    void savesAndFindsWorkout() {
        Workout workout = new Workout("Strength", "Weights", 25);

        Workout savedWorkout = workoutRepository.saveAndFlush(workout);
        entityManager.clear();

        Optional<Workout> foundWorkout =
                workoutRepository.findById(savedWorkout.getId());

        assertThat(foundWorkout).isPresent();
        Workout actual = foundWorkout.orElseThrow();

        assertThat(actual.getId()).isEqualTo(savedWorkout.getId());
        assertThat(actual.getName()).isEqualTo("Strength");
        assertThat(actual.getDescription()).isEqualTo("Weights");
        assertThat(actual.getPrice()).isEqualTo(25);
    }

    @Test
    void tryToSaveThanPriceIsNegative() {
        Workout workout = new Workout("Strength", "Weights", -25);

        assertThatThrownBy(() -> workoutRepository.saveAndFlush(workout))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void tryToSaveThanPriceIsZero() {
        Workout workout = new Workout("Strength", "Weights", 0);

        Workout save = workoutRepository.saveAndFlush(workout);
        entityManager.clear();

        Optional<Workout> foundWorkout =
                workoutRepository.findById(save.getId());

        assertThat(foundWorkout).isPresent();
        Workout actual = foundWorkout.orElseThrow();

        assertThat(actual.getId()).isEqualTo(save.getId());
    }

    @Test
    void tryFindWorkoutByIdWhenIdIsNotFound() {
        Optional<Workout> foundWorkout = workoutRepository.findById(-29L);

        assertThat(foundWorkout).isEmpty();
    }

    @Test
    void deleteWorkout() {
        Workout workout = new Workout("Strength", "Weights", 666);
        Workout save = workoutRepository.saveAndFlush(workout);

        workoutRepository.deleteById(save.getId());
        workoutRepository.flush();
        entityManager.clear();

        Optional<Workout> foundWorkout = workoutRepository.findById(save.getId());

        assertThat(foundWorkout).isEmpty();
    }

    @Test
    void updateWorkout() {
        Workout workout1 = new Workout("Strength", "Weights", 222);
        Workout save1 = workoutRepository.saveAndFlush(workout1);

        Workout workout2 = new Workout("Strength", "Weights", 333);
        workout2.setId(workout1.getId());

        workoutRepository.saveAndFlush(workout2);
        entityManager.clear();

        Optional<Workout> foundWorkout =
                workoutRepository.findById(workout1.getId());

        assertThat(foundWorkout).isPresent();

        Workout actual = foundWorkout.orElseThrow();
        assertThat(actual.getPrice()).isEqualTo(333);
    }
}