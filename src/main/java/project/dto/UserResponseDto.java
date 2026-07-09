package project.dto;

import jakarta.validation.constraints.NotBlank;

public record UserResponseDto(@NotBlank String name, @NotBlank String last_name, @NotBlank String email) {
}
