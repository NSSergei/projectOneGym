package project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record BookingCreateRequest(@NotBlank long slotId, @NotNull long userId, @NotNull long workoutId,
                                   @NotBlank LocalDateTime localDateTime) {

}
