package ru.practicum.shareit.request;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class ItemRequestRepository {
    private final Map<Long, ItemRequest> requests = new HashMap<>();
    private long nextId = 1L;

    public ItemRequest save(ItemRequest request) {
        if (request.getId() == null) {
            request.setId(nextId++);
        }
        requests.put(request.getId(), request);
        return request;
    }

    public Optional<ItemRequest> findById(Long id) {
        return Optional.ofNullable(requests.get(id));
    }
}

