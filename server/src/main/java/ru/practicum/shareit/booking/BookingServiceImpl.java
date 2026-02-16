package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    private static final Sort SORT_BY_START_DESC = Sort.by(Sort.Direction.DESC, "start");

    @Override
    public BookingDto create(Long userId, BookingDto bookingDto) {
        User booker = getUserOrThrow(userId);
        Item item = getItemOrThrow(bookingDto.getItemId());

        if (!item.getAvailable()) {
            throw new IllegalArgumentException("Item is not available for booking");
        }

        if (item.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Owner cannot book his own item");
        }

        if (bookingDto.getEnd().isBefore(bookingDto.getStart())
                || bookingDto.getEnd().equals(bookingDto.getStart())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        Booking booking = new Booking();
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        Booking savedBooking = bookingRepository.save(booking);
        return BookingMapper.toDto(savedBooking);
    }

    @Override
    public BookingDto update(Long userId, Long bookingId, Boolean approved) {
        Booking booking = getBookingOrThrow(bookingId);

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Only owner can approve or reject booking");
        }

        if (!booking.getStatus().equals(BookingStatus.WAITING)) {
            throw new IllegalArgumentException("Booking status cannot be changed");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking updatedBooking = bookingRepository.save(booking);
        return BookingMapper.toDto(updatedBooking);
    }

    @Override
    public BookingDto getById(Long userId, Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);

        if (!booking.getBooker().getId().equals(userId)
                && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("Only booker or owner can view booking");
        }

        return BookingMapper.toDto(booking);
    }

    @Override
    public List<BookingDto> getAllByBooker(Long userId, BookingState state) {
        getUserOrThrow(userId);
        List<Booking> bookings;

        switch (state) {
            case CURRENT:
                bookings = bookingRepository.findByBookerIdAndStartIsBeforeAndEndIsAfter(
                        userId, LocalDateTime.now(), LocalDateTime.now(), SORT_BY_START_DESC);
                break;
            case PAST:
                bookings = bookingRepository.findByBookerIdAndEndIsBefore(
                        userId, LocalDateTime.now(), SORT_BY_START_DESC);
                break;
            case FUTURE:
                bookings = bookingRepository.findByBookerIdAndStartIsAfter(
                        userId, LocalDateTime.now(), SORT_BY_START_DESC);
                break;
            case WAITING:
                bookings = bookingRepository.findByBookerIdAndStatus(
                        userId, BookingStatus.WAITING, SORT_BY_START_DESC);
                break;
            case REJECTED:
                bookings = bookingRepository.findByBookerIdAndStatus(
                        userId, BookingStatus.REJECTED, SORT_BY_START_DESC);
                break;
            default:
                bookings = bookingRepository.findByBookerId(userId, SORT_BY_START_DESC);
        }

        return bookings.stream()
                .map(BookingMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getAllByOwner(Long userId, BookingState state) {
        getUserOrThrow(userId);
        List<Booking> bookings;

        switch (state) {
            case CURRENT:
                bookings = bookingRepository.findByItemOwnerIdAndStartIsBeforeAndEndIsAfter(
                        userId, LocalDateTime.now(), LocalDateTime.now(), SORT_BY_START_DESC);
                break;
            case PAST:
                bookings = bookingRepository.findByItemOwnerIdAndEndIsBefore(
                        userId, LocalDateTime.now(), SORT_BY_START_DESC);
                break;
            case FUTURE:
                bookings = bookingRepository.findByItemOwnerIdAndStartIsAfter(
                        userId, LocalDateTime.now(), SORT_BY_START_DESC);
                break;
            case WAITING:
                bookings = bookingRepository.findByItemOwnerIdAndStatus(
                        userId, BookingStatus.WAITING, SORT_BY_START_DESC);
                break;
            case REJECTED:
                bookings = bookingRepository.findByItemOwnerIdAndStatus(
                        userId, BookingStatus.REJECTED, SORT_BY_START_DESC);
                break;
            default:
                bookings = bookingRepository.findByItemOwnerId(userId, SORT_BY_START_DESC);
        }

        return bookings.stream()
                .map(BookingMapper::toDto)
                .collect(Collectors.toList());
    }

    private Booking getBookingOrThrow(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking with id " + id + " not found"));
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User with id " + id + " not found"));
    }

    private Item getItemOrThrow(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Item with id " + id + " not found"));
    }
}
