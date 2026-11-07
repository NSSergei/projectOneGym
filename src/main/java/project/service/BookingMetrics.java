package project.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class BookingMetrics {
    private final Counter created;
    private final Counter cancelled;
    private final Counter rejected;

    public BookingMetrics(MeterRegistry meterRegistry) {
        this.created = Counter.builder("gym.bookings.created")
                .description("Number of successfully created bookings")
                .register(meterRegistry);
        this.cancelled = Counter.builder("gym.bookings.cancelled")
                .description("Number of successfully cancelled bookings")
                .register(meterRegistry);
        this.rejected = Counter.builder("gym.bookings.rejected")
                .description("Number of rejected booking attempts")
                .register(meterRegistry);
    }

    public void recordCreated() {
        created.increment();
    }

    public void recordCancelled() {
        cancelled.increment();
    }

    public void recordRejected() {
        rejected.increment();
    }
}
