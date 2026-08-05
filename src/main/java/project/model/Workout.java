package project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@Data
@ToString
@RequiredArgsConstructor

@Entity
@Table(name = "workouts")
public class Workout implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    @Column(name = "workout_name", nullable = false)
    String name;
    @Column(name = "description", nullable = false)
    String description;
    @PositiveOrZero(message = "price cant be negative")
    @Column(name = "price", nullable = false)
    int price;

    public Workout(String name, String description, int price) {
        this.name = name;
        this.description = description;
        this.price = price;
    }
}


