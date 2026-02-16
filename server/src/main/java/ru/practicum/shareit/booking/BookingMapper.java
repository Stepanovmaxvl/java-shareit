package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.user.dto.UserShortDto;

public final class BookingMapper {

    private BookingMapper() {
    }

    public static BookingDto toDto(Booking booking) {
        BookingDto dto = new BookingDto();
        dto.setId(booking.getId());
        dto.setStart(booking.getStart());
        dto.setEnd(booking.getEnd());
        dto.setItemId(booking.getItem().getId());
        dto.setItem(new ItemShortDto(booking.getItem().getId(), booking.getItem().getName()));
        dto.setBooker(new UserShortDto(booking.getBooker().getId()));
        dto.setStatus(booking.getStatus());
        return dto;
    }
}
