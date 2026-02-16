package ru.practicum.shareit.request;

import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

public final class ItemRequestMapper {

    private ItemRequestMapper() {
    }

    public static ItemRequestDto toDto(ItemRequest request, List<Item> items) {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(request.getId());
        dto.setDescription(request.getDescription());
        dto.setCreated(request.getCreated());
        List<ItemDto> itemDtos = ItemMapper.toDto(items);
        dto.setItems(itemDtos);
        return dto;
    }

    public static ItemRequest toEntity(ItemRequestDto dto, User requestor) {
        ItemRequest request = new ItemRequest();
        request.setDescription(dto.getDescription());
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());
        return request;
    }
}
