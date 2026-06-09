/*package project.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.model.Workout;
import project.service.WorkoutService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/workout")
public class WorkoutController {
    private final WorkoutService workoutService;

    @PostMapping
    public ResponseEntity<Workout> createWorkout(@Valid @RequestBody Workout workout) {
        Workout response = workoutService.createWorkout(workout);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
*/