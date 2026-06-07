package project.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@ToString
public class Booking {
    @PositiveOrZero(message = "Id cant be negative")
    long id;
    @NotNull(message = "Slot Id cant be null")
    int slotId;
    @NotNull(message = "Slot Id cant be null")
    int userId;
    @NotBlank(message = "Status cant be null")
    String status;
    @NotBlank(message = "Create cant be null")
    LocalDateTime createdAt;
}
