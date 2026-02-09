package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponseDto> createBooking(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @Valid @RequestBody BookingRequestDto bookingRequestDto) {
        log.info("POST /bookings - создание бронирования для пользователя {}", userId);
        BookingResponseDto response = bookingService.createBooking(userId, bookingRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDto> updateBookingStatus(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long bookingId,
            @RequestParam Boolean approved) {
        log.info("PATCH /bookings/{} - обновление статуса на {} пользователем {}",
                bookingId, approved, userId);
        BookingResponseDto response = bookingService.updateBookingStatus(userId, bookingId, approved);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDto> getBookingById(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long bookingId) {
        log.info("GET /bookings/{} - получение бронирования пользователем {}", bookingId, userId);
        BookingResponseDto response = bookingService.getBookingById(userId, bookingId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<BookingResponseDto>> getUserBookings(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size) {
        log.info("GET /bookings?state={} - получение бронирований пользователя {}", state, userId);
        List<BookingResponseDto> response = bookingService.getUserBookings(userId, state, from, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/owner")
    public ResponseEntity<List<BookingResponseDto>> getOwnerBookings(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size) {
        log.info("GET /bookings/owner?state={} - получение бронирований владельца {}", state, userId);
        List<BookingResponseDto> response = bookingService.getOwnerBookings(userId, state, from, size);
        return ResponseEntity.ok(response);
    }
}
