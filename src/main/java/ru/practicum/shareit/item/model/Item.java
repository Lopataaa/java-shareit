package ru.practicum.shareit.item.model;

import jakarta.persistence.*;
import lombok.*;
//import ru.practicum.shareit.booking.Booking;
//
//import java.util.List;

@Entity
@Table(name = "items")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_available", nullable = false)
    private Boolean available;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "request_id")
    private Long requestId;

//    @OneToMany(mappedBy = "item", fetch = FetchType.LAZY)
//    private List<Booking> bookings;
}