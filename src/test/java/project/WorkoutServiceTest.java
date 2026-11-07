package project;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import project.dto.WorkoutCreateRequest;
import project.exception.NotFoundException;
import project.exception.ValidationException;
import project.model.Workout;
import project.service.WorkoutService;
import project.storage.workout.WorkoutRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkoutServiceTest {
    @Mock
    private WorkoutRepository workoutRepository;
    @InjectMocks
    private WorkoutService workoutService;

    @Test
    void createWorkoutMapsRequestAndPersistsIt() {
        WorkoutCreateRequest request = new WorkoutCreateRequest("Strength", "Weights", 25);
        when(workoutRepository.save(any(Workout.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Workout result = workoutService.createWorkout(request);

        assertThat(result.getName()).isEqualTo("Strength");
        assertThat(result.getDescription()).isEqualTo("Weights");
        assertThat(result.getPrice()).isEqualTo(25);
    }

    @Test
    void listWorkoutsReturnsRepositoryResults() {
        List<Workout> workouts = List.of(new Workout("Strength", "Weights", 25));
        when(workoutRepository.findAll()).thenReturn(workouts);

        assertThat(workoutService.getAllWorkout()).containsExactlyElementsOf(workouts);
    }

    @Test
    void getWorkoutReturnsExistingWorkout() {
        Workout workout = new Workout("Strength", "Weights", 25);
        when(workoutRepository.findById(4L)).thenReturn(Optional.of(workout));

        assertThat(workoutService.getWorkoutById(4L)).isSameAs(workout);
    }

    @Test
    void getWorkoutRejectsMissingId() {
        when(workoutRepository.findById(4L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutService.getWorkoutById(4L))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Workout with id=4 not found");
    }

    @Test
    void updateWorkoutSavesAnExistingWorkout() {
        Workout workout = new Workout("Strength", "Weights", 25);
        workout.setId(4L);
        when(workoutRepository.findById(4L)).thenReturn(Optional.of(workout));
        when(workoutRepository.save(workout)).thenReturn(workout);

        assertThat(workoutService.updateWorkout(workout)).isSameAs(workout);
        verify(workoutRepository).save(workout);
    }

    @Test
    void updateWorkoutRejectsMissingWorkout() {
        Workout workout = new Workout("Strength", "Weights", 25);
        workout.setId(4L);
        when(workoutRepository.findById(4L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutService.updateWorkout(workout))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Workout not found");
        verify(workoutRepository, never()).save(any(Workout.class));
    }

    @Test
    void deleteWorkoutDeletesAnExistingWorkout() {
        when(workoutRepository.findById(4L)).thenReturn(Optional.of(new Workout("Strength", "Weights", 25)));

        workoutService.deleteWorkoutById(4L);

        verify(workoutRepository).deleteById(4L);
    }

    @Test
    void deleteWorkoutRejectsMissingWorkout() {
        when(workoutRepository.findById(4L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutService.deleteWorkoutById(4L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Workout with id=4 not found");
        verify(workoutRepository, never()).deleteById(4L);
    }
}
