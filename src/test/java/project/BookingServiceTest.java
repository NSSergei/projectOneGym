package project;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import project.enums.Role;
import project.enums.BookingStatus;
import project.exception.ValidationException;
import project.model.TrainingSlot;
import project.model.User;
import project.messaging.BookingCreatedEvent;
import project.security.user.CustomUserDetails;
import project.service.BookingService;
import project.service.BookingMetrics;
import project.storage.booking.BookingRepository;
import project.storage.trainingSlot.TrainingRepository;
import project.storage.user.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TrainingRepository trainingRepository;
    @Mock
    private RabbitTemplate rabbitTemplate;
    @Mock
    private BookingMetrics bookingMetrics;
    @InjectMocks
    private BookingService bookingService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void registrationRejectsAFullSlotBeforePersisting() {
        User user = User.builder().id(7L).role(Role.CLIENT).build();
        TrainingSlot slot = TrainingSlot.builder().id(11L).capacity(2).build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new CustomUserDetails(user), null));
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(trainingRepository.findByIdForUpdate(11L)).thenReturn(Optional.of(slot));
        when(bookingRepository.findByUserIdAndTrainingSlotId(7L, 11L)).thenReturn(Optional.empty());
        when(bookingRepository.countByTrainingSlotIdAndBookingStatusIn(
                11L, java.util.List.of(BookingStatus.CREATED, BookingStatus.PENDING))).thenReturn(2L);

        assertThatThrownBy(() -> bookingService.registerForWorkout(11L))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Training slot is full");

        verify(bookingRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(rabbitTemplate, never()).convertAndSend(
                org.mockito.ArgumentMatchers.eq("booking.exchange"),
                org.mockito.ArgumentMatchers.eq("booking.created"),
                org.mockito.ArgumentMatchers.any(BookingCreatedEvent.class));
    }
}
