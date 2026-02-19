package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@DisplayName("Тесты контроллера пользователей")
class UserControllerTest {

    private static final Long USER_ID = 1L;
    private static final String USER_NAME = "John Doe";
    private static final String USER_EMAIL = "john@test.com";
    private static final String UPDATED_NAME = "John Updated";
    private static final String UPDATED_EMAIL = "john.updated@test.com";
    private static final String EMPTY_STRING = "";
    private static final String INVALID_EMAIL = "invalid-email";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserClient userClient;

    private UserDto validUserDto;
    private UserDto invalidUserDtoNoName;
    private UserDto invalidUserDtoNoEmail;
    private UserDto invalidUserDtoInvalidEmail;
    private UserDto updateUserDto;
    private ResponseEntity<Object> responseEntityOk;

    @BeforeEach
    void setUp() {
        // given - подготовка тестовых данных
        validUserDto = new UserDto(null, USER_NAME, USER_EMAIL);
        updateUserDto = new UserDto(null, UPDATED_NAME, UPDATED_EMAIL);

        invalidUserDtoNoName = new UserDto(null, EMPTY_STRING, USER_EMAIL);
        invalidUserDtoNoEmail = new UserDto(null, USER_NAME, EMPTY_STRING);
        invalidUserDtoInvalidEmail = new UserDto(null, USER_NAME, INVALID_EMAIL);

        responseEntityOk = ResponseEntity.ok().build();
    }

    @Test
    @DisplayName("POST /users: успешное создание пользователя должно возвращать 200 OK")
    void createUser_shouldReturnOk() throws Exception {
        // given
        when(userClient.createUser(any(UserDto.class))).thenReturn(responseEntityOk);

        // when + then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserDto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /users: пустое имя должно возвращать 400 Bad Request")
    void createUser_shouldReturnBadRequestWhenNameIsBlank() throws Exception {
        // when + then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserDtoNoName)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /users: пустой email должно возвращать 400 Bad Request")
    void createUser_shouldReturnBadRequestWhenEmailIsBlank() throws Exception {
        // when + then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserDtoNoEmail)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /users: некорректный email должно возвращать 400 Bad Request")
    void createUser_shouldReturnBadRequestWhenEmailIsInvalid() throws Exception {
        // when + then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserDtoInvalidEmail)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /users/{userId}: обновление пользователя должно возвращать 200 OK")
    void updateUser_shouldReturnOk() throws Exception {
        // given
        when(userClient.updateUser(eq(USER_ID), any(UserDto.class))).thenReturn(responseEntityOk);

        // when + then
        mockMvc.perform(patch("/users/{userId}", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /users/{userId}: получение пользователя должно возвращать 200 OK")
    void getUserById_shouldReturnOk() throws Exception {
        // given
        when(userClient.getUserById(USER_ID)).thenReturn(responseEntityOk);

        // when + then
        mockMvc.perform(get("/users/{userId}", USER_ID))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /users: получение всех пользователей должно возвращать 200 OK")
    void getAllUsers_shouldReturnOk() throws Exception {
        // given
        when(userClient.getAllUsers()).thenReturn(responseEntityOk);

        // when + then
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /users/{userId}: удаление пользователя должно возвращать 200 OK")
    void deleteUser_shouldReturnOk() throws Exception {
        // given
        when(userClient.deleteUser(USER_ID)).thenReturn(responseEntityOk);

        // when + then
        mockMvc.perform(delete("/users/{userId}", USER_ID))
                .andExpect(status().isOk());
    }
}