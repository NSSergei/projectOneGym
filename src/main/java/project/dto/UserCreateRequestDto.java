package project.dto;

import jakarta.validation.constraints.*;

public record UserCreateRequestDto(@NotBlank String name, @NotBlank String lastName,
                                   @NotBlank @Email String email,
                                   @NotBlank String pass) {

}
