package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserServiceIntegrationTest {

    private final UserService userService;

    @Test
    void createUser() {
        UserDto userDto = new UserDto(null, "User", "user@email.com");
        UserDto result = userService.create(userDto);

        assertThat(result.getId(), notNullValue());
        assertThat(result.getName(), equalTo("User"));
        assertThat(result.getEmail(), equalTo("user@email.com"));
    }

    @Test
    void createUserDuplicateEmail() {
        UserDto userDto1 = new UserDto(null, "User1", "user@email.com");
        userService.create(userDto1);

        UserDto userDto2 = new UserDto(null, "User2", "user@email.com");
        assertThrows(ConflictException.class, () -> userService.create(userDto2));
    }

    @Test
    void updateUser() {
        UserDto userDto = new UserDto(null, "User", "user@email.com");
        UserDto created = userService.create(userDto);

        UserDto updateDto = new UserDto(null, "Updated", null);
        UserDto result = userService.update(created.getId(), updateDto);

        assertThat(result.getName(), equalTo("Updated"));
        assertThat(result.getEmail(), equalTo("user@email.com"));
    }

    @Test
    void getUserById() {
        UserDto userDto = new UserDto(null, "User", "user@email.com");
        UserDto created = userService.create(userDto);

        UserDto result = userService.getById(created.getId());

        assertThat(result.getId(), equalTo(created.getId()));
        assertThat(result.getName(), equalTo("User"));
    }

    @Test
    void getUserByIdNotFound() {
        assertThrows(NotFoundException.class, () -> userService.getById(999L));
    }

    @Test
    void getAllUsers() {
        userService.create(new UserDto(null, "User1", "user1@email.com"));
        userService.create(new UserDto(null, "User2", "user2@email.com"));

        List<UserDto> result = userService.getAll();

        assertThat(result, hasSize(2));
    }

    @Test
    void deleteUser() {
        UserDto created = userService.create(new UserDto(null, "User", "user@email.com"));
        userService.delete(created.getId());

        assertThrows(NotFoundException.class, () -> userService.getById(created.getId()));
    }
}
