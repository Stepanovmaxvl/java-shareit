package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;

import java.util.List;

public interface BookingService {
    BookingDto create(Long userId, BookingDto bookingDto);

    BookingDto update(Long userId, Long bookingId, Boolean approved);

    BookingDto getById(Long userId, Long bookingId);

    List<BookingDto> getAllByBooker(Long userId, BookingState state);

    List<BookingDto> getAllByOwner(Long userId, BookingState state);
}

