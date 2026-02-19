package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@DisplayName("JSON-тесты для DTO вещи")
class ItemDtoJsonTest {

    private static final Long ITEM_ID = 1L;
    private static final Long OWNER_ID = 2L;
    private static final Long REQUEST_ID = 3L;
    private static final String ITEM_NAME = "Hammer";
    private static final String ITEM_DESCRIPTION = "Heavy hammer";
    private static final boolean ITEM_AVAILABLE = true;

    @Autowired
    private JacksonTester<ItemDto> json;

    @Test
    @DisplayName("Сериализация: объект DTO со всеми полями должен корректно преобразовываться в JSON")
    void testSerialize() throws Exception {
        // given
        ItemDto itemDto = ItemDto.builder()
                .id(ITEM_ID)
                .name(ITEM_NAME)
                .description(ITEM_DESCRIPTION)
                .available(ITEM_AVAILABLE)
                .ownerId(OWNER_ID)
                .requestId(REQUEST_ID)
                .build();

        // when
        JsonContent<ItemDto> result = json.write(itemDto);

        // then
        assertThat(result).hasJsonPathNumberValue("$.id");
        assertThat(result).hasJsonPathStringValue("$.name");
        assertThat(result).hasJsonPathStringValue("$.description");
        assertThat(result).hasJsonPathBooleanValue("$.available");
        assertThat(result).hasJsonPathNumberValue("$.ownerId");
        assertThat(result).hasJsonPathNumberValue("$.requestId");
    }

    @Test
    @DisplayName("Сериализация: минимальный набор полей должен корректно преобразовываться в JSON")
    void testSerializeWithMinimalFields() throws Exception {
        // given
        ItemDto itemDto = ItemDto.builder()
                .id(ITEM_ID)
                .name(ITEM_NAME)
                .description(ITEM_DESCRIPTION)
                .available(ITEM_AVAILABLE)
                .ownerId(OWNER_ID)
                .build();

        // when
        JsonContent<ItemDto> result = json.write(itemDto);

        // then
        assertThat(result).hasJsonPathNumberValue("$.id");
        assertThat(result).hasJsonPathStringValue("$.name");
        assertThat(result).hasJsonPathStringValue("$.description");
        assertThat(result).hasJsonPathBooleanValue("$.available");
        assertThat(result).hasJsonPathNumberValue("$.ownerId");

        assertThat(result).hasJsonPath("$.requestId");
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isNull();
    }

    @Test
    @DisplayName("Десериализация: JSON должен корректно преобразовываться в объект DTO")
    void testDeserialize() throws Exception {
        // given
        String content = "{\"id\":1,\"name\":\"Drill\",\"description\":\"Electric drill\"," +
                "\"available\":true,\"ownerId\":2,\"requestId\":3}";

        // when
        ItemDto itemDto = json.parseObject(content);

        // then
        assertThat(itemDto.getId()).isEqualTo(ITEM_ID);
        assertThat(itemDto.getName()).isEqualTo("Drill");
        assertThat(itemDto.getDescription()).isEqualTo("Electric drill");
        assertThat(itemDto.getAvailable()).isTrue();
        assertThat(itemDto.getOwnerId()).isEqualTo(OWNER_ID);
        assertThat(itemDto.getRequestId()).isEqualTo(REQUEST_ID);
    }
}