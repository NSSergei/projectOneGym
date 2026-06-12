package project.service;

import org.springframework.stereotype.Service;
import project.dto.WorkoutCreateRequest;
import project.model.Workout;
import project.storage.workout.WorkoutRepository;

import java.util.List;
import java.util.Optional;

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

    public Optional<Workout> getWorkoutById(long id) {
        return workoutRepository.findById(id);
    }

    public void deleteWorkoutById(long id) {
        workoutRepository.deleteById(id);
    }

    public Workout toMaptoWorkout(WorkoutCreateRequest request) {
        return new Workout(request.name(), request.description(),
                request.price());
    }
}
