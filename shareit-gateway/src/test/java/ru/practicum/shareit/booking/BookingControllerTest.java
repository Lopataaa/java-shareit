package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
@DisplayName("Тесты контроллера бронирований")
class BookingControllerTest {

    private static final Long USER_ID = 1L;
    private static final Long BOOKING_ID = 1L;
    private static final Long ITEM_ID = 1L;
    private static final int FROM_DEFAULT = 0;
    private static final int SIZE_DEFAULT = 10;
    private static final int FROM_NEGATIVE = -1;
    private static final int SIZE_ZERO = 0;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingClient bookingClient;

    private BookItemRequestDto validBookingDto;
    private BookItemRequestDto invalidBookingDto;
    private ResponseEntity<Object> responseEntityOk;

    @BeforeEach
    void setUp() {
        // given - подготовка тестовых данных
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(3);

        validBookingDto = BookItemRequestDto.builder()
                .itemId(ITEM_ID)
                .start(start)
                .end(end)
                .build();

        invalidBookingDto = BookItemRequestDto.builder()
                .itemId(null)
                .start(null)
                .end(null)
                .build();

        responseEntityOk = ResponseEntity.ok().build();
    }

    @Test
    @DisplayName("POST /bookings: успешное создание бронирования должно возвращать 200 OK")
    void createBooking_shouldReturnOk() throws Exception {
        // given
        when(bookingClient.bookItem(eq(USER_ID), any(BookItemRequestDto.class)))
                .thenReturn(responseEntityOk);

        // when + then
        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validBookingDto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /bookings: создание с невалидными данными должно возвращать 400 Bad Request")
    void createBooking_shouldReturnBadRequestWhenInvalid() throws Exception {
        // when + then
        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidBookingDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /bookings/{bookingId}: обновление статуса должно возвращать 200 OK")
    void updateBookingStatus_shouldReturnOk() throws Exception {
        // given
        when(bookingClient.updateBookingStatus(eq(USER_ID), eq(BOOKING_ID), eq(true)))
                .thenReturn(responseEntityOk);

        // when + then
        mockMvc.perform(patch("/bookings/{bookingId}", BOOKING_ID)
                        .header("X-Sharer-User-Id", USER_ID)
                        .param("approved", "true"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /bookings/{bookingId}: получение бронирования должно возвращать 200 OK")
    void getBookingById_shouldReturnOk() throws Exception {
        // given
        when(bookingClient.getBooking(USER_ID, BOOKING_ID)).thenReturn(responseEntityOk);

        // when + then
        mockMvc.perform(get("/bookings/{bookingId}", BOOKING_ID)
                        .header("X-Sharer-User-Id", USER_ID))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /bookings: получение бронирований пользователя должно возвращать 200 OK")
    void getUserBookings_shouldReturnOk() throws Exception {
        // given
        when(bookingClient.getBookings(eq(USER_ID), any(BookingState.class), anyInt(), anyInt()))
                .thenReturn(responseEntityOk);

        // when + then
        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", USER_ID)
                        .param("state", "ALL")
                        .param("from", String.valueOf(FROM_DEFAULT))
                        .param("size", String.valueOf(SIZE_DEFAULT)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /bookings: отрицательное значение from должно возвращать 400 Bad Request")
    void getUserBookings_shouldReturnBadRequestWhenFromNegative() throws Exception {
        // when + then
        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", USER_ID)
                        .param("state", "ALL")
                        .param("from", String.valueOf(FROM_NEGATIVE))
                        .param("size", String.valueOf(SIZE_DEFAULT)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /bookings/owner: получение бронирований владельца должно возвращать 200 OK")
    void getOwnerBookings_shouldReturnOk() throws Exception {
        // given
        when(bookingClient.getOwnerBookings(eq(USER_ID), any(BookingState.class), anyInt(), anyInt()))
                .thenReturn(responseEntityOk);

        // when + then
        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", USER_ID)
                        .param("state", "ALL")
                        .param("from", String.valueOf(FROM_DEFAULT))
                        .param("size", String.valueOf(SIZE_DEFAULT)))
                .andExpect(status().isOk());
    }
}