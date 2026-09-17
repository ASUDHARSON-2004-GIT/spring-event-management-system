package com.EventManagementSystem.repository;

import java.util.List;
import java.util.Optional;

import com.EventManagementSystem.model.EventStatus;
import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.EventManagementSystem.model.Event;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findAllByOrderByEventDateAscEventTimeAsc();

    List<Event> findByOrganizerIdOrderByEventDate(long organizerId);

    List<Event> findByVenueIdOrderByEventDate(long venueId);

    List<Event> findByEventNameContainingIgnoreCaseOrderByEventDate(String keyword);

    List<Event> findByStatusNot(EventStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Event e where e.eventId = :eventId")
    Optional<Event> findByIdForUpdate(@Param("eventId") long eventId);

    @Query("""
    SELECT e FROM Event e
    WHERE e.status <> :completed
    AND e.status <> :cancelled
""")
    List<Event> findActiveEvents(
            @Param("completed") EventStatus completed,
            @Param("cancelled") EventStatus cancelled
    );

    @Modifying
    @Query("update Event e set e.status = case "
            + "when e.eventDate = CURRENT_DATE then com.EventManagementSystem.model.EventStatus.ONGOING "
            + "when e.eventDate < CURRENT_DATE then com.EventManagementSystem.model.EventStatus.COMPLETED "
            + "else com.EventManagementSystem.model.EventStatus.UPCOMING end "
            + "where e.status <> com.EventManagementSystem.model.EventStatus.CANCELLED")
    int syncStatuses();
}
