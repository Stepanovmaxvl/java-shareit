package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.user.dto.UserShortDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoJsonTest {

    @Autowired
    private JacksonTester<BookingDto> json;

    @Test
    void testBookingDtoSerialization() throws Exception {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(1L);
        bookingDto.setStart(LocalDateTime.of(2026, 3, 1, 10, 0, 0));
        bookingDto.setEnd(LocalDateTime.of(2026, 3, 2, 10, 0, 0));
        bookingDto.setItemId(1L);
        bookingDto.setItem(new ItemShortDto(1L, "Drill"));
        bookingDto.setBooker(new UserShortDto(2L));
        bookingDto.setStatus(BookingStatus.WAITING);

        JsonContent<BookingDto> result = json.write(bookingDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2026-03-01T10:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2026-03-02T10:00:00");
        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.item.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.item.name").isEqualTo("Drill");
        assertThat(result).extractingJsonPathNumberValue("$.booker.id").isEqualTo(2);
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("WAITING");
    }

    @Test
    void testBookingDtoDeserialization() throws Exception {
        String jsonContent = "{\"itemId\":1,"
                + "\"start\":\"2026-03-01T10:00:00\","
                + "\"end\":\"2026-03-02T10:00:00\"}";

        BookingDto result = json.parseObject(jsonContent);

        assertThat(result.getItemId()).isEqualTo(1L);
        assertThat(result.getStart()).isEqualTo(LocalDateTime.of(2026, 3, 1, 10, 0, 0));
        assertThat(result.getEnd()).isEqualTo(LocalDateTime.of(2026, 3, 2, 10, 0, 0));
    }
}
