package project.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationListener {

    @RabbitListener(queues = "booking.created.queue")
    public void handleBookingCreated(BookingCreatedEvent event) {
        System.out.println("Создана новая запись: " + event +
                " id записи: " + event.bookingId() +
                " id пользователя: " + event.userId() +
                " id тренировки " + event.trainingSlotId());
    }

    @RabbitListener(queues = "booking.cancelled.queue")
    public void handleBookingCancelled(BookingCancelledEvent event) {
        System.out.println("Уведомление запись отменена: " + event +
                " id записи: " + event.bookingId() +
                " Причина :" + event.reason());
    }

    @RabbitListener(queues = "booking.status.changed.queue")
    public void handleBookingStatusChanged(BookingStatusChangedEvent event) {
        System.out.println("Уведомление стутус записи изменен " + event +
                " id записи " + event.bookingId() +
                " изменен с : " + event.oldStatus() +
                " на : " + event.newStatus()
        );
    }
}
