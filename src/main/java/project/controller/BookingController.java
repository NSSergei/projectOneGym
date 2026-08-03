    package project.controller;

    import lombok.RequiredArgsConstructor;
    import org.springframework.security.core.Authentication;
    import org.springframework.web.bind.annotation.*;
    import project.dto.BookingCreateRequest;
    import project.dto.BookingResponseDto;
    import project.enums.BookingStatus;
    import project.model.Booking;
    import project.service.BookingService;

    import java.util.List;

    @RestController
    @RequiredArgsConstructor
    @RequestMapping("/api/booking")

    public class BookingController {
        private final BookingService bookingService;

        @GetMapping
        public List<BookingResponseDto> getBookings() {
            return bookingService.getAllBookings();
        }

        @PostMapping("/registration/{workoutId}")
        public BookingCreateRequest createBooking(@PathVariable long workoutId,
                                                  Authentication authentication) {
            System.out.println("AUTH = " + authentication);
            System.out.println("PRINCIPAL = " + authentication.getPrincipal());
            return bookingService.registerForWorkout(workoutId);
        }

        @DeleteMapping("/{id}")
        public void deleteBookingById(@PathVariable long id) {
            bookingService.deleteBookingById(id);
        }

        @PutMapping
        public BookingResponseDto updateBooking(@RequestBody Booking booking) {
            return bookingService.updateBooking(booking);
        }

        @PatchMapping("/{id}/status")
        public BookingResponseDto changeBookingStatus(@PathVariable long id,
                                                      @RequestBody BookingStatus bookingStatus) {
            return bookingService.changeBookingStatus(id, bookingStatus);
        }

        @GetMapping("/status")
        public List<BookingResponseDto> findByBookingStatus(@RequestParam BookingStatus bookingStatus) {
            return bookingService.findByBookingStatus(bookingStatus);
        }
    }
