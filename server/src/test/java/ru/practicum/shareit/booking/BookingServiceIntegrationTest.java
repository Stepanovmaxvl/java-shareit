package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
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
class BookingServiceIntegrationTest {

    private final BookingService bookingService;
    private final ItemService itemService;
    private final UserService userService;

    private UserDto owner;
    private UserDto booker;
    private ItemDto item;

    @BeforeEach
    void setUp() {
        owner = userService.create(new UserDto(null, "Owner", "owner@email.com"));
        booker = userService.create(new UserDto(null, "Booker", "booker@email.com"));
        item = itemService.create(owner.getId(), new ItemDto(null, "Item", "Description", true, null));
    }

    @Test
    void createBooking() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        BookingDto result = bookingService.create(booker.getId(), bookingDto);

        assertThat(result.getId(), notNullValue());
        assertThat(result.getStatus(), equalTo(BookingStatus.WAITING));
        assertThat(result.getItem().getId(), equalTo(item.getId()));
        assertThat(result.getBooker().getId(), equalTo(booker.getId()));
    }

    @Test
    void createBookingByOwner() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        assertThrows(NotFoundException.class, () -> bookingService.create(owner.getId(), bookingDto));
    }

    @Test
    void approveBooking() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));
        BookingDto created = bookingService.create(booker.getId(), bookingDto);

        BookingDto result = bookingService.update(owner.getId(), created.getId(), true);

        assertThat(result.getStatus(), equalTo(BookingStatus.APPROVED));
    }

    @Test
    void rejectBooking() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));
        BookingDto created = bookingService.create(booker.getId(), bookingDto);

        BookingDto result = bookingService.update(owner.getId(), created.getId(), false);

        assertThat(result.getStatus(), equalTo(BookingStatus.REJECTED));
    }

    @Test
    void getAllByBooker() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));
        bookingService.create(booker.getId(), bookingDto);

        List<BookingDto> result = bookingService.getAllByBooker(booker.getId(), BookingState.ALL);

        assertThat(result, hasSize(1));
    }

    @Test
    void getAllByOwner() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));
        bookingService.create(booker.getId(), bookingDto);

        List<BookingDto> result = bookingService.getAllByOwner(owner.getId(), BookingState.ALL);

        assertThat(result, hasSize(1));
    }

    @Test
    void getAllByBookerFuture() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));
        bookingService.create(booker.getId(), bookingDto);

        List<BookingDto> result = bookingService.getAllByBooker(booker.getId(), BookingState.FUTURE);

        assertThat(result, hasSize(1));
    }

    @Test
    void getAllByBookerWaiting() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));
        bookingService.create(booker.getId(), bookingDto);

        List<BookingDto> result = bookingService.getAllByBooker(booker.getId(), BookingState.WAITING);

        assertThat(result, hasSize(1));
    }

    @Test
    void getAllByBookerRejected() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));
        BookingDto created = bookingService.create(booker.getId(), bookingDto);
        bookingService.update(owner.getId(), created.getId(), false);

        List<BookingDto> result = bookingService.getAllByBooker(booker.getId(), BookingState.REJECTED);

        assertThat(result, hasSize(1));
    }

    @Test
    void getAllByBookerCurrent() {
        List<BookingDto> result = bookingService.getAllByBooker(booker.getId(), BookingState.CURRENT);

        assertThat(result, empty());
    }

    @Test
    void getAllByBookerPast() {
        List<BookingDto> result = bookingService.getAllByBooker(booker.getId(), BookingState.PAST);

        assertThat(result, empty());
    }

    @Test
    void getAllByOwnerFuture() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));
        bookingService.create(booker.getId(), bookingDto);

        List<BookingDto> result = bookingService.getAllByOwner(owner.getId(), BookingState.FUTURE);

        assertThat(result, hasSize(1));
    }

    @Test
    void getAllByOwnerWaiting() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));
        bookingService.create(booker.getId(), bookingDto);

        List<BookingDto> result = bookingService.getAllByOwner(owner.getId(), BookingState.WAITING);

        assertThat(result, hasSize(1));
    }

    @Test
    void getAllByOwnerRejected() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));
        BookingDto created = bookingService.create(booker.getId(), bookingDto);
        bookingService.update(owner.getId(), created.getId(), false);

        List<BookingDto> result = bookingService.getAllByOwner(owner.getId(), BookingState.REJECTED);

        assertThat(result, hasSize(1));
    }

    @Test
    void getAllByOwnerCurrent() {
        List<BookingDto> result = bookingService.getAllByOwner(owner.getId(), BookingState.CURRENT);

        assertThat(result, empty());
    }

    @Test
    void getAllByOwnerPast() {
        List<BookingDto> result = bookingService.getAllByOwner(owner.getId(), BookingState.PAST);

        assertThat(result, empty());
    }

    @Test
    void getBookingById() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));
        BookingDto created = bookingService.create(booker.getId(), bookingDto);

        BookingDto result = bookingService.getById(booker.getId(), created.getId());

        assertThat(result.getId(), equalTo(created.getId()));
    }

    @Test
    void getBookingByIdNotFound() {
        assertThrows(NotFoundException.class, () -> bookingService.getById(booker.getId(), 999L));
    }
}
