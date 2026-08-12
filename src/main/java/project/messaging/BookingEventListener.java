package project.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class BookingEventListener {

    @RabbitListener(queues = "bookingQueue")
    public void handleBookingCreated(BookingCreatedEvent event) {
        System.out.println("Получения события" + event);
    }
}
