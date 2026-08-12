package project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import project.enums.BookingStatus;


public record BookingResponseDto(@NotNull long bookingId, @NotNull long userId,
                                 @NotNull long trainingId, @NotBlank BookingStatus bookingStatus) {
}
