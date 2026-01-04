package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final ItemMapper itemMapper;

    private Item getItemOrThrow(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Item with id " + id + " not found"));
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User with id " + id + " not found"));
    }

    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        User owner = getUserOrThrow(userId);

        ItemRequest request = null;
        if (itemDto.getRequestId() != null) {
            request = itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Request with id " + itemDto.getRequestId() + " not found"));
        }

        Item item = itemMapper.toEntity(itemDto, owner, request);
        Item savedItem = itemRepository.save(item);
        return itemMapper.toDto(savedItem);
    }

    @Override
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        Item existingItem = getItemOrThrow(itemId);

        if (!userId.equals(existingItem.getOwner().getId())) {
            throw new NotFoundException("User with id " + userId + " is not the owner of item " + itemId);
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
        return itemMapper.toDto(updatedItem);
    }

    @Override
    public ItemDto getById(Long itemId) {
        Item item = getItemOrThrow(itemId);
        return itemMapper.toDto(item);
    }

    @Override
    public List<ItemDto> getAllByOwner(Long userId) {
        return itemMapper.toDto(itemRepository.findByOwnerId(userId));
    }

    @Override
    public List<ItemDto> search(String text) {
        return itemMapper.toDto(itemRepository.search(text));
    }
}

