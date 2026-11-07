package project.dto;

import jakarta.validation.constraints.*;

public record UserCreateRequest(@NotBlank String name, @NotBlank String last_name,
                                @NotBlank @Email String email,
                                @NotBlank String pass) {

}
