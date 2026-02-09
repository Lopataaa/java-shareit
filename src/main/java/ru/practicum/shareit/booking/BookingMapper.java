package ru.practicum.shareit.booking;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

@Component
public class BookingMapper {

    public Booking toEntity(BookingRequestDto dto, Long bookerId) {
        if (dto == null) {
            throw new IllegalArgumentException("BookingRequestDto не может быть null");
        }

        return Booking.builder()
                .start(dto.getStart())
                .end(dto.getEnd())
                .itemId(dto.getItemId())
                .bookerId(bookerId)
                .status(BookingStatus.WAITING)
                .build();
    }

    public BookingResponseDto toDto(Booking booking, User booker, Item item) {
        if (booking == null) {
            throw new IllegalArgumentException("Booking не может быть null");
        }
        if (booker == null) {
            throw new IllegalArgumentException("Booker не может быть null для бронирования id: " + booking.getId());
        }
        if (item == null) {
            throw new IllegalArgumentException("Item не может быть null для бронирования id: " + booking.getId());
        }

        return BookingResponseDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .booker(new BookingResponseDto.BookerDto(booker.getId(), booker.getName()))
                .item(new BookingResponseDto.ItemDto(item.getId(), item.getName()))
                .build();
    }
}
