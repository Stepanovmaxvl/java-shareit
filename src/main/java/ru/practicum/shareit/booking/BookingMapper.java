package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserShortDto;

@Component
@RequiredArgsConstructor
public class BookingMapper {
    private final ItemMapper itemMapper;
    private final UserMapper userMapper;

    public BookingDto toDto(Booking booking) {
        BookingDto dto = new BookingDto();
        dto.setId(booking.getId());
        dto.setStart(booking.getStart());
        dto.setEnd(booking.getEnd());
        dto.setItemId(booking.getItem().getId());

        ItemShortDto itemDto = itemMapper.toShortDto(booking.getItem());
        dto.setItem(itemDto);

        UserShortDto bookerDto = userMapper.toShortDto(booking.getBooker());
        dto.setBooker(bookerDto);

        dto.setStatus(booking.getStatus());
        return dto;
    }
}