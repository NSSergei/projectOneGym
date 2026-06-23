package project.dto;

import jakarta.validation.constraints.NotBlank;

public record UserLoginRequest(@NotBlank String email, @NotBlank String pass) {

}
