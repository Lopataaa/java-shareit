package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@DisplayName("JSON-тесты для DTO ответа на запрос вещи")
class ItemRequestResponseDtoJsonTest {

    private static final Long REQUEST_ID = 1L;
    private static final String DESCRIPTION = "Need a hammer";
    private static final Long ITEM_ID = 10L;
    private static final String ITEM_NAME = "Hammer";
    private static final String ITEM_DESCRIPTION = "Heavy hammer";
    private static final Long OWNER_ID = 5L;
    private static final int YEAR = 2025;
    private static final int MONTH = 1;
    private static final int DAY = 1;
    private static final int HOUR = 10;
    private static final int MINUTE = 0;

    @Autowired
    private JacksonTester<ItemRequestResponseDto> json;

    @Test
    @DisplayName("Сериализация: объект DTO должен корректно преобразовываться в JSON")
    void testSerialize() throws Exception {
        // given
        LocalDateTime now = LocalDateTime.of(YEAR, MONTH, DAY, HOUR, MINUTE);

        ItemDto itemDto = ItemDto.builder()
                .id(ITEM_ID)
                .name(ITEM_NAME)
                .description(ITEM_DESCRIPTION)
                .available(true)
                .ownerId(OWNER_ID)
                .requestId(REQUEST_ID)
                .build();

        ItemRequestResponseDto dto = ItemRequestResponseDto.builder()
                .id(REQUEST_ID)
                .description(DESCRIPTION)
                .created(now)
                .items(List.of(itemDto))
                .build();

        // when
        JsonContent<ItemRequestResponseDto> result = json.write(dto);

        // then
        assertThat(result).hasJsonPathNumberValue("$.id");
        assertThat(result).hasJsonPathStringValue("$.description");
        assertThat(result).hasJsonPathStringValue("$.created");
        assertThat(result).hasJsonPathArrayValue("$.items");
    }

    @Test
    @DisplayName("Десериализация: JSON должен корректно преобразовываться в объект DTO")
    void testDeserialize() throws Exception {
        // given
        String content = "{\"id\":1,\"description\":\"Need a hammer\"," +
                "\"created\":\"2025-01-01T10:00:00\"," +
                "\"items\":[{\"id\":10,\"name\":\"Hammer\",\"description\":\"Heavy hammer\"," +
                "\"available\":true,\"ownerId\":5,\"requestId\":1}]}";

        // when
        ItemRequestResponseDto dto = json.parseObject(content);

        // then
        assertThat(dto.getId()).isEqualTo(REQUEST_ID);
        assertThat(dto.getDescription()).isEqualTo(DESCRIPTION);
        assertThat(dto.getCreated()).isEqualTo(LocalDateTime.of(YEAR, MONTH, DAY, HOUR, MINUTE));
        assertThat(dto.getItems()).hasSize(1);
        assertThat(dto.getItems().get(0).getId()).isEqualTo(ITEM_ID);
        assertThat(dto.getItems().get(0).getName()).isEqualTo(ITEM_NAME);
    }
}