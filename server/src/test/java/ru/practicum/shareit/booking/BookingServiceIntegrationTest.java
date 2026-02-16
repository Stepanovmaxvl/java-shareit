package ru.practicum.shareit.booking;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

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
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BookingServiceIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private EntityManager em;

    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@email.com");
        em.persist(owner);

        booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker@email.com");
        em.persist(booker);

        item = new Item();
        item.setName("Item");
        item.setDescription("Description");
        item.setAvailable(true);
        item.setOwner(owner);
        em.persist(item);

        em.flush();
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

        assertThrows(ForbiddenException.class,
                () -> bookingService.create(owner.getId(), bookingDto));
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

        List<BookingDto> result = bookingService.getAllByBooker(
                booker.getId(), BookingState.WAITING);

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

        List<BookingDto> result = bookingService.getAllByBooker(
                booker.getId(), BookingState.REJECTED);

        assertThat(result, hasSize(1));
    }

    @Test
    void getAllByBookerCurrent() {
        List<BookingDto> result = bookingService.getAllByBooker(
                booker.getId(), BookingState.CURRENT);

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

        List<BookingDto> result = bookingService.getAllByOwner(
                owner.getId(), BookingState.WAITING);

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

        List<BookingDto> result = bookingService.getAllByOwner(
                owner.getId(), BookingState.REJECTED);

        assertThat(result, hasSize(1));
    }

    @Test
    void getAllByOwnerCurrent() {
        List<BookingDto> result = bookingService.getAllByOwner(
                owner.getId(), BookingState.CURRENT);

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
        assertThrows(NotFoundException.class,
                () -> bookingService.getById(booker.getId(), 999L));
    }
}
