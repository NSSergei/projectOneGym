package project.dto;

import jakarta.validation.constraints.NotBlank;
import project.enums.Role;

public record UserResponseDto(@NotBlank String name, @NotBlank String lastName, @NotBlank String email,
                              @NotBlank Role role) {
}
