package ru.practicum.shareit.booking;

public enum BookingStatus {
    WAITING("Ожидает подтверждения"),
    APPROVED("Подтверждено владельцем"),
    REJECTED("Отклонено владельцем"),
    CANCELED("Отменено пользователем");

    private final String description;

    BookingStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}