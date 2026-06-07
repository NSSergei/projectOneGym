package project.model;

import jakarta.validation.constraints.NegativeOrZero;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Workout {
    @PositiveOrZero(message = "Id cant be negative")
    long id;
    @NotBlank(message = "Name cant be null")
    String name;
    @NotBlank(message = "Description cant be null")
    String description;
    @PositiveOrZero(message = "price cant be negative")
    int price;
}
