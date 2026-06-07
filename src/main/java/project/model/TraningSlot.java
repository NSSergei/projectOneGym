package project.model;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.ToString;

import java.time.LocalTime;

@Data
@ToString
public class TraningSlot {
    @PositiveOrZero(message = "Id cant be negative")
    long id;
    @PositiveOrZero(message = "Workout id cant be negative")
    long workoutId;
    @PositiveOrZero(message = "CoachId id cant be negative")
    long coachId;
    LocalTime startTime;
    LocalTime endTime;
    int capacity;
}
