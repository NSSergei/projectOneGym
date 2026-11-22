package project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record WorkoutCreateRequestDto(@NotBlank String name, @NotBlank String description, @PositiveOrZero int price) {

}
