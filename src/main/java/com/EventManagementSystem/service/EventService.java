package com.EventManagementSystem.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.EventManagementSystem.model.Event;

public interface EventService {

    Event createEvent(long organizerId, Long categoryId, long venueId, String eventName, String description,
                       LocalDate eventDate, LocalTime eventTime, int totalSeats, BigDecimal ticketPrice);

    Event getEventById(long eventId);

    List<Event> listAllEvents();

    List<Event> listEventsByOrganizer(long organizerId);

    List<Event> listEventsByVenue(long venueId);

    List<Event> searchEvents(String keyword);

    void updateEvent(long eventId, long requestingOrganizerId, String eventName, String description,
                      LocalDate eventDate, LocalTime eventTime, int totalSeats, BigDecimal ticketPrice);

    void updateEventStatus(long eventId, String status);

    void deleteEvent(long eventId, long requestingOrganizerId);

    long countAllEvents();

    int syncEventStatuses();

    List<Event> listActiveEvents();
}
