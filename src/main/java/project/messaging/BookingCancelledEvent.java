package project.messaging;

import java.time.LocalDateTime;

public record BookingCancelledEvent(
    String eventId,
    Long bookingId,
    Long userId,
    Long workoutId,
    LocalDateTime cancelledAt,
    String reason
) {
}


