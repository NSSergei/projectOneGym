package project;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import project.enums.Role;
import project.enums.BookingStatus;
import project.exception.NotFoundException;
import project.exception.ValidationException;
import project.model.TrainingSlot;
import project.model.Booking;
import project.model.User;
import project.model.Workout;
import project.messaging.BookingCancelledEvent;
import project.messaging.BookingStatusChangedEvent;
import project.messaging.BookingCreatedEvent;
import project.security.user.CustomUserDetails;
import project.service.BookingService;
import project.service.BookingMetrics;
import project.storage.booking.BookingRepository;
import project.storage.trainingSlot.TrainingRepository;
import project.storage.user.UserRepository;

import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    void registrationCreatesBookingAndPublishesEvent() {
        User user = User.builder().id(7L).role(Role.CLIENT).build();
        TrainingSlot slot = TrainingSlot.builder().id(11L).capacity(2).build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new CustomUserDetails(user), null));
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(trainingRepository.findByIdForUpdate(11L)).thenReturn(Optional.of(slot));
        when(bookingRepository.findByUserIdAndTrainingSlotId(7L, 11L)).thenReturn(Optional.empty());
        when(bookingRepository.countByTrainingSlotIdAndBookingStatusIn(
                11L, List.of(BookingStatus.CREATED, BookingStatus.PENDING))).thenReturn(1L);
        when(bookingRepository.save(org.mockito.ArgumentMatchers.any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setId(29L);
            return booking;
        });

        var result = bookingService.registerForWorkout(11L);

        assertThat(result.slotId()).isEqualTo(29L);
        assertThat(result.userId()).isEqualTo(7L);
        assertThat(result.trainingId()).isEqualTo(11L);
        verify(rabbitTemplate).convertAndSend(
                org.mockito.ArgumentMatchers.eq("booking.exchange"),
                org.mockito.ArgumentMatchers.eq("booking.created"),
                org.mockito.ArgumentMatchers.any(BookingCreatedEvent.class));
        verify(bookingMetrics).recordCreated();
    }

    @Test
    void registrationRejectsDuplicateBooking() {
        User user = User.builder().id(7L).role(Role.CLIENT).build();
        TrainingSlot slot = TrainingSlot.builder().id(11L).capacity(2).build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new CustomUserDetails(user), null));
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(trainingRepository.findByIdForUpdate(11L)).thenReturn(Optional.of(slot));
        when(bookingRepository.findByUserIdAndTrainingSlotId(7L, 11L))
                .thenReturn(Optional.of(new Booking()));

        assertThatThrownBy(() -> bookingService.registerForWorkout(11L))
                .isInstanceOf(ValidationException.class)
                .hasMessage("User already has a booking for this training slot");
        verify(bookingMetrics).recordRejected();
        verify(bookingRepository, never()).save(org.mockito.ArgumentMatchers.any(Booking.class));
    }

    @Test
    void registrationRejectsNonClientRole() {
        User user = User.builder().id(7L).role(Role.ADMINISTRATOR).build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new CustomUserDetails(user), null));
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> bookingService.registerForWorkout(11L))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Role must be CLIENT");
        verify(trainingRepository, never()).findByIdForUpdate(11L);
    }

    @Test
    void cancellationUpdatesBookingPublishesEventAndIncrementsMetric() {
        User user = User.builder().id(7L).role(Role.CLIENT).build();
        Workout workout = new Workout("Yoga", "Mobility", 30);
        workout.setId(12L);
        TrainingSlot slot = TrainingSlot.builder().id(11L).workout(workout).build();
        Booking booking = booking(user, slot, BookingStatus.CREATED);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new CustomUserDetails(user), null));
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(trainingRepository.findById(11L)).thenReturn(Optional.of(slot));
        when(bookingRepository.findByUserIdAndTrainingSlotId(7L, 11L)).thenReturn(Optional.of(booking));
        ArgumentCaptor<BookingCancelledEvent> eventCaptor = ArgumentCaptor.forClass(BookingCancelledEvent.class);

        bookingService.cancelBookingStatus(11L, "schedule change");

        assertThat(booking.getBookingStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(rabbitTemplate).convertAndSend(
                org.mockito.ArgumentMatchers.eq("booking.exchange"),
                org.mockito.ArgumentMatchers.eq("booking.cancelled"),
                eventCaptor.capture());
        assertThat(eventCaptor.getValue().workoutId()).isEqualTo(12L);
        assertThat(eventCaptor.getValue().reason()).isEqualTo("schedule change");
        verify(bookingMetrics).recordCancelled();
    }

    @Test
    void cancellationRejectsMissingBooking() {
        User user = User.builder().id(7L).role(Role.CLIENT).build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new CustomUserDetails(user), null));
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(trainingRepository.findById(11L)).thenReturn(Optional.of(TrainingSlot.builder().id(11L).build()));
        when(bookingRepository.findByUserIdAndTrainingSlotId(7L, 11L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.cancelBookingStatus(11L, "reason"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Booking not found");
    }

    @Test
    void cancellationRejectsAlreadyCancelledBooking() {
        User user = User.builder().id(7L).role(Role.CLIENT).build();
        TrainingSlot slot = TrainingSlot.builder().id(11L).build();
        Booking booking = booking(user, slot, BookingStatus.CANCELLED);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new CustomUserDetails(user), null));
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(trainingRepository.findById(11L)).thenReturn(Optional.of(slot));
        when(bookingRepository.findByUserIdAndTrainingSlotId(7L, 11L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancelBookingStatus(11L, "reason"))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Booking is already cancelled");
        verify(bookingRepository, never()).save(booking);
    }

    @Test
    void changeBookingStatusPublishesCorrectOldAndNewStatuses() {
        User user = User.builder().id(7L).role(Role.CLIENT).build();
        Workout workout = new Workout("Yoga", "Mobility", 30);
        workout.setId(12L);
        TrainingSlot slot = TrainingSlot.builder().id(11L).workout(workout).build();
        Booking booking = booking(user, slot, BookingStatus.CREATED);
        when(bookingRepository.findById(29L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);
        ArgumentCaptor<BookingStatusChangedEvent> eventCaptor =
                ArgumentCaptor.forClass(BookingStatusChangedEvent.class);

        var result = bookingService.changeBookingStatus(29L, BookingStatus.PENDING);

        assertThat(result.bookingStatus()).isEqualTo(BookingStatus.PENDING);
        verify(rabbitTemplate).convertAndSend(
                org.mockito.ArgumentMatchers.eq("booking.exchange"),
                org.mockito.ArgumentMatchers.eq("booking.status.changed"),
                eventCaptor.capture());
        assertThat(eventCaptor.getValue().oldStatus()).isEqualTo(BookingStatus.CREATED);
        assertThat(eventCaptor.getValue().newStatus()).isEqualTo(BookingStatus.PENDING);
    }

    @Test
    void changeBookingStatusRejectsUnchangedStatus() {
        Booking booking = booking(User.builder().id(7L).build(), TrainingSlot.builder().id(11L).build(),
                BookingStatus.CREATED);
        when(bookingRepository.findById(29L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.changeBookingStatus(29L, BookingStatus.CREATED))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Similar status");
        verify(bookingRepository, never()).save(booking);
    }

    @Test
    void findByStatusMapsBookingResponses() {
        User user = User.builder().id(7L).build();
        TrainingSlot slot = TrainingSlot.builder().id(11L).build();
        Booking booking = booking(user, slot, BookingStatus.CREATED);
        booking.setId(29L);
        when(bookingRepository.findByBookingStatus(BookingStatus.CREATED)).thenReturn(List.of(booking));

        assertThat(bookingService.findByBookingStatus(BookingStatus.CREATED))
                .containsExactly(new project.dto.BookingResponseDto(29L, 7L, 11L, BookingStatus.CREATED));
    }

    private Booking booking(User user, TrainingSlot slot, BookingStatus status) {
        Booking booking = new Booking();
        booking.setId(29L);
        booking.setUser(user);
        booking.setTrainingSlot(slot);
        booking.setBookingStatus(status);
        return booking;
    }
}
