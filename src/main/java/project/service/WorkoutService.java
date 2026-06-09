/*package project.service;

import org.springframework.stereotype.Service;
import project.model.Workout;
import project.storage.workout.WorkoutRepository;

import java.util.List;

@Service
public class WorkoutService {
    private final WorkoutRepository workoutRepository;

    public WorkoutService(WorkoutRepository workoutRepository) {
        this.workoutRepository = workoutRepository;
    }

    public List<Workout> getAllWorkout() {
        return workoutRepository.findAll();
    }

    public Workout createWorkout(Workout workout) {
        return workoutRepository.save(workout);
    }
}
*/