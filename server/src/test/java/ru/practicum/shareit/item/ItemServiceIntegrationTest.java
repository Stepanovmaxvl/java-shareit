package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ItemServiceIntegrationTest {

    private final ItemService itemService;
    private final UserService userService;
    private final BookingService bookingService;

    private UserDto user;

    @BeforeEach
    void setUp() {
        user = userService.create(new UserDto(null, "User", "user@email.com"));
    }

    @Test
    void createItem() {
        ItemDto itemDto = new ItemDto(null, "Item", "Description", true, null);
        ItemDto result = itemService.create(user.getId(), itemDto);

        assertThat(result.getId(), notNullValue());
        assertThat(result.getName(), equalTo("Item"));
        assertThat(result.getDescription(), equalTo("Description"));
        assertThat(result.getAvailable(), equalTo(true));
    }

    @Test
    void updateItem() {
        ItemDto itemDto = new ItemDto(null, "Item", "Description", true, null);
        ItemDto created = itemService.create(user.getId(), itemDto);

        ItemDto updateDto = new ItemDto(null, "Updated", null, null, null);
        ItemDto result = itemService.update(user.getId(), created.getId(), updateDto);

        assertThat(result.getName(), equalTo("Updated"));
        assertThat(result.getDescription(), equalTo("Description"));
    }

    @Test
    void updateItemNotOwner() {
        ItemDto itemDto = new ItemDto(null, "Item", "Description", true, null);
        ItemDto created = itemService.create(user.getId(), itemDto);

        UserDto user2 = userService.create(new UserDto(null, "User2", "user2@email.com"));

        assertThrows(NotFoundException.class,
                () -> itemService.update(user2.getId(), created.getId(), new ItemDto(null, "Updated", null, null, null)));
    }

    @Test
    void getItemById() {
        ItemDto itemDto = new ItemDto(null, "Item", "Description", true, null);
        ItemDto created = itemService.create(user.getId(), itemDto);

        ItemWithBookingsDto result = itemService.getById(user.getId(), created.getId());

        assertThat(result.getId(), equalTo(created.getId()));
        assertThat(result.getName(), equalTo("Item"));
    }

    @Test
    void getAllByOwner() {
        itemService.create(user.getId(), new ItemDto(null, "Item1", "Description1", true, null));
        itemService.create(user.getId(), new ItemDto(null, "Item2", "Description2", true, null));

        List<ItemWithBookingsDto> result = itemService.getAllByOwner(user.getId());

        assertThat(result, hasSize(2));
    }

    @Test
    void searchItems() {
        itemService.create(user.getId(), new ItemDto(null, "Drill", "Electric drill", true, null));
        itemService.create(user.getId(), new ItemDto(null, "Hammer", "Steel hammer", true, null));

        List<ItemDto> result = itemService.search("drill");

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getName(), equalTo("Drill"));
    }

    @Test
    void searchItemsEmptyText() {
        itemService.create(user.getId(), new ItemDto(null, "Drill", "Electric drill", true, null));

        List<ItemDto> result = itemService.search("");

        assertThat(result, empty());
    }

    @Test
    void searchItemsNull() {
        List<ItemDto> result = itemService.search(null);

        assertThat(result, empty());
    }

    @Test
    void addCommentNoBooking() {
        ItemDto created = itemService.create(user.getId(), new ItemDto(null, "Item", "Desc", true, null));
        UserDto commenter = userService.create(new UserDto(null, "Commenter", "commenter@email.com"));

        CommentDto commentDto = new CommentDto(null, "Great!", null, null);

        assertThrows(IllegalArgumentException.class,
                () -> itemService.addComment(commenter.getId(), created.getId(), commentDto));
    }

    @Test
    void getItemByIdAsNonOwner() {
        ItemDto created = itemService.create(user.getId(), new ItemDto(null, "Item", "Desc", true, null));
        UserDto otherUser = userService.create(new UserDto(null, "Other", "other@email.com"));

        ItemWithBookingsDto result = itemService.getById(otherUser.getId(), created.getId());

        assertThat(result.getId(), equalTo(created.getId()));
        assertThat(result.getLastBooking(), equalTo(null));
        assertThat(result.getNextBooking(), equalTo(null));
    }
}
