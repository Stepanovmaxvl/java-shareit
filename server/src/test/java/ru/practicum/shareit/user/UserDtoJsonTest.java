package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.user.dto.UserDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserDtoJsonTest {

    @Autowired
    private JacksonTester<UserDto> json;

    @Test
    void testUserDtoSerialization() throws Exception {
        UserDto userDto = new UserDto(1L, "User", "user@email.com");

        JsonContent<UserDto> result = json.write(userDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("User");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("user@email.com");
    }

    @Test
    void testUserDtoDeserialization() throws Exception {
        String jsonContent = "{\"name\":\"User\",\"email\":\"user@email.com\"}";

        UserDto result = json.parseObject(jsonContent);

        assertThat(result.getName()).isEqualTo("User");
        assertThat(result.getEmail()).isEqualTo("user@email.com");
    }
}
