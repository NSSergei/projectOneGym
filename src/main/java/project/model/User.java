package project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import project.enums.Role;

@Data
@ToString

@Entity
@Table(name = "users")
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    @Column(name = "user_name", nullable = false)
    String name;
    @Column(name = "last_name", nullable = false)
    String last_name;
    @Email(message = "Email должна содержать символ @")
    @Column(name = "email", nullable = false)
    String email;
    String pass;
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    Role role;
    @PositiveOrZero(message = "balance cant be negative")
    @Column(name = "balance", nullable = false)
    int balance;

    public User(String name, String last_name, String email, String pass, Role role) {
        this.name = name;
        this.last_name = last_name;
        this.email = email;
        this.pass = pass;
        this.role = role;
    }
}
