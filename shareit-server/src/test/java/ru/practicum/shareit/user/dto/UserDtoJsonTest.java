package ru.practicum.shareit.user.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@DisplayName("JSON-тесты для DTO пользователя")
class UserDtoJsonTest {

    private static final Long USER_ID = 1L;
    private static final String USER_NAME = "John Doe";
    private static final String USER_EMAIL = "john@test.com";
    private static final String USER_NAME_DESERIALIZE = "Jane Smith";
    private static final String USER_EMAIL_DESERIALIZE = "jane@test.com";

    @Autowired
    private JacksonTester<UserDto> json;

    @Test
    @DisplayName("Сериализация: объект DTO со всеми полями должен корректно преобразовываться в JSON")
    void testSerialize() throws Exception {
        // given
        UserDto userDto = new UserDto(USER_ID, USER_NAME, USER_EMAIL);

        // when
        JsonContent<UserDto> result = json.write(userDto);

        // then
        assertThat(result).hasJsonPathNumberValue("$.id");
        assertThat(result).hasJsonPathStringValue("$.name");
        assertThat(result).hasJsonPathStringValue("$.email");
    }

    @Test
    @DisplayName("Сериализация: null id не должен включаться в JSON")
    void testSerializeWithNullId() throws Exception {
        // given
        UserDto userDto = new UserDto(null, USER_NAME, USER_EMAIL);

        // when
        JsonContent<UserDto> result = json.write(userDto);

        // then
        assertThat(result).hasJsonPath("$.id");
        assertThat(result).hasJsonPathStringValue("$.name");
        assertThat(result).hasJsonPathStringValue("$.email");
        assertThat(result).extractingJsonPathNumberValue("$.id").isNull();
    }

    @Test
    @DisplayName("Десериализация: JSON должен корректно преобразовываться в объект DTO")
    void testDeserialize() throws Exception {
        // given
        String content = "{\"id\":1,\"name\":\"Jane Smith\",\"email\":\"jane@test.com\"}";

        // when
        UserDto userDto = json.parseObject(content);

        // then
        assertThat(userDto.getId()).isEqualTo(USER_ID);
        assertThat(userDto.getName()).isEqualTo(USER_NAME_DESERIALIZE);
        assertThat(userDto.getEmail()).isEqualTo(USER_EMAIL_DESERIALIZE);
    }
}