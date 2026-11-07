package project.storage.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.enums.BookingStatus;
import project.model.Booking;

import java.util.List;
import java.util.Collection;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookingStatus(BookingStatus status);

    Optional<Booking> findByUserIdAndTrainingSlotId(Long userId, Long trainingSlotId);

    long countByTrainingSlotIdAndBookingStatusIn(Long trainingSlotId, Collection<BookingStatus> statuses);
}
