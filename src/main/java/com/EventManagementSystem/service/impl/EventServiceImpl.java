package com.EventManagementSystem.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.EventManagementSystem.exception.EventNotFoundException;
import com.EventManagementSystem.exception.ValidationException;
import com.EventManagementSystem.model.Event;
import com.EventManagementSystem.model.EventStatus;
import com.EventManagementSystem.repository.EventRepository;
import com.EventManagementSystem.service.EventService;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    @Autowired
    public EventServiceImpl(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public Event createEvent(long organizerId, long categoryId, long venueId, String eventName, String description,
                              LocalDate eventDate, LocalTime eventTime, int totalSeats, BigDecimal ticketPrice) {

        validateEventFields(eventName, eventDate, totalSeats, ticketPrice);

        Event event = new Event();
        event.setOrganizerId(organizerId);
        event.setCategoryId(categoryId);
        event.setVenueId(venueId);
        event.setEventName(eventName);
        event.setDescription(description);
        event.setEventDate(eventDate);
        event.setEventTime(eventTime);
        event.setTotalSeats(totalSeats);
        event.setAvailableSeats(totalSeats);
        event.setTicketPrice(ticketPrice);
        event.setStatus(EventStatus.UPCOMING);

        return eventRepository.save(event);
    }

    @Override
    public Event getEventById(long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id " + eventId));
        applyDerivedStatus(event);
        return event;
    }

    @Override
    public List<Event> listAllEvents() {
        List<Event> events = eventRepository.findAllByOrderByEventDateAscEventTimeAsc();
        events.forEach(this::applyDerivedStatus);
        return events;
    }

    @Override
    public List<Event> listEventsByOrganizer(long organizerId) {
        List<Event> events = eventRepository.findByOrganizerIdOrderByEventDate(organizerId);
        events.forEach(this::applyDerivedStatus);
        return events;
    }

    @Override
    public List<Event> listEventsByVenue(long venueId) {
        List<Event> events = eventRepository.findByVenueIdOrderByEventDate(venueId);
        events.forEach(this::applyDerivedStatus);
        return events;
    }

    @Override
    public List<Event> searchEvents(String keyword) {
        List<Event> events = eventRepository.findByEventNameContainingIgnoreCaseOrderByEventDate(keyword);
        events.forEach(this::applyDerivedStatus);
        return events;
    }

    @Override
    public void updateEvent(long eventId, long requestingOrganizerId, String eventName, String description,
                             LocalDate eventDate, LocalTime eventTime, int totalSeats, BigDecimal ticketPrice) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id " + eventId));

        if (event.getOrganizerId() != requestingOrganizerId) {
            throw new ValidationException("You can only update events that you created");
        }

        validateEventFields(eventName, eventDate, totalSeats, ticketPrice);

        int seatsAlreadyBooked = event.getTotalSeats() - event.getAvailableSeats();
        if (totalSeats < seatsAlreadyBooked) {
            throw new ValidationException("Total seats cannot be less than seats already booked ("
                    + seatsAlreadyBooked + ")");
        }

        event.setEventName(eventName);
        event.setDescription(description);
        event.setEventDate(eventDate);
        event.setEventTime(eventTime);
        event.setTotalSeats(totalSeats);
        event.setAvailableSeats(totalSeats - seatsAlreadyBooked);
        event.setTicketPrice(ticketPrice);

        eventRepository.save(event);
    }

    @Override
    public void updateEventStatus(long eventId, String status) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id " + eventId));
        event.setStatus(EventStatus.valueOf(status));
        eventRepository.save(event);
    }

    @Override
    public void deleteEvent(long eventId, long requestingOrganizerId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id " + eventId));

        if (event.getOrganizerId() != requestingOrganizerId) {
            throw new ValidationException("You can only delete events that you created");
        }
        if (event.getAvailableSeats() != event.getTotalSeats()) {
            throw new ValidationException("Cannot delete an event that already has bookings, cancel it instead");
        }

        eventRepository.delete(event);
    }

    @Override
    public long countAllEvents() {
        return eventRepository.count();
    }

    @Override
    @Transactional
    public int syncEventStatuses() {
        return eventRepository.syncStatuses();
    }

    @Override
    public List<Event> listActiveEvents() {
        return eventRepository.findActiveEvents(
                EventStatus.COMPLETED,
                EventStatus.CANCELLED
        );
    }

    private void applyDerivedStatus(Event event) {
        event.setStatus(EventStatus.deriveStatus(event.getEventDate(), event.getStatus()));
    }

    private void validateEventFields(String eventName, LocalDate eventDate, int totalSeats, BigDecimal ticketPrice) {
        if (eventName == null || eventName.trim().isEmpty()) {
            throw new ValidationException("Event name cannot be empty");
        }
        if (eventDate == null || eventDate.isBefore(LocalDate.now())) {
            throw new ValidationException("Event date cannot be in the past");
        }
        if (totalSeats <= 0) {
            throw new ValidationException("Total seats must be greater than zero");
        }
        if (ticketPrice == null || ticketPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Ticket price cannot be negative");
        }
    }
}
