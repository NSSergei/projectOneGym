package project.service;

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

    public Workout createWorkout(WorkoutCreateRequest workoutCreateRequest) {
        return workoutRepository.save(toMaptoWorkout(workoutCreateRequest));
    }

    public List<Workout> getAllWorkout() {
        return workoutRepository.findAll();
    }

    public Workout getWorkoutById(long id) {
        return workoutRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Workout with id=" + id + " not found"));
    }

    public Workout updateWorkout(Workout workout) {
        workoutRepository.findById(workout.getId())
                .orElseThrow(() -> new NotFoundException("Workout not found"));

        return workoutRepository.save(workout);
    }

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
