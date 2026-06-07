package project.dto;

import jakarta.validation.constraints.*;

public record UserCreateRequest(@NotNull String name, @NotNull String last_name, @NotNull String email,
                                @NotNull String pass, @NotNull String role) {

}
