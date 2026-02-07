package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

@Component
@RequiredArgsConstructor
public class BookingMapper {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final UserMapper userMapper;
    private final ItemMapper itemMapper;

    public Booking toEntity(BookingRequestDto dto, Long bookerId) {
        if (dto == null) {
            return null;
        }

        return Booking.builder()
                .start(dto.getStart())
                .end(dto.getEnd())
                .itemId(dto.getItemId())
                .bookerId(bookerId)
                .status(BookingStatus.WAITING)
                .build();
    }

    public BookingResponseDto toDto(Booking booking) {
        if (booking == null) {
            return null;
        }

        // Получаем пользователя и вещь по ID
        User booker = userRepository.findById(booking.getBookerId())
                .orElseThrow(() -> new NotFoundException("User not found with id: " + booking.getBookerId()));

        Item item = itemRepository.findById(booking.getItemId())
                .orElseThrow(() -> new NotFoundException("Item not found with id: " + booking.getItemId()));

        return new BookingResponseDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getStatus(),
                userMapper.toDto(booker),
                itemMapper.toDto(item)
        );
    }
}
