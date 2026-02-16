package ru.practicum.shareit.item;

import ru.practicum.shareit.booking.dto.BookingInfoDto;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public final class ItemMapper {

    private ItemMapper() {
    }

    public static ItemDto toDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getRequest() != null ? item.getRequest().getId() : null
        );
    }

    public static List<ItemDto> toDto(List<Item> items) {
        return items.stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }

    public static ItemWithBookingsDto toDtoWithBookings(Item item, BookingInfoDto lastBooking,
                                                        BookingInfoDto nextBooking,
                                                        List<CommentDto> comments) {
        ItemWithBookingsDto dto = new ItemWithBookingsDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.getAvailable());
        dto.setRequestId(item.getRequest() != null ? item.getRequest().getId() : null);
        dto.setLastBooking(lastBooking);
        dto.setNextBooking(nextBooking);
        dto.setComments(comments);
        return dto;
    }

    public static Item toEntity(ItemDto itemDto, User owner, ItemRequest request) {
        Item item = new Item();
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        item.setOwner(owner);
        item.setRequest(request);
        return item;
    }

    public static ItemShortDto toShortDto(Item item) {
        return new ItemShortDto(
                item.getId(),
                item.getName()
        );
    }

    public static CommentDto toCommentDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setText(comment.getText());
        dto.setAuthorName(comment.getAuthor().getName());
        dto.setCreated(comment.getCreated());
        return dto;
    }

    public static List<CommentDto> toCommentDto(List<Comment> comments) {
        return comments.stream()
                .map(ItemMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    public static Comment toCommentEntity(CommentDto dto, Item item, User author) {
        Comment comment = new Comment();
        comment.setText(dto.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());
        return comment;
    }
}
