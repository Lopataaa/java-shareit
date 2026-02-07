package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;

import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemMapper itemMapper;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }

        validateItemDto(itemDto);

        Item item = itemMapper.toEntity(itemDto);
        item.setOwnerId(userId);
        Item savedItem = itemRepository.save(item);

        return itemMapper.toDto(savedItem, userId);
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с ID " + itemId + " не найдена"));

        if (!existingItem.getOwnerId().equals(userId)) {
            throw new NotFoundException("Нельзя обновить чужую вещь");
        }

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            existingItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }

        Item updatedItem = itemRepository.save(existingItem);
        return itemMapper.toDto(updatedItem, userId);
    }

    @Override
    public ItemDto getItemById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));
        return itemMapper.toDto(item, userId);
    }

    @Override
    public List<ItemDto> getAllItemsByOwner(Long ownerId, int from, int size) {
        if (from < 0 || size <= 0) {
            throw new ValidationException("Некорректные параметры пагинации");
        }

        Pageable pageable = PageRequest.of(from / size, size);
        List<Item> items = itemRepository.findByOwnerId(ownerId, pageable);

        return items.stream()
                .map(item -> itemMapper.toDto(item, ownerId))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text, int from, int size) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        if (from < 0 || size <= 0) {
            throw new ValidationException("Некорректные параметры пагинации");
        }

        Pageable pageable = PageRequest.of(from / size, size);
        List<Item> items = itemRepository.searchItems(text, pageable);

        return items.stream()
                .map(item -> itemMapper.toDto(item, null))
                .collect(Collectors.toList());
    }

    private void validateItemDto(ItemDto itemDto) {
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new ValidationException("Название вещи не может быть пустым");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new ValidationException("Описание вещи не может быть пустым");
        }
        if (itemDto.getAvailable() == null) {
            throw new ValidationException("Статус доступности не может быть null");
        }
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        // Проверяем существование пользователя и вещи
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        // Проверяем, что пользователь брал эту вещь в аренду
        LocalDateTime now = LocalDateTime.now();
        boolean hasUserBookedItem = bookingRepository.hasUserBookedItem(itemId, userId, now);

        if (!hasUserBookedItem) {
            throw new ValidationException("Пользователь не брал эту вещь в аренду");
        }

        // Создаем и сохраняем комментарий
        Comment comment = commentMapper.toEntity(commentDto, itemId, userId);
        Comment savedComment = commentRepository.save(comment);

        return commentMapper.toDto(savedComment);
    }
}