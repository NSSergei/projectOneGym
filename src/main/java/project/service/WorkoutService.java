package project.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import project.dto.WorkoutCreateRequestDto;
import project.exception.NotFoundException;
import project.exception.ValidationException;
import project.model.Workout;
import project.repository.workout.WorkoutRepository;

import java.util.List;

@Service
@Slf4j
public class WorkoutService {
    private final WorkoutRepository workoutRepository;

    public WorkoutService(WorkoutRepository workoutRepository) {
        this.workoutRepository = workoutRepository;
    }

    /**
     * Создание тренировки
     * @param workoutCreateRequestDto DTO данные создоваемой тренировки
     * @return возвращение сохраненного объекта
     */
    @CacheEvict(value = {"workout", "workouts"}, allEntries = true)
    public Workout createWorkout(WorkoutCreateRequestDto workoutCreateRequestDto) {
        Workout workout = workoutRepository.save(toMaptoWorkout(workoutCreateRequestDto));
        log.info("workout was created id:{}", workout.getId());
        return workout;
    }

    /**
     * Получение списка тренировок
     * @return список тренировок
     */
    @Cacheable(value = "workouts")
    public List<Workout> getAllWorkout() {
        List<Workout> lst =  workoutRepository.findAll();
        log.debug("Workouts lst size is{}", lst.size());
        return lst;
    }

    /**
     * Получение тренировки по идентификатору
     * @param id идентификатор тренировки
     * @return результат поиска
     */
    @Cacheable(value = "workout", key = "#id")
    public Workout getWorkoutById(long id) {
        Workout workout = workoutRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Workout with id=" + id + " not found"));
        log.debug("workout was find id:{}",workout.getId());
        return workout;
    }

    /**
     * Обновление тренировки
     * @param workout запись с обновленными данными
     * @return обновленная запись
     */
    @Caching(
            put = @CachePut(value = "workout", key = "#workout.id"),
            evict = @CacheEvict(value = "workouts", allEntries = true)
    )
    public Workout updateWorkout(Workout workout) {
        workoutRepository.findById(workout.getId())
                .orElseThrow(() -> new NotFoundException("Workout not found"));

        Workout savedWorkout = workoutRepository.save(workout);
        log.info("Тренировка обновлена: workoutId={}", savedWorkout.getId());

        return savedWorkout;
    }

    /**
     * Удаление тренировки по идентификатору
     * @param id идентификатор записи
     */
    @Caching(evict = {
            @CacheEvict(value = "workout", key = "#id"),
            @CacheEvict(value = "workouts", allEntries = true)
        }
    )
    public void deleteWorkoutById(long id) {
        workoutRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Workout with id=" + id + " not found"));
        workoutRepository.deleteById(id);
        log.info("workout was delete id:{}", id);
    }

    /**
     * Преобразование DTO в Workout
     * @param request объект DTO
     * @return объект Workout
     */
    public Workout toMaptoWorkout(WorkoutCreateRequestDto request) {
        return new Workout(request.name(), request.description(),
                request.price());
    }
}
