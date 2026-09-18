package com.EventManagementSystem.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.EventManagementSystem.dto.EventRequest;
import com.EventManagementSystem.dto.VenueRequest;
import com.EventManagementSystem.model.Account;
import com.EventManagementSystem.model.Booking;
import com.EventManagementSystem.model.Event;
import com.EventManagementSystem.model.Venue;
import com.EventManagementSystem.service.AccountService;
import com.EventManagementSystem.service.BookingService;
import com.EventManagementSystem.service.EventService;
import com.EventManagementSystem.service.VenueService;

@RestController
@RequestMapping("/api/organizer/{organizerId}")
public class OrganizerController {

    private final EventService eventService;
    private final BookingService bookingService;
    private final AccountService accountService;
    private final VenueService venueService;

    @Autowired
    public OrganizerController(EventService eventService, BookingService bookingService,
                                AccountService accountService, VenueService venueService) {
        this.eventService = eventService;
        this.bookingService = bookingService;
        this.accountService = accountService;
        this.venueService = venueService;
    }

    @PostMapping("/event")
    public ResponseEntity<Event> createEvent(@PathVariable long organizerId, @RequestBody EventRequest request) {
        
        long venueId;
        if (request.getNewVenue() != null) {
            VenueRequest v = request.getNewVenue();
            Venue venue = venueService.createVenue(v.getName(), v.getDoorNo(), v.getStreet(), v.getCity(), v.getState());
            venueId = venue.getId();
        } else {
            venueId = request.getVenueId();
        }
        if(request.getVenueId() == null && request.getNewVenue() == null){
            throw new RuntimeException("Venu id or new Venu needed to create event");
        }

        Event event = eventService.createEvent(organizerId, request.getCategoryId(), venueId, request.getEventName(),
                request.getDescription(), request.getEventDate(), request.getEventTime(),
                request.getTotalSeats(), request.getTicketPrice());

        return ResponseEntity.ok(event);
    }

    @GetMapping("/events")
    public List<Event> viewMyEvents(@PathVariable long organizerId) {
        List<Event> events = eventService.listEventsByOrganizer(organizerId);
        for (Event event : events) {
            BigDecimal revenue = bookingService.getRevenueForEvent(event.getEventId());
            event.setRevenue(revenue);
        }
        return events;
    }

    @GetMapping("/venues")
    public List<Venue> getAllVenues(){
        return venueService.listVenues();
    }

    @PutMapping("/events/{eventId}")
    public ResponseEntity<Void> updateEvent(@PathVariable long organizerId, @PathVariable long eventId,
                                             @RequestBody EventRequest request) {

        if (request.getNewVenue() != null) {
            Event existing = eventService.getEventById(eventId);
            VenueRequest v = request.getNewVenue();
            venueService.updateVenue(existing.getVenueId(), v.getName(), v.getDoorNo(), v.getStreet(),
                    v.getCity(), v.getState());
        }

        eventService.updateEvent(eventId, organizerId, request.getEventName(), request.getDescription(),
                request.getEventDate(), request.getEventTime(), request.getTotalSeats(), request.getTicketPrice());

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/events/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable long organizerId, @PathVariable long eventId) {
        eventService.deleteEvent(eventId, organizerId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/events/{eventId}/bookings")
    public List<Booking> viewBookingsForEvent(@PathVariable long eventId) {
        return bookingService.listBookingsForEvent(eventId);
    }

    @GetMapping("/account")
    public ResponseEntity<Account> viewAccountBalance(@PathVariable long organizerId) {
        Account account = accountService.getAccountForOrganizer(organizerId);
        if (account == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(account);
    }
}
