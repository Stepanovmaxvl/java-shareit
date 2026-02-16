package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemDtoJsonTest {

    @Autowired
    private JacksonTester<ItemDto> json;

    @Test
    void testItemDtoSerialization() throws Exception {
        ItemDto itemDto = new ItemDto(1L, "Drill", "Electric drill", true, 1L);

        JsonContent<ItemDto> result = json.write(itemDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Drill");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Electric drill");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(true);
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(1);
    }

    @Test
    void testItemDtoDeserialization() throws Exception {
        String jsonContent = "{\"name\":\"Drill\","
                + "\"description\":\"Electric drill\","
                + "\"available\":true}";

        ItemDto result = json.parseObject(jsonContent);

        assertThat(result.getName()).isEqualTo("Drill");
        assertThat(result.getDescription()).isEqualTo("Electric drill");
        assertThat(result.getAvailable()).isEqualTo(true);
    }
}
