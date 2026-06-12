package project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;


@Data
@ToString
@RequiredArgsConstructor

@Entity
@Table(name = "workouts")
public class Workout {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    @NotBlank(message = "Name cant be null")
    String name;
    @NotBlank(message = "Description cant be null")
    String description;
    @PositiveOrZero(message = "price cant be negative")
    int price;

    public Workout(String name, String description, int price) {
        this.name = name;
        this.description = description;
        this.price = price;
    }
}


