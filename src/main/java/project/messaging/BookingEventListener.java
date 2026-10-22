package project.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class BookingEventListener {

    @RabbitListener(queues = "booking.created.queue")
    public void handleBookingCreated(BookingCreatedEvent event) {
        System.out.println("Получения события" + event);
    }

    @RabbitListener(queues = "booking.cancelled.queue")
    public void handleBookingCancelled(BookingCancelledEvent event) {
        System.out.println("Получено событие отмены: " + event);
    }

    @RabbitListener(queues = "booking.status.changed.queue")
    public void handleBookingStatusChanged(BookingStatusChangedEvent event) {
        System.out.println("Получено событие изменения статуса: " + event);
    }
}
