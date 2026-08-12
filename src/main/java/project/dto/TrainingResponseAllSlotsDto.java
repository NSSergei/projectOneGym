package project.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record TrainingResponseAllSlotsDto(@NotNull Long id,

                                          @NotNull String workoutName,
                                          @NotNull String workoutDescription,
                                          @NotNull Integer workoutPrice,

                                          @NotNull String coachName,
                                          @NotNull String coachLastName,
                                          @NotNull String coachRole,

                                          @NotNull LocalTime startTime,
                                          @NotNull LocalTime endTime,
                                          @NotNull Integer capacity) {
}