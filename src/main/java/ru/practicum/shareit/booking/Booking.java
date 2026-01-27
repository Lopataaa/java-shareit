package ru.practicum.shareit.booking;

import lombok.Data;

import java.time.LocalDateTime;

// Добрый день! Постаралась учесть в коде все комментарии

@Data
public class Booking {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private Long itemId;
    private Long bookerId;
    private BookingStatus status;
}
