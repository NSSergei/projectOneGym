package project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;


@Data
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    //@NotBlank(message = "Name cant be null")
    String name;
    @NotBlank(message = "Name cant be null")
    String last_name;
    @Email(message = "Email должна содержать символ @")
    String email;
    @Size(min = 6, max = 20, message = "Password must have 6el - 20el size")
    String pass;
    @NotBlank
    String role;
    @PositiveOrZero(message = "balance cant be negative")
    int balance;

    public User(String name, String last_name, String email, String pass, String role) {
        this.name = name;
        this.last_name = last_name;
        this.email = email;
        this.pass = pass;
        this.role = role;
    }
}
