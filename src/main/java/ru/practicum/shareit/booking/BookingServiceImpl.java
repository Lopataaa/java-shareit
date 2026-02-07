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
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Item item = itemRepository.findById(bookingRequestDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Item not found"));

        // Проверка, что пользователь не владелец вещи
        if (item.getOwnerId().equals(userId)) {
            throw new AccessDeniedException("Owner cannot book their own item");
        }

        // Проверка доступности вещи
        if (!item.getAvailable()) {
            throw new ItemNotAvailableException("Item is not available for booking");
        }

        // Проверка дат
        if (bookingRequestDto.getEnd().isBefore(bookingRequestDto.getStart()) ||
                bookingRequestDto.getEnd().equals(bookingRequestDto.getStart())) {
            throw new ValidationException("End date must be after start date");
        }

        Booking booking = bookingMapper.toEntity(bookingRequestDto, userId);

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking created with id: {}", savedBooking.getId());

        return bookingMapper.toDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingResponseDto updateBookingStatus(Long userId, Long bookingId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        // Получаем вещь для проверки владельца
        Item item = itemRepository.findById(booking.getItemId())
                .orElseThrow(() -> new NotFoundException("Item not found"));

        // Проверка, что пользователь - владелец вещи
        if (!item.getOwnerId().equals(userId)) {
            throw new AccessDeniedException("Only item owner can change booking status");
        }

        // Проверка, что бронирование еще не подтверждено/отклонено
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Booking status already changed");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking updatedBooking = bookingRepository.save(booking);
        log.info("Booking id: {} status updated to: {}", bookingId, updatedBooking.getStatus());

        return bookingMapper.toDto(updatedBooking);
    }

    @Override
    public BookingResponseDto getBookingById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        // Получаем вещь и пользователя для проверки прав
        Item item = itemRepository.findById(booking.getItemId())
                .orElseThrow(() -> new NotFoundException("Item not found"));

        User booker = userRepository.findById(booking.getBookerId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Проверка прав доступа
        if (!booking.getBookerId().equals(userId) &&
                !item.getOwnerId().equals(userId)) {
            throw new AccessDeniedException("Access denied");
        }

        return bookingMapper.toDto(booking);
    }

    @Override
    public List<BookingResponseDto> getUserBookings(Long userId, String state, int from, int size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "start"));
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings;
        switch (state.toUpperCase()) {
            case "ALL":
                bookings = bookingRepository.findByBookerId(userId, pageable);
                break;
            case "CURRENT":
                bookings = bookingRepository.findByBookerIdAndStartBeforeAndEndAfter(
                        userId, now, pageable);
                break;
            case "PAST":
                bookings = bookingRepository.findByBookerIdAndEndBefore(userId, now, pageable);
                break;
            case "FUTURE":
                bookings = bookingRepository.findByBookerIdAndStartAfter(userId, now, pageable);
                break;
            case "WAITING":
                bookings = bookingRepository.findByBookerIdAndStatus(
                        userId, BookingStatus.WAITING, pageable);
                break;
            case "REJECTED":
                bookings = bookingRepository.findByBookerIdAndStatus(
                        userId, BookingStatus.REJECTED, pageable);
                break;
            default:
                throw new ValidationException("Unknown state: " + state);
        }

        return bookings.stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getOwnerBookings(Long userId, String state, int from, int size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Проверка, что у пользователя есть вещи
        if (itemRepository.findByOwnerId(userId, Pageable.unpaged()).isEmpty()) {
            throw new AccessDeniedException("User has no items");
        }

        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "start"));
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings;

        switch (state.toUpperCase()) {
            case "ALL" -> bookings = bookingRepository.findByItemOwnerId(userId, pageable);
            case "CURRENT" -> bookings = bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfter(
                    userId, now, pageable);
            case "PAST" -> bookings = bookingRepository.findByItemOwnerIdAndEndBefore(userId, now, pageable);
            case "FUTURE" -> bookings = bookingRepository.findByItemOwnerIdAndStartAfter(userId, now, pageable);
            case "WAITING" -> bookings = bookingRepository.findByItemOwnerIdAndStatus(
                    userId, BookingStatus.WAITING, pageable);
            case "REJECTED" -> bookings = bookingRepository.findByItemOwnerIdAndStatus(
                    userId, BookingStatus.REJECTED, pageable);
            default -> throw new ValidationException("Unknown state: " + state);
        }

        return bookings.stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }
}