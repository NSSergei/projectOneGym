package project.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record BookingCreateRequest(long slotId, long userId, long trainingId,
                                   @NotNull LocalDateTime localDateTime) {

}
