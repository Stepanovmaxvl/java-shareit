package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingInfoDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    private static final Sort SORT_BY_START_DESC = Sort.by(Sort.Direction.DESC, "start");

    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        User owner = getUserOrThrow(userId);

        ItemRequest request = null;
        if (itemDto.getRequestId() != null) {
            request = itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException(
                            "Request with id " + itemDto.getRequestId() + " not found"));
        }

        Item item = ItemMapper.toEntity(itemDto, owner, request);
        Item savedItem = itemRepository.save(item);
        return ItemMapper.toDto(savedItem);
    }

    @Override
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        Item existingItem = getItemOrThrow(itemId);

        if (!userId.equals(existingItem.getOwner().getId())) {
            throw new NotFoundException(
                    "User with id " + userId + " is not the owner of item " + itemId);
        }

        if (itemDto.getName() != null) {
            existingItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }

        Item updatedItem = itemRepository.save(existingItem);
        return ItemMapper.toDto(updatedItem);
    }

    @Override
    public ItemWithBookingsDto getById(Long userId, Long itemId) {
        getUserOrThrow(userId);
        Item item = getItemOrThrow(itemId);
        List<Comment> comments = commentRepository.findByItemId(itemId);
        List<CommentDto> commentDtos = ItemMapper.toCommentDto(comments);

        BookingInfoDto lastBooking = null;
        BookingInfoDto nextBooking = null;

        if (item.getOwner().getId().equals(userId)) {
            List<Booking> bookings = bookingRepository.findByItemIdAndStatus(
                    itemId, BookingStatus.APPROVED, SORT_BY_START_DESC);
            LocalDateTime now = LocalDateTime.now();

            for (Booking booking : bookings) {
                if (lastBooking == null && booking.getEnd().isBefore(now)) {
                    lastBooking = new BookingInfoDto(booking.getId(),
                            booking.getBooker().getId(),
                            booking.getStart(), booking.getEnd());
                }
                if (nextBooking == null && booking.getStart().isAfter(now)) {
                    nextBooking = new BookingInfoDto(booking.getId(),
                            booking.getBooker().getId(),
                            booking.getStart(), booking.getEnd());
                    break;
                }
            }
        }

        return ItemMapper.toDtoWithBookings(item, lastBooking, nextBooking, commentDtos);
    }

    @Override
    public List<ItemWithBookingsDto> getAllByOwner(Long userId) {
        getUserOrThrow(userId);
        List<Item> items = itemRepository.findByOwnerIdOrderById(userId);

        if (items.isEmpty()) {
            return List.of();
        }

        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .collect(Collectors.toList());

        LocalDateTime now = LocalDateTime.now();

        Map<Long, List<Booking>> bookingsByItem = bookingRepository
                .findByItemIdInAndStatus(itemIds, BookingStatus.APPROVED, SORT_BY_START_DESC)
                .stream()
                .collect(Collectors.groupingBy(b -> b.getItem().getId()));

        Map<Long, List<Comment>> commentsByItem = commentRepository
                .findByItemIdIn(itemIds)
                .stream()
                .collect(Collectors.groupingBy(c -> c.getItem().getId()));

        return items.stream().map(item -> {
            List<Booking> bookings = bookingsByItem.getOrDefault(
                    item.getId(), Collections.emptyList());

            BookingInfoDto lastBooking = null;
            BookingInfoDto nextBooking = null;

            for (Booking booking : bookings) {
                if (lastBooking == null && booking.getEnd().isBefore(now)) {
                    lastBooking = new BookingInfoDto(booking.getId(),
                            booking.getBooker().getId(),
                            booking.getStart(), booking.getEnd());
                }
                if (nextBooking == null && booking.getStart().isAfter(now)) {
                    nextBooking = new BookingInfoDto(booking.getId(),
                            booking.getBooker().getId(),
                            booking.getStart(), booking.getEnd());
                    break;
                }
            }

            List<CommentDto> commentDtos = ItemMapper.toCommentDto(
                    commentsByItem.getOrDefault(item.getId(), Collections.emptyList()));

            return ItemMapper.toDtoWithBookings(item, lastBooking, nextBooking, commentDtos);
        }).collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return ItemMapper.toDto(itemRepository.search(text));
    }

    @Override
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        User author = getUserOrThrow(userId);
        Item item = getItemOrThrow(itemId);

        List<Booking> bookings = bookingRepository.findByItemIdAndBookerId(
                itemId, userId, SORT_BY_START_DESC);
        boolean hasApprovedBooking = bookings.stream()
                .anyMatch(b -> b.getStatus() == BookingStatus.APPROVED
                        && b.getEnd().isBefore(LocalDateTime.now()));

        if (!hasApprovedBooking) {
            throw new IllegalArgumentException(
                    "User can only comment on items they have booked and used");
        }

        Comment comment = ItemMapper.toCommentEntity(commentDto, item, author);
        Comment savedComment = commentRepository.save(comment);
        return ItemMapper.toCommentDto(savedComment);
    }

    private Item getItemOrThrow(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Item with id " + id + " not found"));
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User with id " + id + " not found"));
    }
}
