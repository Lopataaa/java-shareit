package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
@DisplayName("Тесты контроллера вещей")
class ItemControllerTest {

    private static final Long USER_ID = 1L;
    private static final Long ITEM_ID = 1L;
    private static final Long REQUEST_ID = 5L;
    private static final int FROM_DEFAULT = 0;
    private static final int SIZE_DEFAULT = 10;
    private static final int FROM_NEGATIVE = -1;
    private static final int SIZE_ZERO = 0;
    private static final String SEARCH_TEXT = "hammer";
    private static final String EMPTY_STRING = "";
    private static final String BLANK_STRING = "   ";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemClient itemClient;

    private ItemDto validItemDto;
    private ItemDto itemDtoWithRequest;
    private ItemDto invalidItemDtoNoName;
    private ItemDto invalidItemDtoNoDescription;
    private ItemDto invalidItemDtoNoAvailable;
    private CommentDto validCommentDto;
    private CommentDto invalidCommentDto;
    private ResponseEntity<Object> responseEntityCreated;
    private ResponseEntity<Object> responseEntityOk;

    @BeforeEach
    void setUp() {
        // given - подготовка тестовых данных
        validItemDto = ItemDto.builder()
                .name("Hammer")
                .description("Heavy hammer")
                .available(true)
                .build();

        itemDtoWithRequest = ItemDto.builder()
                .name("Hammer")
                .description("Heavy hammer")
                .available(true)
                .requestId(REQUEST_ID)
                .build();

        validCommentDto = CommentDto.builder()
                .text("Great item!")
                .build();

        invalidItemDtoNoName = ItemDto.builder()
                .name(EMPTY_STRING)
                .description("Heavy hammer")
                .available(true)
                .build();

        invalidItemDtoNoDescription = ItemDto.builder()
                .name("Hammer")
                .description(EMPTY_STRING)
                .available(true)
                .build();

        invalidItemDtoNoAvailable = ItemDto.builder()
                .name("Hammer")
                .description("Heavy hammer")
                .available(null)
                .build();

        invalidCommentDto = CommentDto.builder()
                .text(EMPTY_STRING)
                .build();

        responseEntityCreated = ResponseEntity.status(HttpStatus.CREATED).build();
        responseEntityOk = ResponseEntity.ok().build();
    }

    @Test
    @DisplayName("POST /items: успешное создание вещи должно возвращать 201 Created")
    void createItem_shouldReturnCreated() throws Exception {
        // given
        when(itemClient.createItem(eq(USER_ID), any(ItemDto.class)))
                .thenReturn(responseEntityCreated);

        // when + then
        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validItemDto)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /items: создание вещи с requestId должно возвращать 201 Created")
    void createItem_shouldReturnCreatedWithRequestId() throws Exception {
        // given
        when(itemClient.createItem(eq(USER_ID), any(ItemDto.class)))
                .thenReturn(responseEntityCreated);

        // when + then
        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDtoWithRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /items: пустое название должно возвращать 400 Bad Request")
    void createItem_shouldReturnBadRequestWhenNameIsBlank() throws Exception {
        // when + then
        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidItemDtoNoName)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /items: пустое описание должно возвращать 400 Bad Request")
    void createItem_shouldReturnBadRequestWhenDescriptionIsBlank() throws Exception {
        // when + then
        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidItemDtoNoDescription)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /items: null статус доступности должно возвращать 400 Bad Request")
    void createItem_shouldReturnBadRequestWhenAvailableIsNull() throws Exception {
        // when + then
        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidItemDtoNoAvailable)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /items/{itemId}: обновление вещи должно возвращать 200 OK")
    void updateItem_shouldReturnOk() throws Exception {
        // given
        when(itemClient.updateItem(eq(USER_ID), eq(ITEM_ID), any(ItemDto.class)))
                .thenReturn(responseEntityOk);

        // when + then
        mockMvc.perform(patch("/items/{itemId}", ITEM_ID)
                        .header("X-Sharer-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validItemDto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /items/{itemId}: получение вещи должно возвращать 200 OK")
    void getItemById_shouldReturnOk() throws Exception {
        // given
        when(itemClient.getItemById(ITEM_ID, USER_ID)).thenReturn(responseEntityOk);

        // when + then
        mockMvc.perform(get("/items/{itemId}", ITEM_ID)
                        .header("X-Sharer-User-Id", USER_ID))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /items: получение всех вещей владельца должно возвращать 200 OK")
    void getAllItemsByOwner_shouldReturnOk() throws Exception {
        // given
        when(itemClient.getAllItemsByOwner(eq(USER_ID), anyInt(), anyInt()))
                .thenReturn(responseEntityOk);

        // when + then
        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", USER_ID)
                        .param("from", String.valueOf(FROM_DEFAULT))
                        .param("size", String.valueOf(SIZE_DEFAULT)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /items: отрицательный from должен возвращать 400 Bad Request")
    void getAllItemsByOwner_shouldReturnBadRequestWhenFromNegative() throws Exception {
        // when + then
        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", USER_ID)
                        .param("from", String.valueOf(FROM_NEGATIVE))
                        .param("size", String.valueOf(SIZE_DEFAULT)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /items: нулевой size должен возвращать 400 Bad Request")
    void getAllItemsByOwner_shouldReturnBadRequestWhenSizeZero() throws Exception {
        // when + then
        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", USER_ID)
                        .param("from", String.valueOf(FROM_DEFAULT))
                        .param("size", String.valueOf(SIZE_ZERO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /items/search: поиск вещей должен возвращать 200 OK")
    void searchItems_shouldReturnOk() throws Exception {
        // given
        when(itemClient.searchItems(anyString(), anyInt(), anyInt()))
                .thenReturn(responseEntityOk);

        // when + then
        mockMvc.perform(get("/items/search")
                        .param("text", SEARCH_TEXT)
                        .param("from", String.valueOf(FROM_DEFAULT))
                        .param("size", String.valueOf(SIZE_DEFAULT)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /items/{itemId}/comment: добавление комментария должно возвращать 201 Created")
    void addComment_shouldReturnCreated() throws Exception {
        // given
        when(itemClient.addComment(eq(USER_ID), eq(ITEM_ID), any(CommentDto.class)))
                .thenReturn(responseEntityCreated);

        // when + then
        mockMvc.perform(post("/items/{itemId}/comment", ITEM_ID)
                        .header("X-Sharer-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCommentDto)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /items/{itemId}/comment: пустой текст комментария должен возвращать 400 Bad Request")
    void addComment_shouldReturnBadRequestWhenTextIsBlank() throws Exception {
        // when + then
        mockMvc.perform(post("/items/{itemId}/comment", ITEM_ID)
                        .header("X-Sharer-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCommentDto)))
                .andExpect(status().isBadRequest());
    }
}