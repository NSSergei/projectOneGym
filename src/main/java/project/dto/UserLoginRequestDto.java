package project.dto;

import jakarta.validation.constraints.NotBlank;

public record UserLoginRequestDto(@NotBlank String email, @NotBlank String pass) {

}
