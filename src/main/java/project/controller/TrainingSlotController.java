package project.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import project.dto.TrainingResponseAllSlotsDto;
import project.model.TrainingSlot;
import project.service.TrainingSlotService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/training")
public class TrainingSlotController {
    private final TrainingSlotService trainingSlotService;

    @GetMapping
    public List<TrainingResponseAllSlotsDto> getTraining() {
        return trainingSlotService.toTrainingResponseDtoList();
    }

    @PostMapping
    public TrainingSlot creatTrainingSlot(@Valid @RequestBody TrainingSlot trainingSlot) {
        return trainingSlotService.createTraining(trainingSlot);
    }

    @GetMapping("/{id}")
    public TrainingSlot getTrainingById(@PathVariable long id) {
        return trainingSlotService.getTrainingSlotById(id);
    }

    @PutMapping
    public TrainingSlot updateTraining(@Valid @RequestBody TrainingSlot trainingSlot) {
        return trainingSlotService.updateTrainingSlot(trainingSlot);
    }

    @DeleteMapping("/{id}")
    public void deleteTrainingById(@PathVariable long id) {
        trainingSlotService.deleteTrainingSlotById(id);
    }
}
