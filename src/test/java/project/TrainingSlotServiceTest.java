package project;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import project.dto.TrainingResponseAllSlotsDto;
import project.enums.Role;
import project.exception.ValidationException;
import project.model.TrainingSlot;
import project.model.User;
import project.model.Workout;
import project.service.TrainingSlotService;
import project.storage.trainingSlot.TrainingRepository;
import project.storage.user.UserRepository;
import project.storage.workout.WorkoutRepository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingSlotServiceTest {
    @Mock
    private TrainingRepository trainingRepository;
    @Mock
    private WorkoutRepository workoutRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private TrainingSlotService trainingSlotService;

    private User coach() {
        return User.builder().id(8L).name("Morgan").last_name("Lee").role(Role.COACH).build();
    }

    private Workout workout() {
        Workout workout = new Workout("Yoga", "Mobility", 30);
        workout.setId(12L);
        return workout;
    }

    private TrainingSlot slot() {
        return TrainingSlot.builder()
                .id(17L)
                .coach(coach())
                .workout(workout())
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .capacity(12)
                .build();
    }

    @Test
    void createTrainingConnectsExistingCoachAndWorkout() {
        TrainingSlot requested = slot();
        User coach = coach();
        Workout workout = workout();
        when(userRepository.findById(8L)).thenReturn(Optional.of(coach));
        when(workoutRepository.findById(12L)).thenReturn(Optional.of(workout));
        when(trainingRepository.save(requested)).thenReturn(requested);

        TrainingSlot result = trainingSlotService.createTraining(requested);

        assertThat(result.getCoach()).isSameAs(coach);
        assertThat(result.getWorkout()).isSameAs(workout);
    }

    @Test
    void createTrainingRejectsUnknownCoach() {
        TrainingSlot requested = slot();
        when(userRepository.findById(8L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingSlotService.createTraining(requested))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Coach не существует");
        verify(trainingRepository, never()).save(requested);
    }

    @Test
    void createTrainingRejectsUserWhoIsNotACoach() {
        TrainingSlot requested = slot();
        when(userRepository.findById(8L)).thenReturn(Optional.of(
                User.builder().id(8L).role(Role.CLIENT).build()));

        assertThatThrownBy(() -> trainingSlotService.createTraining(requested))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Ошибка роли");
        verify(workoutRepository, never()).findById(12L);
    }

    @Test
    void createTrainingRejectsUnknownWorkout() {
        when(userRepository.findById(8L)).thenReturn(Optional.of(coach()));
        when(workoutRepository.findById(12L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingSlotService.createTraining(slot()))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Тренировка не существует");
    }

    @Test
    void getTrainingSlotReturnsExistingSlot() {
        TrainingSlot slot = slot();
        when(trainingRepository.findById(17L)).thenReturn(Optional.of(slot));

        assertThat(trainingSlotService.getTrainingSlotById(17L)).isSameAs(slot);
    }

    @Test
    void getTrainingSlotRejectsMissingSlot() {
        when(trainingRepository.findById(17L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingSlotService.getTrainingSlotById(17L))
                .isInstanceOf(ValidationException.class)
                .hasMessage("TraininSlot с данным id не найдет:17");
    }

    @Test
    void updateTrainingSlotSavesExistingSlot() {
        TrainingSlot slot = slot();
        when(trainingRepository.findById(17L)).thenReturn(Optional.of(slot));
        when(trainingRepository.save(slot)).thenReturn(slot);

        assertThat(trainingSlotService.updateTrainingSlot(slot)).isSameAs(slot);
    }

    @Test
    void updateTrainingSlotRejectsMissingSlot() {
        TrainingSlot slot = slot();
        when(trainingRepository.findById(17L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingSlotService.updateTrainingSlot(slot))
                .isInstanceOf(ValidationException.class)
                .hasMessage("TraininSlot отсутствуте в BD");
        verify(trainingRepository, never()).save(slot);
    }

    @Test
    void deleteTrainingSlotDelegatesToRepository() {
        trainingSlotService.deleteTrainingSlotById(17L);

        verify(trainingRepository).deleteById(17L);
    }

    @Test
    void listTrainingSlotsMapsRelatedEntitiesAndTimes() {
        when(trainingRepository.findAll()).thenReturn(List.of(slot()));

        List<TrainingResponseAllSlotsDto> result = trainingSlotService.toTrainingResponseDtoList();

        assertThat(result).containsExactly(new TrainingResponseAllSlotsDto(
                17L, "Yoga", "Mobility", 30, "Morgan", "Lee", "COACH",
                LocalTime.of(9, 0), LocalTime.of(10, 0), 12));
    }
}
