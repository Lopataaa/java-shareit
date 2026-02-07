package ru.practicum.shareit.booking;

public enum BookingStatus {
    WAITING,    // Ожидает подтверждения
    APPROVED,   // Подтверждено владельцем
    REJECTED,   // Отклонено владельцем
    CANCELED    // Отменено пользователем
}