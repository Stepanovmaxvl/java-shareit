package ru.practicum.shareit.request;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ItemRequestServiceIntegrationTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private EntityManager em;

    private User requestor;
    private User otherUser;

    @BeforeEach
    void setUp() {
        requestor = new User();
        requestor.setName("Requestor");
        requestor.setEmail("requestor@email.com");
        em.persist(requestor);

        otherUser = new User();
        otherUser.setName("Other");
        otherUser.setEmail("other@email.com");
        em.persist(otherUser);

        em.flush();
    }

    @Test
    void createRequest() {
        ItemRequestDto dto = new ItemRequestDto(null, "Need a drill", null, null);
        ItemRequestDto result = itemRequestService.create(requestor.getId(), dto);

        assertThat(result.getId(), notNullValue());
        assertThat(result.getDescription(), equalTo("Need a drill"));
        assertThat(result.getCreated(), notNullValue());
        assertThat(result.getItems(), empty());
    }

    @Test
    void getOwnRequests() {
        itemRequestService.create(requestor.getId(),
                new ItemRequestDto(null, "Need a drill", null, null));
        itemRequestService.create(requestor.getId(),
                new ItemRequestDto(null, "Need a saw", null, null));

        List<ItemRequestDto> result = itemRequestService.getOwn(requestor.getId());

        assertThat(result, hasSize(2));
    }

    @Test
    void getOwnRequestsWithItems() {
        ItemRequestDto request = itemRequestService.create(requestor.getId(),
                new ItemRequestDto(null, "Need a drill", null, null));

        ItemRequest savedRequest = em.find(ItemRequest.class, request.getId());

        Item item = new Item();
        item.setName("Drill");
        item.setDescription("Electric drill");
        item.setAvailable(true);
        item.setOwner(otherUser);
        item.setRequest(savedRequest);
        em.persist(item);
        em.flush();

        List<ItemRequestDto> result = itemRequestService.getOwn(requestor.getId());

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getItems(), hasSize(1));
        assertThat(result.get(0).getItems().get(0).getName(), equalTo("Drill"));
    }

    @Test
    void getAllRequests() {
        itemRequestService.create(requestor.getId(),
                new ItemRequestDto(null, "Need a drill", null, null));

        List<ItemRequestDto> result = itemRequestService.getAll(otherUser.getId());

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getDescription(), equalTo("Need a drill"));
    }

    @Test
    void getAllRequestsExcludesOwn() {
        itemRequestService.create(requestor.getId(),
                new ItemRequestDto(null, "Need a drill", null, null));

        List<ItemRequestDto> result = itemRequestService.getAll(requestor.getId());

        assertThat(result, empty());
    }

    @Test
    void getRequestById() {
        ItemRequestDto created = itemRequestService.create(requestor.getId(),
                new ItemRequestDto(null, "Need a drill", null, null));

        ItemRequestDto result = itemRequestService.getById(
                otherUser.getId(), created.getId());

        assertThat(result.getId(), equalTo(created.getId()));
        assertThat(result.getDescription(), equalTo("Need a drill"));
    }

    @Test
    void getRequestByIdNotFound() {
        assertThrows(NotFoundException.class,
                () -> itemRequestService.getById(requestor.getId(), 999L));
    }
}
