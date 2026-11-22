package project.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record BookingCreateRequestDto(long slotId, long userId, long trainingId,
                                      @NotNull LocalDateTime localDateTime) {

}
