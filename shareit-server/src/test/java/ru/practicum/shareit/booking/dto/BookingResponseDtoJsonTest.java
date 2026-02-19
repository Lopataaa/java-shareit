package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.BookingStatus;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@DisplayName("JSON-тесты для DTO ответа бронирования")
class BookingResponseDtoJsonTest {

    private static final Long BOOKING_ID = 1L;
    private static final Long BOOKER_ID = 5L;
    private static final Long ITEM_ID = 10L;
    private static final String BOOKER_NAME = "John Doe";
    private static final String ITEM_NAME = "Hammer";
    private static final int YEAR = 2025;
    private static final int MONTH = 3;
    private static final int START_DAY = 1;
    private static final int END_DAY = 5;
    private static final int START_HOUR = 10;
    private static final int END_HOUR = 18;
    private static final int START_MINUTE = 0;
    private static final int END_MINUTE = 0;

    @Autowired
    private JacksonTester<BookingResponseDto> json;

    @Test
    @DisplayName("Сериализация: объект DTO должен корректно преобразовываться в JSON")
    void testSerialize() throws Exception {
        // given
        LocalDateTime start = LocalDateTime.of(YEAR, MONTH, START_DAY, START_HOUR, START_MINUTE);
        LocalDateTime end = LocalDateTime.of(YEAR, MONTH, END_DAY, END_HOUR, END_MINUTE);

        BookingResponseDto.BookerDto booker = BookingResponseDto.BookerDto.builder()
                .id(BOOKER_ID)
                .name(BOOKER_NAME)
                .build();

        BookingResponseDto.ItemDto item = BookingResponseDto.ItemDto.builder()
                .id(ITEM_ID)
                .name(ITEM_NAME)
                .build();

        BookingResponseDto dto = BookingResponseDto.builder()
                .id(BOOKING_ID)
                .start(start)
                .end(end)
                .status(BookingStatus.APPROVED)
                .booker(booker)
                .item(item)
                .build();

        // when
        JsonContent<BookingResponseDto> result = json.write(dto);

        // then
        assertThat(result).hasJsonPathNumberValue("$.id");
        assertThat(result).hasJsonPathStringValue("$.start");
        assertThat(result).hasJsonPathStringValue("$.end");
        assertThat(result).hasJsonPathStringValue("$.status");

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(BOOKING_ID.intValue());
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2025-03-01T10:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2025-03-05T18:00:00");
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("APPROVED");

        assertThat(result).hasJsonPathNumberValue("$.booker.id");
        assertThat(result).hasJsonPathStringValue("$.booker.name");
        assertThat(result).extractingJsonPathNumberValue("$.booker.id").isEqualTo(BOOKER_ID.intValue());
        assertThat(result).extractingJsonPathStringValue("$.booker.name").isEqualTo(BOOKER_NAME);

        assertThat(result).hasJsonPathNumberValue("$.item.id");
        assertThat(result).hasJsonPathStringValue("$.item.name");
        assertThat(result).extractingJsonPathNumberValue("$.item.id").isEqualTo(ITEM_ID.intValue());
        assertThat(result).extractingJsonPathStringValue("$.item.name").isEqualTo(ITEM_NAME);
    }

    @Test
    @DisplayName("Десериализация: JSON должен корректно преобразовываться в объект DTO")
    void testDeserialize() throws Exception {
        // given
        String content = "{\"id\":1,\"start\":\"2025-03-01T10:00:00\",\"end\":\"2025-03-05T18:00:00\"," +
                "\"status\":\"APPROVED\"," +
                "\"booker\":{\"id\":5,\"name\":\"John Doe\"}," +
                "\"item\":{\"id\":10,\"name\":\"Hammer\"}}";

        // when
        BookingResponseDto dto = json.parseObject(content);

        // then
        assertThat(dto.getId()).isEqualTo(BOOKING_ID);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(YEAR, MONTH, START_DAY, START_HOUR, START_MINUTE));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(YEAR, MONTH, END_DAY, END_HOUR, END_MINUTE));
        assertThat(dto.getStatus()).isEqualTo(BookingStatus.APPROVED);

        assertThat(dto.getBooker()).isNotNull();
        assertThat(dto.getBooker().getId()).isEqualTo(BOOKER_ID);
        assertThat(dto.getBooker().getName()).isEqualTo(BOOKER_NAME);

        assertThat(dto.getItem()).isNotNull();
        assertThat(dto.getItem().getId()).isEqualTo(ITEM_ID);
        assertThat(dto.getItem().getName()).isEqualTo(ITEM_NAME);
    }

    @Test
    @DisplayName("Builder: должен корректно создавать объект через builder")
    void testBuilder() {
        // given
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(3);

        BookingResponseDto.BookerDto booker = BookingResponseDto.BookerDto.builder()
                .id(BOOKER_ID)
                .name(BOOKER_NAME)
                .build();

        BookingResponseDto.ItemDto item = BookingResponseDto.ItemDto.builder()
                .id(ITEM_ID)
                .name(ITEM_NAME)
                .build();

        // when
        BookingResponseDto dto = BookingResponseDto.builder()
                .id(BOOKING_ID)
                .start(start)
                .end(end)
                .status(BookingStatus.WAITING)
                .booker(booker)
                .item(item)
                .build();

        // then
        assertThat(dto.getId()).isEqualTo(BOOKING_ID);
        assertThat(dto.getStart()).isEqualTo(start);
        assertThat(dto.getEnd()).isEqualTo(end);
        assertThat(dto.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(dto.getBooker().getId()).isEqualTo(BOOKER_ID);
        assertThat(dto.getBooker().getName()).isEqualTo(BOOKER_NAME);
        assertThat(dto.getItem().getId()).isEqualTo(ITEM_ID);
        assertThat(dto.getItem().getName()).isEqualTo(ITEM_NAME);
    }
}