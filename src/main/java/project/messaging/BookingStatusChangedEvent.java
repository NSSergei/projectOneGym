package project.messaging;

import project.enums.BookingStatus;

import java.time.LocalDateTime;

public record BookingStatusChangedEvent(
        String eventId,
        Long bookingId,
        Long userId,
        Long workoutId,
        BookingStatus oldStatus,
        BookingStatus newStatus,
        LocalDateTime changedAt
) {
}

