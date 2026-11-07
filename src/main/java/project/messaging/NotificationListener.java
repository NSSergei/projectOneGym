package project.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificationListener {

    @RabbitListener(queues = "booking.created.queue")
    public void handleBookingCreated(BookingCreatedEvent event) {
        log.info("Booking created: bookingId={}, trainingSlotId={}", event.bookingId(), event.trainingSlotId());
    }

    @RabbitListener(queues = "booking.cancelled.queue")
    public void handleBookingCancelled(BookingCancelledEvent event) {
        log.info("Booking cancelled: bookingId={}", event.bookingId());
    }

    @RabbitListener(queues = "booking.status.changed.queue")
    public void handleBookingStatusChanged(BookingStatusChangedEvent event) {
        log.info("Booking status changed: bookingId={}, oldStatus={}, newStatus={}",
                event.bookingId(), event.oldStatus(), event.newStatus());
    }
}
