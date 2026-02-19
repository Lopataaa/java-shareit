package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;

@Component
@RequiredArgsConstructor
public class BookingMapper {

    private final UserMapper userMapper;
    private final ItemMapper itemMapper;

    public Booking toEntity(BookingRequestDto dto, Long bookerId) {
        if (dto == null) {
            throw new IllegalArgumentException("BookingRequestDto cannot be null");
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
            throw new IllegalArgumentException("Booking cannot be null");
        }
        if (booker == null) {
            throw new IllegalArgumentException("Booker cannot be null");
        }
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }

        return BookingResponseDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .booker(userMapper.toBookerDto(booker))
                .item(itemMapper.toBookingItemDto(item))
                .build();
    }
}