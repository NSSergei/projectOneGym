package project.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import project.dto.TrainingResponseAllSlotsDto;
import project.enums.Role;
import project.exception.ValidationException;
import project.model.TrainingSlot;
import project.model.User;
import project.model.Workout;
import project.storage.trainingSlot.TrainingRepository;
import project.storage.user.UserRepository;
import project.storage.workout.WorkoutRepository;

import java.util.List;

@Service
public class TrainingSlotService {
    private final TrainingRepository trainingRepository;
    private final WorkoutRepository workoutRepository;
    private final UserRepository userRepository;

    public TrainingSlotService(TrainingRepository trainingRepository,
                               WorkoutRepository workoutRepository, UserRepository userRepository) {
        this.trainingRepository = trainingRepository;
        this.workoutRepository = workoutRepository;
        this.userRepository = userRepository;
    }

    public TrainingSlot createTraining(TrainingSlot trainingSlot) {
        User coach = userRepository.findById(trainingSlot.getCoach().getId())
                .orElseThrow(() -> new ValidationException("Coach не существует"));

        if (!Role.COACH.equals(coach.getRole())) {
            throw new  ValidationException("Ошибка роли");
        }

        Workout workout = workoutRepository.findById(trainingSlot.getWorkout().getId())
                .orElseThrow(() -> new ValidationException("Тренировка не существует"));

        trainingSlot.setCoach(coach);
        trainingSlot.setWorkout(workout);
        return trainingRepository.save(trainingSlot);
    }

    @Cacheable(value = "trainingSlot", key = "#id")
    public TrainingSlot getTrainingSlotById(Long id) {
        return trainingRepository.findById(id)
                .orElseThrow(() -> new ValidationException("TraininSlot с данным id не найдет:" + id));
    }

    public TrainingSlot updateTrainingSlot(TrainingSlot trainingSlot) {
        trainingRepository.findById(trainingSlot.getId())
                .orElseThrow(() -> new ValidationException("TraininSlot отсутствуте в BD"));
        return trainingRepository.save(trainingSlot);
    }

    public void deleteTrainingSlotById(Long id) {
        trainingRepository.deleteById(id);
    }

    public List<TrainingResponseAllSlotsDto> toTrainingResponseDtoList() {
        return trainingRepository.findAll()
                .stream()
                .map(this::toTrainingResponseDto)
                .toList();
    }

    public TrainingResponseAllSlotsDto toTrainingResponseDto(TrainingSlot trainingSlot) {
        return new TrainingResponseAllSlotsDto(
                trainingSlot.getId(),
                trainingSlot.getWorkout().getName(),
                trainingSlot.getWorkout().getDescription(),
                trainingSlot.getWorkout().getPrice(),
                trainingSlot.getCoach().getName(),
                trainingSlot.getCoach().getLast_name(),
                trainingSlot.getCoach().getRole().name(),
                trainingSlot.getStartTime(),
                trainingSlot.getEndTime(),
                trainingSlot.getCapacity()
        );
    }
}
