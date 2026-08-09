package project.storage.trainingSlot;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.model.TrainingSlot;

@Repository
public interface TrainingRepository extends JpaRepository<TrainingSlot, Long> {
}
