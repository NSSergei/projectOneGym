package project.dto;

import jakarta.validation.constraints.NotNull;

public record WorkoutCreateRequest(@NotNull String name, @NotNull String description, @NotNull int price) {

}
