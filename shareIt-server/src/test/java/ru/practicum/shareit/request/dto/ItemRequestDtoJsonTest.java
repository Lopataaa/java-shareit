package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@DisplayName("JSON-тесты для DTO запроса вещи")
class ItemRequestDtoJsonTest {

    private static final Long REQUEST_ID = 1L;
    private static final String DESCRIPTION = "Need a hammer";
    private static final String DESCRIPTION_DRILL = "Need a drill";
    private static final int YEAR = 2025;
    private static final int MONTH = 1;
    private static final int DAY = 1;
    private static final int HOUR = 10;
    private static final int MINUTE = 0;

    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    @DisplayName("Сериализация: объект DTO должен корректно преобразовываться в JSON")
    void testSerialize() throws Exception {
        // given
        LocalDateTime now = LocalDateTime.of(YEAR, MONTH, DAY, HOUR, MINUTE);
        ItemRequestDto dto = ItemRequestDto.builder()
                .id(REQUEST_ID)
                .description(DESCRIPTION)
                .created(now)
                .build();

        // when
        JsonContent<ItemRequestDto> result = json.write(dto);

        // then
        assertThat(result).hasJsonPathNumberValue("$.id");
        assertThat(result).hasJsonPathStringValue("$.description");
        assertThat(result).hasJsonPathStringValue("$.created");
    }

    @Test
    @DisplayName("Десериализация: JSON должен корректно преобразовываться в объект DTO")
    void testDeserialize() throws Exception {
        // given
        String content = "{\"id\":1,\"description\":\"Need a drill\",\"created\":\"2025-01-01T10:00:00\"}";

        // when
        ItemRequestDto dto = json.parseObject(content);

        // then
        assertThat(dto.getId()).isEqualTo(REQUEST_ID);
        assertThat(dto.getDescription()).isEqualTo(DESCRIPTION_DRILL);
        assertThat(dto.getCreated()).isEqualTo(LocalDateTime.of(YEAR, MONTH, DAY, HOUR, MINUTE));
    }
}