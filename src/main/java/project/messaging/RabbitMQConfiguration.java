package project.messaging;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfiguration {

    @Bean
    public TopicExchange bookingExchange() {
        return new TopicExchange("booking.exchange");
    }

    @Bean
    public Queue bookingCreatedQueue() {
        return QueueBuilder
                .durable("booking.created.queue")
                .deadLetterExchange("booking.dlx")
                .deadLetterRoutingKey("booking.created")
                .build();
    }

    @Bean
    public Queue bookingCancelledQueue() {
        return new Queue("booking.cancelled.queue");
    }

    @Bean
    public Queue bookingStatusChangedQueue() {
        return new Queue("booking.status.changed.queue");
    }

    @Bean
    public DirectExchange bookingDlx() {
        return new DirectExchange("booking.dlx");
    }

    @Bean
    public Queue bookingCreatedDlq() {
        return new Queue("booking.created.dlq");
    }

    @Bean
    public Binding bookingCreatedBinding(Queue bookingCreatedQueue,
                                        TopicExchange bookingExchange) {
        return BindingBuilder
                .bind(bookingCreatedQueue)
                .to(bookingExchange)
                .with("booking.created");
    }

    @Bean
    public Binding bookingCreatedDlqBinding(DirectExchange bookingDlx,
                                            Queue bookingCreatedDlq) {
        return BindingBuilder
                .bind(bookingCreatedDlq)
                .to(bookingDlx)
                .with("booking.created");

    }

    @Bean
    public Binding bookingCancelled(Queue bookingCancelledQueue,
                                    TopicExchange bookingExchange) {
        return BindingBuilder
                .bind(bookingCancelledQueue)
                .to(bookingExchange)
                .with("booking.cancelled");
    }

    @Bean
    public Binding bookingStatusChangedBinding(
            Queue bookingStatusChangedQueue,
            TopicExchange bookingExchange
    ) {
        return BindingBuilder
                .bind(bookingStatusChangedQueue)
                .to(bookingExchange)
                .with("booking.status.changed");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory,
                                         MessageConverter messageConverter) {

        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);

        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();

        factory.setConnectionFactory(connectionFactory);

        factory.setAdviceChain(
                RetryInterceptorBuilder.stateless()
                        .maxAttempts(3)
                        .backOffOptions(1000, 2.0, 10000)
                        .recoverer(new RejectAndDontRequeueRecoverer())
                        .build()
        );
        return factory;
    }
}
