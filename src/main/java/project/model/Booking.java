package project.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import project.enums.BookingStatus;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@ToString
@Entity
@Table(name = "booking")
public class Booking implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToOne(fetch = FetchType.LAZY) //
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_id", nullable = false)
    private Workout workout;
    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status",nullable = false)
    private BookingStatus bookingStatus;
    @Column(name = "creation_date",nullable = false) //означает что при добавлении/получении поле должно быть не
    // нулевое
    private LocalDateTime localDateTime;
}
