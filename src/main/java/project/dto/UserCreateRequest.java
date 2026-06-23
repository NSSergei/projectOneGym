package project.dto;

import jakarta.validation.constraints.*;

public record UserCreateRequest(@NotBlank String name, @NotBlank String last_name, @NotBlank String email,
                                @NotBlank String pass) {

}
