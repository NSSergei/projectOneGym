package project.messaging;

public record BookingCreatedEvent(
        Long bookingId,
        Long userId,
        Long trainingSlotId
) {
}