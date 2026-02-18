package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@DisplayName("JSON-тесты для DTO комментария")
class CommentDtoJsonTest {

    private static final Long COMMENT_ID = 1L;
    private static final String COMMENT_TEXT = "Great item!";
    private static final String AUTHOR_NAME = "John Doe";
    private static final int YEAR = 2025;
    private static final int MONTH = 1;
    private static final int DAY = 1;
    private static final int HOUR = 12;
    private static final int MINUTE = 0;
    private static final int SECOND = 0;

    @Autowired
    private JacksonTester<CommentDto> json;

    @Test
    @DisplayName("Сериализация: объект DTO со всеми полями должен корректно преобразовываться в JSON")
    void testSerialize() throws Exception {
        // given
        LocalDateTime created = LocalDateTime.of(YEAR, MONTH, DAY, HOUR, MINUTE, SECOND);

        CommentDto commentDto = CommentDto.builder()
                .id(COMMENT_ID)
                .text(COMMENT_TEXT)
                .authorName(AUTHOR_NAME)
                .created(created)
                .build();

        // when
        JsonContent<CommentDto> result = json.write(commentDto);

        // then
        assertThat(result).hasJsonPathNumberValue("$.id");
        assertThat(result).hasJsonPathStringValue("$.text");
        assertThat(result).hasJsonPathStringValue("$.authorName");
        assertThat(result).hasJsonPathStringValue("$.created");
    }

    @Test
    @DisplayName("Сериализация: null поля не должны включаться в JSON")
    void testSerializeWithNullFields() throws Exception {
        // given
        CommentDto commentDto = CommentDto.builder()
                .id(COMMENT_ID)
                .text(COMMENT_TEXT)
                .build();

        // when
        JsonContent<CommentDto> result = json.write(commentDto);

        // then
        assertThat(result).hasJsonPathNumberValue("$.id");
        assertThat(result).hasJsonPathStringValue("$.text");

        assertThat(result).hasJsonPath("$.authorName");
        assertThat(result).hasJsonPath("$.created");
        assertThat(result).extractingJsonPathStringValue("$.authorName").isNull();
        assertThat(result).extractingJsonPathStringValue("$.created").isNull();
    }

    @Test
    @DisplayName("Десериализация: JSON должен корректно преобразовываться в объект DTO")
    void testDeserialize() throws Exception {
        // given
        String content = "{\"id\":1,\"text\":\"Excellent!\",\"authorName\":\"Jane Smith\"}";

        // when
        CommentDto commentDto = json.parseObject(content);

        // then
        assertThat(commentDto.getId()).isEqualTo(COMMENT_ID);
        assertThat(commentDto.getText()).isEqualTo("Excellent!");
        assertThat(commentDto.getAuthorName()).isEqualTo("Jane Smith");
        assertThat(commentDto.getCreated()).isNull();
    }
}