package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional
    public BookingResponseDto createBooking(Long userId, BookingRequestDto bookingRequestDto) {
        log.info("Создание бронирования для пользователя с id: {}", userId);
        validateDates(bookingRequestDto);

        User booker = getUserOrThrow(userId);
        Item item = getItemOrThrow(bookingRequestDto.getItemId());

        validateBookingRules(userId, item);

        Booking booking = bookingMapper.toEntity(bookingRequestDto, userId);
        Booking savedBooking = bookingRepository.save(booking);

        log.info("Бронирование создано с id: {}", savedBooking.getId());

        return bookingMapper.toDto(savedBooking, booker, item);
    }

    private void validateDates(BookingRequestDto bookingRequestDto) {
        LocalDateTime start = bookingRequestDto.getStart();
        LocalDateTime end = bookingRequestDto.getEnd();

        if (start == null || end == null) {
            throw new ValidationException("Дата начала и окончания обязательны");
        }

        if (start.isBefore(LocalDateTime.now())) {
            throw new ValidationException("Дата начала не может быть в прошлом");
        }

        if (end.isBefore(start) || end.equals(start)) {
            throw new ValidationException("Дата окончания должна быть после даты начала");
        }
    }

    private void validateBookingRules(Long userId, Item item) {
        if (item.getOwnerId().equals(userId)) {
            throw new AccessDeniedException("Владелец не может бронировать свою вещь");
        }

        if (!item.getAvailable()) {
            throw new ItemNotAvailableException("Вещь недоступна для бронирования");
        }
    }

    @Override
    @Transactional
    public BookingResponseDto updateBookingStatus(Long userId, Long bookingId, Boolean approved) {
        log.info("Обновление статуса бронирования id: {} пользователем id: {}", bookingId, userId);

        Booking booking = getBookingOrThrow(bookingId);
        Item item = getItemOrThrow(booking.getItemId());

        validateStatusUpdate(userId, booking, item);

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        bookingRepository.save(booking);

        log.info("Статус бронирования id: {} обновлен на: {}", bookingId, booking.getStatus());

        User booker = getUserOrThrow(booking.getBookerId());
        return bookingMapper.toDto(booking, booker, item);
    }

    private void validateStatusUpdate(Long userId, Booking booking, Item item) {
        if (!item.getOwnerId().equals(userId)) {
            throw new AccessDeniedException("Только владелец вещи может изменять статус бронирования");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Статус бронирования уже был изменен");
        }
    }

    @Override
    public BookingResponseDto getBookingById(Long userId, Long bookingId) {
        log.info("Получение информации о бронировании id: {} пользователем id: {}", bookingId, userId);

        Booking booking = getBookingOrThrow(bookingId);
        Item item = getItemOrThrow(booking.getItemId());
        User booker = getUserOrThrow(booking.getBookerId());

        validateAccess(userId, booking, item);

        return bookingMapper.toDto(booking, booker, item);
    }

    private void validateAccess(Long userId, Booking booking, Item item) {
        if (!booking.getBookerId().equals(userId) && !item.getOwnerId().equals(userId)) {
            throw new AccessDeniedException("Доступ к бронированию запрещен");
        }
    }

    @Override
    public List<BookingResponseDto> getUserBookings(Long userId, String state, int from, int size) {
        log.info("Получение бронирований пользователя id: {} со статусом: {}", userId, state);
        validateUserExists(userId);
        validatePagination(from, size);

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("start").descending());
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = getBookingsForUser(userId, state, now, pageable);
        return convertBookingsToDto(bookings);
    }

    @Override
    public List<BookingResponseDto> getOwnerBookings(Long userId, String state, int from, int size) {
        log.info("Получение бронирований владельца id: {} со статусом: {}", userId, state);
        validateUserExists(userId);
        validatePagination(from, size);

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("start").descending());
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = getBookingsForOwner(userId, state, now, pageable);
        return convertBookingsToDto(bookings);
    }

    private List<BookingResponseDto> convertBookingsToDto(List<Booking> bookings) {
        if (bookings.isEmpty()) {
            return List.of();
        }

        List<Long> userIds = bookings.stream()
                .map(Booking::getBookerId)
                .distinct()
                .collect(Collectors.toList());

        List<Long> itemIds = bookings.stream()
                .map(Booking::getItemId)
                .distinct()
                .collect(Collectors.toList());

        // Загрузка пользователей и вещей
        Map<Long, User> users = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        Map<Long, Item> items = itemRepository.findAllById(itemIds).stream()
                .collect(Collectors.toMap(Item::getId, Function.identity()));

        return bookings.stream()
                .map(booking -> {
                    User booker = users.get(booking.getBookerId());
                    Item item = items.get(booking.getItemId());

                    if (booker == null) {
                        log.error("Booker не найден для бронирования id: {}", booking.getId());
                        throw new NotFoundException("Пользователь не найден с id: " + booking.getBookerId());
                    }
                    if (item == null) {
                        log.error("Вещь не найдена для бронирования id: {}", booking.getId());
                        throw new NotFoundException("Вещь не найдена с id: " + booking.getItemId());
                    }

                    return bookingMapper.toDto(booking, booker, item);
                })
                .collect(Collectors.toList());
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден с id: " + userId));
    }

    private Item getItemOrThrow(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена с id: " + itemId));
    }

    private Booking getBookingOrThrow(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено с id: " + bookingId));
    }

    private void validateUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден с id: " + userId);
        }
    }

    private void validatePagination(int from, int size) {
        if (from < 0) {
            throw new ValidationException("Параметр 'from' должен быть неотрицательным");
        }
        if (size <= 0) {
            throw new ValidationException("Параметр 'size' должен быть положительным");
        }
    }

    private List<Booking> getBookingsForUser(Long userId, String state, LocalDateTime now, Pageable pageable) {
        switch (state.toUpperCase()) {
            case "ALL":
                return bookingRepository.findByBookerId(userId, pageable);
            case "CURRENT":
                return bookingRepository.findByBookerIdAndStartBeforeAndEndAfter(userId, now, pageable);
            case "PAST":
                return bookingRepository.findByBookerIdAndEndBefore(userId, now, pageable);
            case "FUTURE":
                return bookingRepository.findByBookerIdAndStartAfter(userId, now, pageable);
            case "WAITING":
                return bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.WAITING, pageable);
            case "REJECTED":
                return bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.REJECTED, pageable);
            default:
                throw new ValidationException("Неизвестный статус: " + state);
        }
    }

    private List<Booking> getBookingsForOwner(Long userId, String state, LocalDateTime now, Pageable pageable) {
        switch (state.toUpperCase()) {
            case "ALL":
                return bookingRepository.findByItemOwnerId(userId, pageable);
            case "CURRENT":
                return bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfter(userId, now, pageable);
            case "PAST":
                return bookingRepository.findByItemOwnerIdAndEndBefore(userId, now, pageable);
            case "FUTURE":
                return bookingRepository.findByItemOwnerIdAndStartAfter(userId, now, pageable);
            case "WAITING":
                return bookingRepository.findByItemOwnerIdAndStatus(userId, BookingStatus.WAITING, pageable);
            case "REJECTED":
                return bookingRepository.findByItemOwnerIdAndStatus(userId, BookingStatus.REJECTED, pageable);
            default:
                throw new ValidationException("Неизвестный статус: " + state);
        }
    }
}