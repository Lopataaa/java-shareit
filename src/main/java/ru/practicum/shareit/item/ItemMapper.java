package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ItemMapper {

    private final BookingRepository bookingRepository;

    public ItemDto toDto(Item item, Long userId) {
        if (item == null) {
            return null;
        }

        BookingShortDto lastBooking = null;
        BookingShortDto nextBooking = null;

        if (userId != null && userId.equals(item.getOwnerId())) {
            LocalDateTime now = LocalDateTime.now();

            Optional<ru.practicum.shareit.booking.Booking> lastBookingOpt =
                    bookingRepository.findLastBookingForItem(item.getId(), now);

            if (lastBookingOpt.isPresent()) {
                ru.practicum.shareit.booking.Booking last = lastBookingOpt.get();
                lastBooking = new BookingShortDto(
                        last.getId(),
                        last.getBookerId(),
                        last.getStart(),
                        last.getEnd()
                );
            }

            Optional<ru.practicum.shareit.booking.Booking> nextBookingOpt =
                    bookingRepository.findNextBookingForItem(item.getId(), now);

            if (nextBookingOpt.isPresent()) {
                ru.practicum.shareit.booking.Booking next = nextBookingOpt.get();
                nextBooking = new BookingShortDto(
                        next.getId(),
                        next.getBookerId(),
                        next.getStart(),
                        next.getEnd()
                );
            }
        }

        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .ownerId(item.getOwnerId())
                .requestId(item.getRequestId())
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .build();
    }

    public ItemDto toDto(Item item) {
        return toDto(item, null);
    }

    public Item toEntity(ItemDto itemDto) {
        if (itemDto == null) {
            return null;
        }
        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .ownerId(itemDto.getOwnerId())
                .requestId(itemDto.getRequestId())
                .build();
    }
}