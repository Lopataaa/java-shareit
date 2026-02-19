package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@DisplayName("JSON-тесты для DTO запроса бронирования")
class BookItemRequestDtoJsonTest {

    private static final Long ITEM_ID = 1L;
    private static final int YEAR = 2025;
    private static final int MONTH = 3;
    private static final int START_DAY = 1;
    private static final int END_DAY = 5;
    private static final int START_HOUR = 10;
    private static final int END_HOUR = 18;
    private static final int START_MINUTE = 0;
    private static final int END_MINUTE = 0;

    @Autowired
    private JacksonTester<BookItemRequestDto> json;

    @Test
    @DisplayName("Сериализация: объект DTO должен корректно преобразовываться в JSON")
    void testSerialize() throws Exception {
        // given
        LocalDateTime start = LocalDateTime.of(YEAR, MONTH, START_DAY, START_HOUR, START_MINUTE);
        LocalDateTime end = LocalDateTime.of(YEAR, MONTH, END_DAY, END_HOUR, END_MINUTE);

        BookItemRequestDto dto = BookItemRequestDto.builder()
                .itemId(ITEM_ID)
                .start(start)
                .end(end)
                .build();

        // when
        JsonContent<BookItemRequestDto> result = json.write(dto);

        // then
        assertThat(result).hasJsonPathNumberValue("$.itemId");
        assertThat(result).hasJsonPathStringValue("$.start");
        assertThat(result).hasJsonPathStringValue("$.end");

        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2025-03-01T10:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2025-03-05T18:00:00");
    }

    @Test
    @DisplayName("Десериализация: JSON должен корректно преобразовываться в объект DTO")
    void testDeserialize() throws Exception {
        // given
        String content = "{\"itemId\":1,\"start\":\"2025-03-01T10:00:00\",\"end\":\"2025-03-05T18:00:00\"}";

        // when
        BookItemRequestDto dto = json.parseObject(content);

        // then
        assertThat(dto.getItemId()).isEqualTo(ITEM_ID);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(YEAR, MONTH, START_DAY, START_HOUR, START_MINUTE));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(YEAR, MONTH, END_DAY, END_HOUR, END_MINUTE));
    }

    @Test
    @DisplayName("Builder: должен корректно создавать объект через builder")
    void testBuilder() {
        // given
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(3);

        // when
        BookItemRequestDto dto = BookItemRequestDto.builder()
                .itemId(ITEM_ID)
                .start(start)
                .end(end)
                .build();

        // then
        assertThat(dto.getItemId()).isEqualTo(ITEM_ID);
        assertThat(dto.getStart()).isEqualTo(start);
        assertThat(dto.getEnd()).isEqualTo(end);
    }

    @Test
    @DisplayName("NoArgsConstructor: должен корректно создавать объект через пустой конструктор")
    void testNoArgsConstructor() {
        // given
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(3);

        // when
        BookItemRequestDto dto = new BookItemRequestDto();
        dto.setItemId(ITEM_ID);
        dto.setStart(start);
        dto.setEnd(end);

        // then
        assertThat(dto.getItemId()).isEqualTo(ITEM_ID);
        assertThat(dto.getStart()).isNotNull();
        assertThat(dto.getEnd()).isNotNull();
    }

    @Test
    @DisplayName("AllArgsConstructor: должен корректно создавать объект через конструктор со всеми полями")
    void testAllArgsConstructor() {
        // given
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(3);

        // when
        BookItemRequestDto dto = new BookItemRequestDto(ITEM_ID, start, end);

        // then
        assertThat(dto.getItemId()).isEqualTo(ITEM_ID);
        assertThat(dto.getStart()).isEqualTo(start);
        assertThat(dto.getEnd()).isEqualTo(end);
    }
}
