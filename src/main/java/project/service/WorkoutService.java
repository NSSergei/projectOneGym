package project.service;

import org.hibernate.sql.Update;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import project.dto.WorkoutCreateRequest;
import project.exception.NotFoundException;
import project.exception.ValidationException;
import project.model.Workout;
import project.storage.workout.WorkoutRepository;

import java.util.List;

@Service
public class WorkoutService {
    private final WorkoutRepository workoutRepository;

    public WorkoutService(WorkoutRepository workoutRepository) {
        this.workoutRepository = workoutRepository;
    }

    @CacheEvict(value = {"workout", "workouts"}, allEntries = true)
    public Workout createWorkout(WorkoutCreateRequest workoutCreateRequest) {
        return workoutRepository.save(toMaptoWorkout(workoutCreateRequest));
    }

    @Cacheable(value = "workouts")
    public List<Workout> getAllWorkout() {
        return workoutRepository.findAll();
    }

    @Cacheable(value = "workout", key = "#id")
    public Workout getWorkoutById(long id) {
        return workoutRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Workout with id=" + id + " not found"));
    }

    @Caching(
            put = @CachePut(value = "workout", key = "#workout.id"),
            evict = @CacheEvict(value = "workouts", allEntries = true)
    )
    public Workout updateWorkout(Workout workout) {
        workoutRepository.findById(workout.getId())
                .orElseThrow(() -> new NotFoundException("Workout not found"));
        return workoutRepository.save(workout);
    }

    @Caching(evict = {
            @CacheEvict(value = "workout", key = "#id"),
            @CacheEvict(value = "workouts", allEntries = true)
        }
    )
    public void deleteWorkoutById(long id) {
        workoutRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Workout with id=" + id + " not found"));

        workoutRepository.deleteById(id);
    }

    public Workout toMaptoWorkout(WorkoutCreateRequest request) {
        return new Workout(request.name(), request.description(),
                request.price());
    }
}
