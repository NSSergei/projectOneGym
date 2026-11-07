package project;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import project.controller.BookingController;
import project.controller.TrainingSlotController;
import project.controller.UserController;
import project.controller.WorkoutController;
import project.dto.BookingCreateRequest;
import project.dto.BookingResponseDto;
import project.dto.TrainingResponseAllSlotsDto;
import project.dto.UserResponseDto;
import project.dto.WorkoutCreateRequest;
import project.enums.BookingStatus;
import project.enums.Role;
import project.model.Booking;
import project.model.TrainingSlot;
import project.model.User;
import project.model.Workout;
import project.service.BookingService;
import project.service.TrainingSlotService;
import project.service.UserService;
import project.service.WorkoutService;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ControllerDelegationTest {
    @Mock
    private BookingService bookingService;
    @Mock
    private TrainingSlotService trainingSlotService;
    @Mock
    private UserService userService;
    @Mock
    private WorkoutService workoutService;

    @Test
    void workoutControllerDelegatesEveryRoute() {
        WorkoutController controller = new WorkoutController(workoutService);
        Workout workout = new Workout("Yoga", "Mobility", 30);
        WorkoutCreateRequest request = new WorkoutCreateRequest("Yoga", "Mobility", 30);
        when(workoutService.createWorkout(request)).thenReturn(workout);
        when(workoutService.getAllWorkout()).thenReturn(List.of(workout));
        when(workoutService.getWorkoutById(4L)).thenReturn(workout);

        assertThat(controller.createWorkout(request).getStatusCode().value()).isEqualTo(201);
        assertThat(controller.createWorkout(request).getBody()).isSameAs(workout);
        assertThat(controller.getAllWorkout()).containsExactly(workout);
        assertThat(controller.getWorkoutById(4L)).isSameAs(workout);
        controller.updateWorkout(workout);
        controller.deleteWorkoutById(4L);

        verify(workoutService).updateWorkout(workout);
        verify(workoutService).deleteWorkoutById(4L);
    }

    @Test
    void userControllerDelegatesEveryRoute() {
        UserController controller = new UserController(userService);
        User user = User.builder().id(7L).email("user@example.com").build();
        UserResponseDto response = new UserResponseDto("A", "B", "user@example.com", Role.CLIENT);
        when(userService.getAllUsers()).thenReturn(List.of(response));
        when(userService.getUserById(7L)).thenReturn(response);
        when(userService.findUserByEmail("user@example.com")).thenReturn(response);
        when(userService.updateUser(user)).thenReturn(response);

        assertThat(controller.getUsers()).containsExactly(response);
        assertThat(controller.getUserById(7L)).isSameAs(response);
        assertThat(controller.getUserByEmail("user@example.com")).isSameAs(response);
        assertThat(controller.updateUser(user)).isSameAs(response);
        controller.deleteUserById(7L);

        verify(userService).deleteUserById(7L);
    }

    @Test
    void trainingSlotControllerDelegatesEveryRoute() {
        TrainingSlotController controller = new TrainingSlotController(trainingSlotService);
        TrainingSlot slot = TrainingSlot.builder().id(11L).build();
        TrainingResponseAllSlotsDto response = new TrainingResponseAllSlotsDto(
                11L, "Yoga", "Mobility", 30, "Coach", "One", "COACH",
                LocalTime.of(9, 0), LocalTime.of(10, 0), 12);
        when(trainingSlotService.toTrainingResponseDtoList()).thenReturn(List.of(response));
        when(trainingSlotService.createTraining(slot)).thenReturn(slot);
        when(trainingSlotService.getTrainingSlotById(11L)).thenReturn(slot);
        when(trainingSlotService.updateTrainingSlot(slot)).thenReturn(slot);

        assertThat(controller.getTraining()).containsExactly(response);
        assertThat(controller.creatTrainingSlot(slot)).isSameAs(slot);
        assertThat(controller.getTrainingById(11L)).isSameAs(slot);
        assertThat(controller.updateTraining(slot)).isSameAs(slot);
        controller.deleteTrainingById(11L);

        verify(trainingSlotService).deleteTrainingSlotById(11L);
    }

    @Test
    void bookingControllerDelegatesEveryRoute() {
        BookingController controller = new BookingController(bookingService);
        Booking booking = new Booking();
        BookingCreateRequest created = new BookingCreateRequest(20L, 7L, 11L, LocalDateTime.now());
        BookingResponseDto response = new BookingResponseDto(20L, 7L, 11L, BookingStatus.CREATED);
        when(bookingService.getAllBookings()).thenReturn(List.of(response));
        when(bookingService.registerForWorkout(11L)).thenReturn(created);
        when(bookingService.updateBooking(booking)).thenReturn(response);
        when(bookingService.changeBookingStatus(20L, BookingStatus.PENDING)).thenReturn(response);
        when(bookingService.findByBookingStatus(BookingStatus.CREATED)).thenReturn(List.of(response));

        assertThat(controller.getBookings()).containsExactly(response);
        assertThat(controller.createBooking(11L)).isSameAs(created);
        controller.deleteBookingById(20L);
        assertThat(controller.updateBooking(booking)).isSameAs(response);
        controller.cancelledBookingStatus(11L, "reason");
        assertThat(controller.changeBookingStatus(20L, BookingStatus.PENDING)).isSameAs(response);
        assertThat(controller.findByBookingStatus(BookingStatus.CREATED)).containsExactly(response);

        verify(bookingService).deleteBookingById(20L);
        verify(bookingService).cancelBookingStatus(11L, "reason");
    }
}
