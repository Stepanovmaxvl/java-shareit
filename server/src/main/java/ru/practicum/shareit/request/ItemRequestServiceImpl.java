package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    private static final Sort SORT_BY_CREATED_DESC = Sort.by(Sort.Direction.DESC, "created");

    @Override
    public ItemRequestDto create(Long userId, ItemRequestDto dto) {
        User requestor = getUserOrThrow(userId);
        ItemRequest request = ItemRequestMapper.toEntity(dto, requestor);
        ItemRequest savedRequest = itemRequestRepository.save(request);
        return ItemRequestMapper.toDto(savedRequest, Collections.emptyList());
    }

    @Override
    public List<ItemRequestDto> getOwn(Long userId) {
        getUserOrThrow(userId);
        List<ItemRequest> requests = itemRequestRepository.findByRequestorId(
                userId, SORT_BY_CREATED_DESC);
        return toRequestDtosWithItems(requests);
    }

    @Override
    public List<ItemRequestDto> getAll(Long userId) {
        getUserOrThrow(userId);
        List<ItemRequest> requests = itemRequestRepository.findByRequestorIdNot(
                userId, SORT_BY_CREATED_DESC);
        return toRequestDtosWithItems(requests);
    }

    @Override
    public ItemRequestDto getById(Long userId, Long requestId) {
        getUserOrThrow(userId);
        ItemRequest request = getRequestOrThrow(requestId);
        List<Item> items = itemRepository.findByRequestId(requestId);
        return ItemRequestMapper.toDto(request, items);
    }

    private List<ItemRequestDto> toRequestDtosWithItems(List<ItemRequest> requests) {
        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());

        Map<Long, List<Item>> itemsByRequest = itemRepository
                .findByRequestIdIn(requestIds).stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));

        return requests.stream()
                .map(request -> ItemRequestMapper.toDto(request,
                        itemsByRequest.getOrDefault(request.getId(), Collections.emptyList())))
                .collect(Collectors.toList());
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User with id " + id + " not found"));
    }

    private ItemRequest getRequestOrThrow(Long id) {
        return itemRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Request with id " + id + " not found"));
    }
}
