package com.EventManagementSystem.controller;

import java.time.LocalDate;
import java.util.ArrayList;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.EventManagementSystem.dto.BookingRequest;
import com.EventManagementSystem.dto.PaymentRequest;
import com.EventManagementSystem.dto.ProfileUpdateRequest;
import com.EventManagementSystem.model.Booking;
import com.EventManagementSystem.model.Event;
import com.EventManagementSystem.model.User;
import com.EventManagementSystem.service.BookingService;
import com.EventManagementSystem.service.EventService;
import com.EventManagementSystem.service.PaymentService;
import com.EventManagementSystem.service.UserService;

@RestController
@RequestMapping("/api/customer/{customerId}")
public class CustomerController {

    private final EventService eventService;
    private final BookingService bookingService;
    private final PaymentService paymentService;
    private final UserService userService;

    @Autowired
    public CustomerController(EventService eventService, BookingService bookingService,
                               PaymentService paymentService, UserService userService) {
        this.eventService = eventService;
        this.bookingService = bookingService;
        this.paymentService = paymentService;
        this.userService = userService;
    }

    @GetMapping("/events")
    public List<Event> browseEvents() {
        return upcomingOnly(eventService.listAllEvents());
    }

    @GetMapping("/events/search")
    public List<Event> searchEvents(@RequestParam String keyword) {
        return upcomingOnly(eventService.searchEvents(keyword));
    }

    @PostMapping("/bookings")
    public ResponseEntity<Long> bookEvent(
            @PathVariable long customerId, @RequestBody BookingRequest request) {

        long bookingId = bookingService.createBooking(customerId, request.getEventId(), request.getSeats());
        return ResponseEntity.ok(bookingId);
    }

    @PostMapping("/bookings/{bookingId}/payment")
    public ResponseEntity<Void> payForBooking(
            @PathVariable long bookingId, @RequestBody PaymentRequest request) {
        paymentService.confirmPayment(bookingId, request.getMethod());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/bookings")
    public List<Booking> viewMyBookings(@PathVariable long customerId) {
        return bookingService.listBookingsForCustomer(customerId);
    }

    @DeleteMapping("/bookings/{bookingId}")
    public ResponseEntity<Void> cancelBooking(
            @PathVariable long customerId, @PathVariable long bookingId) {
        bookingService.cancelBooking(bookingId, customerId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(
            @PathVariable long customerId, @RequestBody ProfileUpdateRequest request) {
        userService.updateProfile(customerId, request.getName(), request.getPhone());
        return ResponseEntity.ok(userService.getProfile(customerId));
    }

    private List<Event> upcomingOnly(List<Event> events) {
        LocalDate today = LocalDate.now();
        List<Event> upcoming = new ArrayList<>();

        for (Event event : events) {
            if (!event.getEventDate().isBefore(today)) {
                upcoming.add(event);
            }
        }

        return upcoming;
    }
}
