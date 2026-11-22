package project.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import project.dto.TrainingResponseAllSlotsDto;
import project.enums.Role;
import project.exception.ValidationException;
import project.model.TrainingSlot;
import project.model.User;
import project.model.Workout;
import project.repository.trainingSlot.TrainingRepository;
import project.repository.user.UserRepository;
import project.repository.workout.WorkoutRepository;

import java.util.List;

@Service
@Slf4j
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

    /**
     * Создание тренировочного слота
     * @param trainingSlot данные создаваемого тренировочного слота
     * @return возвращает сохраненный объект TrainingSlot
     */
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

        TrainingSlot saved = trainingRepository.save(trainingSlot);
        log.info("Тренировочный слот создан: slotId={}, coachId={}, workoutId={}",
                saved.getId(), coach.getId(), workout.getId());

        return saved;
    }

    /**
     * Получение слота по идентификатору
     * @param id идентификатор записи
     * @return получение результата поиска по идентификатору
     */
    @Cacheable(value = "trainingSlot", key = "#id")
    public TrainingSlot getTrainingSlotById(Long id) {
        TrainingSlot trainingSlot =  trainingRepository.findById(id)
                .orElseThrow(() -> new ValidationException("TraininSlot с данным id не найдет:" + id));
        log.info("training slot id:{}",trainingSlot.getId());

        return trainingSlot;
    }

    /**
     * Обновление данных слота
     * @param trainingSlot запись с обновлёнными данными
     * @return обновлённая запись слота
     */
    public TrainingSlot updateTrainingSlot(TrainingSlot trainingSlot) {
        trainingRepository.findById(trainingSlot.getId())
                .orElseThrow(() -> new ValidationException("TraininSlot отсутствуте в BD"));

        TrainingSlot saved = trainingRepository.save(trainingSlot);
        log.info("Тренировочный слот обновлён: slotId={}", saved.getId());

        return saved;
    }

    /**
     * Удаляет запись по её идентификатору.
     * @param id идентификатор записи
     */
    public void deleteTrainingSlotById(Long id) {
        trainingRepository.deleteById(id);
        log.info("TrainingSlot was deleted id:{}",id);
    }

    /**
     * Получение все слотов
     * @return список всех слотов в формате DTO
     */
    public List<TrainingResponseAllSlotsDto> toTrainingResponseDtoList() {
        List<TrainingResponseAllSlotsDto> list = trainingRepository.findAll()
                .stream()
                .map(this::toTrainingResponseDto)
                .toList();

        log.debug("TrainingResponseDtoList size is{}",list.size());
        return list;
    }

    /**
     * Преобразует запись слотов в DTO
     * @param trainingSlot запись для преобразования в DTO
     * @return запись преобразовання в DTO
     */
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
