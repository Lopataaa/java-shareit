package ru.practicum.shareit.booking;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b WHERE b.bookerId = :bookerId")
    List<Booking> findByBookerId(@Param("bookerId") Long bookerId, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.bookerId = :bookerId AND b.status = :status")
    List<Booking> findByBookerIdAndStatus(@Param("bookerId") Long bookerId, @Param("status") BookingStatus status, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.bookerId = :bookerId AND b.end < :now")
    List<Booking> findByBookerIdAndEndBefore(@Param("bookerId") Long bookerId, @Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.bookerId = :bookerId AND b.start > :now")
    List<Booking> findByBookerIdAndStartAfter(@Param("bookerId") Long bookerId, @Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.bookerId = :bookerId AND b.start < :now AND b.end > :now")
    List<Booking> findByBookerIdAndStartBeforeAndEndAfter(@Param("bookerId") Long bookerId, @Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT b FROM Booking b, Item i WHERE b.itemId = i.id AND i.ownerId = :ownerId")
    List<Booking> findByItemOwnerId(@Param("ownerId") Long ownerId, Pageable pageable);

    @Query("SELECT b FROM Booking b, Item i WHERE b.itemId = i.id AND i.ownerId = :ownerId AND b.status = :status")
    List<Booking> findByItemOwnerIdAndStatus(@Param("ownerId") Long ownerId, @Param("status") BookingStatus status, Pageable pageable);

    @Query("SELECT b FROM Booking b, Item i WHERE b.itemId = i.id AND i.ownerId = :ownerId AND b.end < :now")
    List<Booking> findByItemOwnerIdAndEndBefore(@Param("ownerId") Long ownerId, @Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT b FROM Booking b, Item i WHERE b.itemId = i.id AND i.ownerId = :ownerId AND b.start > :now")
    List<Booking> findByItemOwnerIdAndStartAfter(@Param("ownerId") Long ownerId, @Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT b FROM Booking b, Item i WHERE b.itemId = i.id " + "AND i.ownerId = :ownerId AND b.start < :now AND b.end > :now")
    List<Booking> findByItemOwnerIdAndStartBeforeAndEndAfter(@Param("ownerId") Long ownerId, @Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT b FROM Booking b, Item i WHERE b.itemId = i.id " + "AND b.itemId = :itemId AND b.bookerId = :bookerId AND b.end < :now AND b.status = 'APPROVED'")
    List<Booking> findCompletedBookingsForItemAndUser(@Param("itemId") Long itemId, @Param("bookerId") Long bookerId, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.itemId = :itemId AND b.status = 'APPROVED' " + "AND b.start < :now ORDER BY b.start DESC")
    Optional<Booking> findLastBookingForItem(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.itemId = :itemId AND b.status = 'APPROVED' " + "AND b.start > :now ORDER BY b.start ASC")
    Optional<Booking> findNextBookingForItem(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(b) > 0 FROM Booking b, Item i WHERE b.itemId = i.id " + "AND b.itemId = :itemId AND b.bookerId = :bookerId AND b.end < :now AND b.status = 'APPROVED'")
    boolean hasUserBookedItem(@Param("itemId") Long itemId, @Param("bookerId") Long bookerId, @Param("now") LocalDateTime now);

    Optional<Booking> findByIdAndBookerId(Long id, Long bookerId);

    @Query("SELECT b FROM Booking b, Item i WHERE b.itemId = i.id AND b.id = :id AND i.ownerId = :ownerId")
    Optional<Booking> findByIdAndItemOwnerId(@Param("id") Long id, @Param("ownerId") Long ownerId);

}