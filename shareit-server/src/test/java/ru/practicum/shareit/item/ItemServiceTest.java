package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты сервиса вещей")
class ItemServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;
    private static final Long ITEM_ID = 1L;
    private static final Long REQUEST_ID = 5L;
    private static final String ITEM_NAME = "Hammer";
    private static final String ITEM_DESCRIPTION = "Heavy hammer";
    private static final String UPDATED_NAME = "Updated Hammer";
    private static final String UPDATED_DESCRIPTION = "Updated description";
    private static final String EMPTY_STRING = "";
    private static final String SEARCH_TEXT = "hammer";
    private static final int FROM_DEFAULT = 0;
    private static final int SIZE_DEFAULT = 10;
    private static final int FROM_NEGATIVE = -1;
    private static final int SIZE_ZERO = 0;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User owner;
    private Item item;
    private ItemDto itemDto;
    private ItemDto itemDtoWithRequest;
    private ItemDto updateDto;
    private Booking lastBooking;
    private Booking nextBooking;

    @BeforeEach
    void setUp() {
        // given - подготовка тестовых данных
        owner = User.builder()
                .id(USER_ID)
                .name("Owner")
                .email("owner@test.com")
                .build();

        item = Item.builder()
                .id(ITEM_ID)
                .name(ITEM_NAME)
                .description(ITEM_DESCRIPTION)
                .available(true)
                .ownerId(USER_ID)
                .requestId(null)
                .build();

        itemDto = ItemDto.builder()
                .id(ITEM_ID)
                .name(ITEM_NAME)
                .description(ITEM_DESCRIPTION)
                .available(true)
                .ownerId(USER_ID)
                .requestId(null)
                .build();

        itemDtoWithRequest = ItemDto.builder()
                .id(ITEM_ID)
                .name(ITEM_NAME)
                .description(ITEM_DESCRIPTION)
                .available(true)
                .ownerId(USER_ID)
                .requestId(REQUEST_ID)
                .build();

        updateDto = ItemDto.builder()
                .name(UPDATED_NAME)
                .description(UPDATED_DESCRIPTION)
                .available(false)
                .build();

        lastBooking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().minusDays(5))
                .end(LocalDateTime.now().minusDays(1))
                .itemId(ITEM_ID)
                .bookerId(OTHER_USER_ID)
                .status(BookingStatus.APPROVED)
                .build();

        nextBooking = Booking.builder()
                .id(2L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(5))
                .itemId(ITEM_ID)
                .bookerId(3L)
                .status(BookingStatus.APPROVED)
                .build();
    }

    @Test
    @DisplayName("Создание вещи: успешное создание без requestId")
    void createItem_shouldCreateItemSuccessfully() {
        // given
        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(itemMapper.toEntity(itemDto)).thenReturn(item);
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        when(itemMapper.toDto(item, USER_ID)).thenReturn(itemDto);

        // when
        ItemDto result = itemService.createItem(USER_ID, itemDto);

        // then
        assertNotNull(result);
        assertEquals(ITEM_ID, result.getId());
        assertEquals(ITEM_NAME, result.getName());
        assertNull(result.getRequestId());

        verify(userRepository).existsById(USER_ID);
        verify(itemRepository).save(any(Item.class));
        verify(itemRequestRepository, never()).existsById(anyLong());
    }

    @Test
    @DisplayName("Создание вещи: успешное создание с requestId")
    void createItem_shouldCreateItemWithRequestId() {
        // given
        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(itemMapper.toEntity(itemDtoWithRequest)).thenReturn(item);
        when(itemRequestRepository.existsById(REQUEST_ID)).thenReturn(true);
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        when(itemMapper.toDto(item, USER_ID)).thenReturn(itemDtoWithRequest);

        // when
        ItemDto result = itemService.createItem(USER_ID, itemDtoWithRequest);

        // then
        assertNotNull(result);
        assertEquals(ITEM_ID, result.getId());
        assertEquals(REQUEST_ID, result.getRequestId());

        verify(userRepository).existsById(USER_ID);
        verify(itemRequestRepository).existsById(REQUEST_ID);
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    @DisplayName("Создание вещи: несуществующий requestId должен вызывать NotFoundException")
    void createItem_shouldThrowExceptionWhenRequestNotFound() {
        // given
        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(itemMapper.toEntity(itemDtoWithRequest)).thenReturn(item);
        when(itemRequestRepository.existsById(REQUEST_ID)).thenReturn(false);

        // when + then
        assertThrows(NotFoundException.class, () -> itemService.createItem(USER_ID, itemDtoWithRequest));

        verify(userRepository).existsById(USER_ID);
        verify(itemRequestRepository).existsById(REQUEST_ID);
        verify(itemRepository, never()).save(any());
    }

    @Test
    @DisplayName("Создание вещи: несуществующий пользователь должен вызывать NotFoundException")
    void createItem_shouldThrowExceptionWhenUserNotFound() {
        // given
        when(userRepository.existsById(USER_ID)).thenReturn(false);

        // when + then
        assertThrows(NotFoundException.class, () -> itemService.createItem(USER_ID, itemDto));

        verify(userRepository).existsById(USER_ID);
        verify(itemRepository, never()).save(any());
    }

    @Test
    @DisplayName("Создание вещи: пустое название должно вызывать ValidationException")
    void createItem_shouldThrowExceptionWhenNameIsBlank() {
        // given
        ItemDto invalidDto = ItemDto.builder()
                .name(EMPTY_STRING)
                .description(ITEM_DESCRIPTION)
                .available(true)
                .build();

        when(userRepository.existsById(USER_ID)).thenReturn(true);

        // when + then
        assertThrows(ValidationException.class, () -> itemService.createItem(USER_ID, invalidDto));

        verify(itemRepository, never()).save(any());
    }

    @Test
    @DisplayName("Обновление вещи: успешное обновление")
    void updateItem_shouldUpdateItemSuccessfully() {
        // given
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        when(itemMapper.toDto(item, USER_ID)).thenReturn(itemDto);

        // when
        ItemDto result = itemService.updateItem(USER_ID, ITEM_ID, updateDto);

        // then
        assertNotNull(result);
        assertEquals(UPDATED_NAME, item.getName());
        assertEquals(UPDATED_DESCRIPTION, item.getDescription());
        assertFalse(item.getAvailable());

        verify(itemRepository).findById(ITEM_ID);
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    @DisplayName("Обновление вещи: несуществующая вещь должна вызывать NotFoundException")
    void updateItem_shouldThrowExceptionWhenItemNotFound() {
        // given
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.empty());

        // when + then
        assertThrows(NotFoundException.class, () -> itemService.updateItem(USER_ID, ITEM_ID, itemDto));

        verify(itemRepository).findById(ITEM_ID);
        verify(itemRepository, never()).save(any());
    }

    @Test
    @DisplayName("Обновление вещи: попытка обновить чужую вещь должна вызывать NotFoundException")
    void updateItem_shouldThrowExceptionWhenUserNotOwner() {
        // given
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));

        // when + then
        assertThrows(NotFoundException.class, () -> itemService.updateItem(OTHER_USER_ID, ITEM_ID, itemDto));

        verify(itemRepository).findById(ITEM_ID);
        verify(itemRepository, never()).save(any());
    }

    @Test
    @DisplayName("Получение вещи: владелец должен видеть бронирования")
    void getItemById_shouldReturnItemForOwnerWithBookings() {
        // given
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(bookingRepository.findLastBookingForItem(eq(ITEM_ID), any(LocalDateTime.class)))
                .thenReturn(Optional.of(lastBooking));
        when(bookingRepository.findNextBookingForItem(eq(ITEM_ID), any(LocalDateTime.class)))
                .thenReturn(Optional.of(nextBooking));

        ItemMapper realMapper = new ItemMapper(bookingRepository, commentRepository, commentMapper);

        ItemServiceImpl realItemService = new ItemServiceImpl(
                itemRepository,
                userRepository,
                realMapper,
                commentRepository,
                commentMapper,
                bookingRepository,
                itemRequestRepository
        );

        // when
        ItemDto result = realItemService.getItemById(ITEM_ID, USER_ID);

        // then
        assertNotNull(result);
        assertEquals(ITEM_ID, result.getId());

        verify(itemRepository).findById(ITEM_ID);
        verify(bookingRepository).findLastBookingForItem(eq(ITEM_ID), any(LocalDateTime.class));
        verify(bookingRepository).findNextBookingForItem(eq(ITEM_ID), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Получение вещи: не владелец не должен видеть бронирования")
    void getItemById_shouldReturnItemForNonOwnerWithoutBookings() {
        // given
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(itemMapper.toDto(item, OTHER_USER_ID)).thenReturn(itemDto);

        // when
        ItemDto result = itemService.getItemById(ITEM_ID, OTHER_USER_ID);

        // then
        assertNotNull(result);
        assertEquals(ITEM_ID, result.getId());

        verify(itemRepository).findById(ITEM_ID);
        verify(bookingRepository, never()).findLastBookingForItem(anyLong(), any());
        verify(bookingRepository, never()).findNextBookingForItem(anyLong(), any());
    }

    @Test
    @DisplayName("Получение всех вещей владельца: пагинация работает")
    void getAllItemsByOwner_shouldReturnItemsWithPagination() {
        // given
        List<Item> items = List.of(item);
        when(itemRepository.findByOwnerId(eq(USER_ID), any(PageRequest.class))).thenReturn(items);
        when(itemMapper.toDto(item, USER_ID)).thenReturn(itemDto);

        // when
        List<ItemDto> results = itemService.getAllItemsByOwner(USER_ID, FROM_DEFAULT, SIZE_DEFAULT);

        // then
        assertEquals(1, results.size());
        assertEquals(ITEM_ID, results.get(0).getId());

        verify(itemRepository).findByOwnerId(eq(USER_ID), any(PageRequest.class));
    }

    @Test
    @DisplayName("Получение всех вещей владельца: невалидные параметры пагинации должны вызывать ValidationException")
    void getAllItemsByOwner_shouldThrowExceptionWhenPaginationInvalid() {
        // when + then
        assertThrows(ValidationException.class, () -> itemService.getAllItemsByOwner(USER_ID, FROM_NEGATIVE, SIZE_DEFAULT));
        assertThrows(ValidationException.class, () -> itemService.getAllItemsByOwner(USER_ID, FROM_DEFAULT, SIZE_ZERO));
    }

    @Test
    @DisplayName("Поиск вещей: должен возвращать список найденных вещей")
    void searchItems_shouldReturnItems() {
        // given
        List<Item> items = List.of(item);
        when(itemRepository.searchItems(eq(SEARCH_TEXT), any(PageRequest.class))).thenReturn(items);
        when(itemMapper.toDto(item, null)).thenReturn(itemDto);

        // when
        List<ItemDto> results = itemService.searchItems(SEARCH_TEXT, FROM_DEFAULT, SIZE_DEFAULT);

        // then
        assertEquals(1, results.size());
        assertEquals(ITEM_ID, results.get(0).getId());

        verify(itemRepository).searchItems(eq(SEARCH_TEXT), any(PageRequest.class));
    }

    @Test
    @DisplayName("Поиск вещей: пустой поисковый запрос должен возвращать пустой список")
    void searchItems_shouldReturnEmptyListWhenTextIsBlank() {
        // when
        List<ItemDto> results = itemService.searchItems(EMPTY_STRING, FROM_DEFAULT, SIZE_DEFAULT);
        assertTrue(results.isEmpty());

        results = itemService.searchItems("   ", FROM_DEFAULT, SIZE_DEFAULT);
        assertTrue(results.isEmpty());

        results = itemService.searchItems(null, FROM_DEFAULT, SIZE_DEFAULT);
        assertTrue(results.isEmpty());

        // then
        verify(itemRepository, never()).searchItems(anyString(), any());
    }
}