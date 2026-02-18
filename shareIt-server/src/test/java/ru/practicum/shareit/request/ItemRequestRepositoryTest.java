package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Интеграционные тесты репозитория запросов вещей")
class ItemRequestRepositoryTest {

    private static final String USER1_NAME = "User 1";
    private static final String USER1_EMAIL = "user1@test.com";
    private static final String USER2_NAME = "User 2";
    private static final String USER2_EMAIL = "user2@test.com";
    private static final String USER3_NAME = "User 3";
    private static final String USER3_EMAIL = "user3@test.com";
    private static final String REQUEST1_DESC = "Request 1 from user1";
    private static final String REQUEST2_DESC = "Request 2 from user1";
    private static final String REQUEST3_DESC = "Request 3 from user2";
    private static final long NON_EXISTENT_ID = 999L;
    private static final int PAGE_SIZE_10 = 10;
    private static final int PAGE_SIZE_1 = 1;

    @Autowired
    private ItemRequestRepository requestRepository;

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;
    private ItemRequest request1;
    private ItemRequest request2;
    private ItemRequest request3;

    @BeforeEach
    void setUp() {
        // given - подготовка тестовых данных
        user1 = User.builder()
                .name(USER1_NAME)
                .email(USER1_EMAIL)
                .build();
        user1 = userRepository.save(user1);

        user2 = User.builder()
                .name(USER2_NAME)
                .email(USER2_EMAIL)
                .build();
        user2 = userRepository.save(user2);

        request1 = ItemRequest.builder()
                .description(REQUEST1_DESC)
                .requestor(user1)
                .created(LocalDateTime.now().minusDays(2))
                .build();
        request1 = requestRepository.save(request1);

        request2 = ItemRequest.builder()
                .description(REQUEST2_DESC)
                .requestor(user1)
                .created(LocalDateTime.now().minusDays(1))
                .build();
        request2 = requestRepository.save(request2);

        request3 = ItemRequest.builder()
                .description(REQUEST3_DESC)
                .requestor(user2)
                .created(LocalDateTime.now())
                .build();
        request3 = requestRepository.save(request3);
    }

    @Test
    @DisplayName("Поиск по requestorId: должен возвращать запросы пользователя, отсортированные по убыванию даты")
    void findByRequestorIdOrderByCreatedDesc_shouldReturnUserRequestsSorted() {
        // when
        List<ItemRequest> requests = requestRepository.findByRequestorIdOrderByCreatedDesc(user1.getId());

        // then
        assertEquals(2, requests.size());
        assertTrue(requests.get(0).getCreated().isAfter(requests.get(1).getCreated()));
        assertEquals(request2.getId(), requests.get(0).getId());
        assertEquals(request1.getId(), requests.get(1).getId());
    }

    @Test
    @DisplayName("Поиск по requestorId: для пользователя без запросов должен возвращать пустой список")
    void findByRequestorIdOrderByCreatedDesc_shouldReturnEmptyForUserWithoutRequests() {
        // given
        User user3 = User.builder()
                .name(USER3_NAME)
                .email(USER3_EMAIL)
                .build();
        user3 = userRepository.save(user3);

        // when
        List<ItemRequest> requests = requestRepository.findByRequestorIdOrderByCreatedDesc(user3.getId());

        // then
        assertTrue(requests.isEmpty());
    }

    @Test
    @DisplayName("Поиск чужих запросов: должен возвращать запросы от других пользователей")
    void findOtherRequests_shouldReturnRequestsFromOtherUsers() {
        // given
        Pageable pageable = PageRequest.of(0, PAGE_SIZE_10);

        // when
        List<ItemRequest> requests = requestRepository.findOtherRequests(user1.getId(), pageable);

        // then
        assertEquals(1, requests.size());
        assertEquals(user2.getId(), requests.get(0).getRequestor().getId());
        assertEquals(request3.getId(), requests.get(0).getId());
    }

    @Test
    @DisplayName("Поиск чужих запросов: пагинация должна работать корректно")
    void findOtherRequests_shouldRespectPagination() {
        // given
        Pageable pageable = PageRequest.of(0, PAGE_SIZE_1);

        // when
        List<ItemRequest> requests = requestRepository.findOtherRequests(user1.getId(), pageable);

        // then
        assertEquals(1, requests.size());
    }

    @Test
    @DisplayName("existsById: должен возвращать true для существующего запроса")
    void existsById_shouldReturnTrueForExistingRequest() {
        // when
        boolean exists = requestRepository.existsById(request1.getId());

        // then
        assertTrue(exists);
    }

    @Test
    @DisplayName("existsById: должен возвращать false для несуществующего запроса")
    void existsById_shouldReturnFalseForNonExistingRequest() {
        // when
        boolean exists = requestRepository.existsById(NON_EXISTENT_ID);

        // then
        assertFalse(exists);
    }
}