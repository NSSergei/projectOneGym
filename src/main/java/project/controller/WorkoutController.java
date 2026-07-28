package project.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.dto.WorkoutCreateRequest;
import project.model.Workout;
import project.service.WorkoutService;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/workout")
public class WorkoutController {
    private final WorkoutService workoutService;

    @PostMapping
    public ResponseEntity<Workout> createWorkout(@Valid @RequestBody WorkoutCreateRequest workoutCreateRequest) {
        Workout response = workoutService.createWorkout(workoutCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<Workout> getAllWorkout() {
        return workoutService.getAllWorkout();
    }

    @GetMapping("/{id}")
    public Workout getWorkoutById(@PathVariable long id) {
        return workoutService.getWorkoutById(id);
    }

    @PutMapping
    public void  updateWorkout(@RequestBody Workout workout) {
        workoutService.updateWorkout(workout);
    }

    @DeleteMapping("/{id}")
    public void deleteWorkoutById(@PathVariable long id) {
        workoutService.deleteWorkoutById(id);
    }
}
