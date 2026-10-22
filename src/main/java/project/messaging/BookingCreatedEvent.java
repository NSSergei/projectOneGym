package project.messaging;

import java.time.LocalDateTime;

public record BookingCreatedEvent(
        String eventId,
        Long bookingId,
        Long userId,
        Long trainingSlotId,
        LocalDateTime createdAt
) {
}