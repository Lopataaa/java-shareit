package ru.practicum.shareit.booking.dto;

public enum BookingState {
    ALL("Все бронирования"),
    CURRENT("Текущие бронирования"),
    PAST("Завершенные бронирования"),
    FUTURE("Будущие бронирования"),
    WAITING("Ожидающие подтверждения"),
    REJECTED("Отклоненные бронирования");

    private final String description;

    BookingState(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static BookingState from(String state) {
        try {
            return BookingState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown state: " + state);
        }
    }

    @Override
    public String toString() {
        return name() + " (" + description + ")";
    }
}