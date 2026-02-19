package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты сервиса запросов вещей")
class ItemRequestServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;
    private static final Long REQUEST_ID = 1L;
    private static final Long ITEM_ID = 1L;
    private static final String USER_NAME = "Test User";
    private static final String USER_EMAIL = "test@test.com";
    private static final String REQUEST_DESCRIPTION = "Need a tool";
    private static final String ITEM_NAME = "Hammer";
    private static final String ITEM_DESCRIPTION = "Heavy hammer";
    private static final int FROM_DEFAULT = 0;
    private static final int SIZE_DEFAULT = 10;

    @Mock
    private ItemRequestRepository requestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemRequestMapper requestMapper;

    @InjectMocks
    private ItemRequestServiceImpl requestService;

    private User user;
    private ItemRequest request;
    private ItemRequestDto requestDto;
    private ItemRequestResponseDto responseDto;
    private Item item;

    @BeforeEach
    void setUp() {
        // given - подготовка тестовых данных
        user = User.builder()
                .id(USER_ID)
                .name(USER_NAME)
                .email(USER_EMAIL)
                .build();

        request = ItemRequest.builder()
                .id(REQUEST_ID)
                .description(REQUEST_DESCRIPTION)
                .requestor(user)
                .created(LocalDateTime.now())
                .build();

        requestDto = ItemRequestDto.builder()
                .description(REQUEST_DESCRIPTION)
                .build();

        responseDto = ItemRequestResponseDto.builder()
                .id(REQUEST_ID)
                .description(REQUEST_DESCRIPTION)
                .created(LocalDateTime.now())
                .items(List.of())
                .build();

        item = Item.builder()
                .id(ITEM_ID)
                .name(ITEM_NAME)
                .description(ITEM_DESCRIPTION)
                .available(true)
                .ownerId(OTHER_USER_ID)
                .requestId(REQUEST_ID)
                .build();
    }

    @Test
    @DisplayName("Создание запроса: успешное создание")
    void create_shouldCreateRequestSuccessfully() {
        // given
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(requestMapper.toEntity(requestDto, user)).thenReturn(request);
        when(requestRepository.save(any(ItemRequest.class))).thenReturn(request);
        when(requestMapper.toDto(request, List.of())).thenReturn(responseDto);

        // when
        ItemRequestResponseDto result = requestService.create(USER_ID, requestDto);

        // then
        assertNotNull(result);
        assertEquals(REQUEST_ID, result.getId());
        assertEquals(REQUEST_DESCRIPTION, result.getDescription());

        verify(userRepository).findById(USER_ID);
        verify(requestRepository).save(any(ItemRequest.class));
    }

    @Test
    @DisplayName("Создание запроса: несуществующий пользователь должен вызывать NotFoundException")
    void create_shouldThrowExceptionWhenUserNotFound() {
        // given
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // when + then
        assertThrows(NotFoundException.class, () -> requestService.create(USER_ID, requestDto));

        verify(userRepository).findById(USER_ID);
        verify(requestRepository, never()).save(any());
    }

    @Test
    @DisplayName("Получение своих запросов: должно возвращать запросы пользователя")
    void getUserRequests_shouldReturnUserRequests() {
        // given
        List<ItemRequest> requests = List.of(request);
        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(requestRepository.findByRequestorIdOrderByCreatedDesc(USER_ID)).thenReturn(requests);
        when(itemRepository.findByRequestIdIn(anyList())).thenReturn(List.of(item));
        when(requestMapper.toDto(request, List.of(item))).thenReturn(responseDto);

        // when
        List<ItemRequestResponseDto> results = requestService.getUserRequests(USER_ID);

        // then
        assertEquals(1, results.size());
        assertEquals(REQUEST_ID, results.get(0).getId());

        verify(userRepository).existsById(USER_ID);
        verify(requestRepository).findByRequestorIdOrderByCreatedDesc(USER_ID);
        verify(itemRepository).findByRequestIdIn(List.of(REQUEST_ID));
    }

    @Test
    @DisplayName("Получение своих запросов: несуществующий пользователь должен вызывать NotFoundException")
    void getUserRequests_shouldThrowExceptionWhenUserNotFound() {
        // given
        when(userRepository.existsById(USER_ID)).thenReturn(false);

        // when + then
        assertThrows(NotFoundException.class, () -> requestService.getUserRequests(USER_ID));

        verify(userRepository).existsById(USER_ID);
        verify(requestRepository, never()).findByRequestorIdOrderByCreatedDesc(anyLong());
    }

    @Test
    @DisplayName("Получение чужих запросов: должно возвращать запросы других пользователей")
    void getAllRequests_shouldReturnOtherUsersRequests() {
        // given
        when(requestRepository.findOtherRequests(eq(USER_ID), any(PageRequest.class)))
                .thenReturn(List.of(request));
        when(itemRepository.findByRequestIdIn(anyList())).thenReturn(List.of(item));
        when(requestMapper.toDto(request, List.of(item))).thenReturn(responseDto);

        // when
        List<ItemRequestResponseDto> results = requestService.getAllRequests(USER_ID, FROM_DEFAULT, SIZE_DEFAULT);

        // then
        assertEquals(1, results.size());
        assertEquals(REQUEST_ID, results.get(0).getId());

        verify(requestRepository).findOtherRequests(eq(USER_ID), any(PageRequest.class));
        verify(itemRepository).findByRequestIdIn(List.of(REQUEST_ID));
    }

    @Test
    @DisplayName("Получение чужих запросов: когда нет запросов должен возвращать пустой список")
    void getAllRequests_shouldReturnEmptyListWhenNoRequests() {
        // given
        when(requestRepository.findOtherRequests(eq(USER_ID), any(PageRequest.class)))
                .thenReturn(List.of());

        // when
        List<ItemRequestResponseDto> results = requestService.getAllRequests(USER_ID, FROM_DEFAULT, SIZE_DEFAULT);

        // then
        assertTrue(results.isEmpty());

        verify(requestRepository).findOtherRequests(eq(USER_ID), any(PageRequest.class));
        verify(itemRepository, never()).findByRequestIdIn(anyList());
    }

    @Test
    @DisplayName("Получение запроса по ID: успешное получение")
    void getRequestById_shouldReturnRequest() {
        // given
        when(requestRepository.findById(REQUEST_ID)).thenReturn(Optional.of(request));
        when(itemRepository.findByRequestId(REQUEST_ID)).thenReturn(List.of(item));
        when(requestMapper.toDto(request, List.of(item))).thenReturn(responseDto);

        // when
        ItemRequestResponseDto result = requestService.getRequestById(REQUEST_ID, OTHER_USER_ID);

        // then
        assertNotNull(result);
        assertEquals(REQUEST_ID, result.getId());

        verify(requestRepository).findById(REQUEST_ID);
        verify(itemRepository).findByRequestId(REQUEST_ID);
    }

    @Test
    @DisplayName("Получение запроса по ID: несуществующий запрос должен вызывать NotFoundException")
    void getRequestById_shouldThrowExceptionWhenRequestNotFound() {
        // given
        when(requestRepository.findById(REQUEST_ID)).thenReturn(Optional.empty());

        // when + then
        assertThrows(NotFoundException.class, () -> requestService.getRequestById(REQUEST_ID, OTHER_USER_ID));

        verify(requestRepository).findById(REQUEST_ID);
        verify(itemRepository, never()).findByRequestId(anyLong());
    }
}
