package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
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
        log.info("Создание вещи для пользователя id: {}", userId);

        validateUserExists(userId);
        validateItemDto(itemDto);

        Item item = createItemEntity(itemDto, userId);
        Item savedItem = itemRepository.save(item);

        log.info("Вещь создана с id: {}", savedItem.getId());
        return itemMapper.toDto(savedItem, userId);
    }

    private Item createItemEntity(ItemDto itemDto, Long ownerId) {
        Item item = itemMapper.toEntity(itemDto);
        item.setOwnerId(ownerId);
        return item;
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        log.info("Обновление вещи id: {} пользователем id: {}", itemId, userId);

        Item existingItem = getItemOrThrow(itemId);
        validateItemOwnership(userId, existingItem);

        updateItemFields(existingItem, itemDto);
        Item updatedItem = itemRepository.save(existingItem);

        log.info("Вещь id: {} успешно обновлена", itemId);
        return itemMapper.toDto(updatedItem, userId);
    }

    private void updateItemFields(Item item, ItemDto itemDto) {
        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            log.debug("Обновление названия вещи id: {}", item.getId());
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            log.debug("Обновление описания вещи id: {}", item.getId());
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            log.debug("Обновление статуса доступности вещи id: {}", item.getId());
            item.setAvailable(itemDto.getAvailable());
        }
    }

    @Override
    public ItemDto getItemById(Long itemId, Long userId) {
        log.info("Получение вещи id: {} пользователем id: {}", itemId, userId);

        Item item = getItemOrThrow(itemId);
        ItemDto itemDto = itemMapper.toDto(item, userId);

        log.debug("Вещь id: {} успешно получена", itemId);
        return itemDto;
    }

    @Override
    public List<ItemDto> getAllItemsByOwner(Long ownerId, int from, int size) {
        log.info("Получение всех вещей владельца id: {} (from={}, size={})", ownerId, from, size);

        validatePagination(from, size);
        Pageable pageable = createPageRequest(from, size);

        List<Item> items = itemRepository.findByOwnerId(ownerId, pageable);
        log.debug("Найдено {} вещей для владельца id: {}", items.size(), ownerId);

        return items.stream().map(item -> itemMapper.toDto(item, ownerId)).collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text, int from, int size) {
        log.info("Поиск вещей по тексту: '{}' (from={}, size={})", text, from, size);

        if (isSearchTextEmpty(text)) {
            log.debug("Пустой поисковый запрос - возвращаем пустой список");
            return List.of();
        }

        validatePagination(from, size);
        Pageable pageable = createPageRequest(from, size);

        List<Item> items = itemRepository.searchItems(text, pageable);
        log.debug("Найдено {} вещей по запросу: '{}'", items.size(), text);

        return items.stream().map(item -> itemMapper.toDto(item, null)).collect(Collectors.toList());
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

    private void validateCommentData(CommentDto commentDto) {
        if (commentDto.getText() == null || commentDto.getText().isBlank()) {
            throw new ValidationException("Текст комментария не может быть пустым");
        }
    }

    private void validateUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            log.warn("Попытка доступа к несуществующему пользователю id: {}", userId);
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
    }

    private void validateItemOwnership(Long userId, Item item) {
        if (!item.getOwnerId().equals(userId)) {
            log.warn("Попытка обновления чужой вещи. Вещь id: {}, пользователь id: {}", item.getId(), userId);
            throw new NotFoundException("Нельзя обновить чужую вещь");
        }
    }

    private void validateUserCanComment(Long userId, Long itemId) {
        LocalDateTime now = LocalDateTime.now();
        boolean hasUserBookedItem = bookingRepository.hasUserBookedItem(itemId, userId, now);

        if (!hasUserBookedItem) {
            log.warn("Попытка оставить комментарий без бронирования. Вещь id: {}, пользователь id: {}", itemId, userId);
            throw new ValidationException("Пользователь не брал эту вещь в аренду");
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

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> {
            log.error("Пользователь не найден с id: {}", userId);
            return new NotFoundException("Пользователь не найден");
        });
    }

    private Item getItemOrThrow(Long itemId) {
        return itemRepository.findById(itemId).orElseThrow(() -> {
            log.error("Вещь не найдена с id: {}", itemId);
            return new NotFoundException("Item not found");
        });
    }

    private Comment createAndSaveComment(CommentDto commentDto, User user, Item item) {
        Comment comment = commentMapper.toEntity(commentDto, item.getId(), user.getId());
        comment.setCreated(LocalDateTime.now());
        return commentRepository.save(comment);
    }

    private boolean isSearchTextEmpty(String text) {
        return text == null || text.isBlank();
    }

    private Pageable createPageRequest(int from, int size) {
        return PageRequest.of(from / size, size);
    }
}