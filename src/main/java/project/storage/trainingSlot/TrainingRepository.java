package project.storage.trainingSlot;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.model.TrainingSlot;

@Repository
public interface TrainingRepository extends JpaRepository<TrainingSlot, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select slot from TrainingSlot slot where slot.id = :id")
    java.util.Optional<TrainingSlot> findByIdForUpdate(@Param("id") Long id);
}
