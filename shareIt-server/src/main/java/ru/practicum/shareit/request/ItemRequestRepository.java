package ru.practicum.shareit.request;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    // Свои запросы
    List<ItemRequest> findByRequestorIdOrderByCreatedDesc(Long requestorId);

    // Чужие запросы
    @Query("SELECT r FROM ItemRequest r WHERE r.requestor.id <> :userId ORDER BY r.created DESC")
    List<ItemRequest> findOtherRequests(@Param("userId") Long userId, Pageable pageable);

    boolean existsById(Long id);
}
