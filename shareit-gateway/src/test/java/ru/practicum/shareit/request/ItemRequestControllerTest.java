package ru.practicum.shareit.request;

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
import ru.practicum.shareit.request.dto.ItemRequestDto;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
@DisplayName("Тесты контроллера запросов вещей")
class ItemRequestControllerTest {

    private static final Long USER_ID = 1L;
    private static final Long REQUEST_ID = 1L;
    private static final int FROM_DEFAULT = 0;
    private static final int SIZE_DEFAULT = 10;
    private static final int FROM_NEGATIVE = -1;
    private static final int SIZE_ZERO = 0;
    private static final String DESCRIPTION_VALID = "Need a hammer";
    private static final String DESCRIPTION_EMPTY = "";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RequestClient requestClient;

    private ItemRequestDto validRequestDto;
    private ItemRequestDto invalidRequestDto;
    private ResponseEntity<Object> responseEntityCreated;
    private ResponseEntity<Object> responseEntityOk;

    @BeforeEach
    void setUp() {
        // given - подготовка тестовых данных
        validRequestDto = ItemRequestDto.builder()
                .description(DESCRIPTION_VALID)
                .build();

        invalidRequestDto = ItemRequestDto.builder()
                .description(DESCRIPTION_EMPTY)
                .build();

        responseEntityCreated = ResponseEntity.status(HttpStatus.CREATED).build();
        responseEntityOk = ResponseEntity.ok().build();
    }

    @Test
    @DisplayName("POST /requests: успешное создание запроса должно возвращать 201 Created")
    void create_shouldReturnCreated() throws Exception {
        // given
        when(requestClient.createRequest(eq(USER_ID), any(ItemRequestDto.class)))
                .thenReturn(responseEntityCreated);

        // when + then
        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestDto)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /requests: пустое описание должно возвращать 400 Bad Request")
    void create_shouldReturnBadRequestWhenDescriptionIsBlank() throws Exception {
        // when + then
        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /requests: получение своих запросов должно возвращать 200 OK")
    void getUserRequests_shouldReturnOk() throws Exception {
        // given
        when(requestClient.getUserRequests(USER_ID)).thenReturn(responseEntityOk);

        // when + then
        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", USER_ID))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /requests/all: получение чужих запросов должно возвращать 200 OK")
    void getAllRequests_shouldReturnOk() throws Exception {
        // given
        when(requestClient.getAllRequests(eq(USER_ID), anyInt(), anyInt()))
                .thenReturn(responseEntityOk);

        // when + then
        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", USER_ID)
                        .param("from", String.valueOf(FROM_DEFAULT))
                        .param("size", String.valueOf(SIZE_DEFAULT)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /requests/all: отрицательный from должен возвращать 400 Bad Request")
    void getAllRequests_shouldReturnBadRequestWhenFromNegative() throws Exception {
        // when + then
        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", USER_ID)
                        .param("from", String.valueOf(FROM_NEGATIVE))
                        .param("size", String.valueOf(SIZE_DEFAULT)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /requests/all: нулевой size должен возвращать 400 Bad Request")
    void getAllRequests_shouldReturnBadRequestWhenSizeZero() throws Exception {
        // when + then
        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", USER_ID)
                        .param("from", String.valueOf(FROM_DEFAULT))
                        .param("size", String.valueOf(SIZE_ZERO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /requests/{requestId}: получение запроса по id должно возвращать 200 OK")
    void getRequestById_shouldReturnOk() throws Exception {
        // given
        when(requestClient.getRequestById(REQUEST_ID, USER_ID)).thenReturn(responseEntityOk);

        // when + then
        mockMvc.perform(get("/requests/{requestId}", REQUEST_ID)
                        .header("X-Sharer-User-Id", USER_ID))
                .andExpect(status().isOk());
    }
}