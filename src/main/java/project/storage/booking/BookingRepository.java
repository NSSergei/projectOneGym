package project.storage.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.enums.BookingStatus;
import project.model.Booking;
import project.model.User;
import project.model.Workout;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findById(long id);

    boolean existsByUser(User user);

    boolean existsById(Booking booking);

    //boolean existsByWorkout(Workout workout);

    void deleteBookingById(long id);

    List<Booking> findByBookingStatus(BookingStatus status);

}
