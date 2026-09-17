package com.EventManagementSystem.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.EventManagementSystem.dto.UserResponse;
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

import com.EventManagementSystem.dto.CategoryRequest;
import com.EventManagementSystem.dto.UserStatusRequest;
import com.EventManagementSystem.model.AccountTransaction;
import com.EventManagementSystem.model.Booking;
import com.EventManagementSystem.model.Category;
import com.EventManagementSystem.model.Event;
import com.EventManagementSystem.model.Role;
import com.EventManagementSystem.model.User;
import com.EventManagementSystem.service.AccountService;
import com.EventManagementSystem.service.BookingService;
import com.EventManagementSystem.service.CategoryService;
import com.EventManagementSystem.service.EventService;
import com.EventManagementSystem.service.UserService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final EventService eventService;
    private final BookingService bookingService;
    private final CategoryService categoryService;
    private final AccountService accountService;

    @Autowired
    public AdminController(UserService userService, EventService eventService, BookingService bookingService,
                            CategoryService categoryService, AccountService accountService) {
        this.userService = userService;
        this.eventService = eventService;
        this.bookingService = bookingService;
        this.categoryService = categoryService;
        this.accountService = accountService;
    }

    @PostMapping("/category")
    public ResponseEntity<Category> createCategory(@RequestBody CategoryRequest request) {
        Category category = categoryService.createCategory(request.getName(), request.getDescription());
        return ResponseEntity.status(201).body(category);
    }

    @GetMapping("/categories")
    public List<Category> listCategories() {
        return categoryService.listCategories();
    }

    @PutMapping("/category/{id}")
    public ResponseEntity<String> updateCategory(@PathVariable long id, @RequestBody CategoryRequest request) {
        categoryService.updateCategory(id, request.getName(), request.getDescription());
        return ResponseEntity.ok().body("Category Updated");
    }

    @DeleteMapping("/category/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok().body("category deleted successfully!");
    }

    @GetMapping("/customers")
    public List<UserResponse> listCustomers() {
        List<User> users = userService.listUsersByRole(Role.CUSTOMER);
        List<UserResponse> responseList = new ArrayList<>(users.size());
        for (User user : users) {
            user.setEventCount(bookingService.listBookingsForCustomer(user.getId()).size());
            responseList.add(userService.convertToUserResponse(user));
        }
        return responseList;
    }

    @GetMapping("/organizers")
    public List<UserResponse> listOrganizers() {
        List<User> users = userService.listUsersByRole(Role.ORGANIZER);
        List<UserResponse> responseList = new ArrayList<>(users.size());
        for (User user : users) {
            user.setEventCount(eventService.listEventsByOrganizer(user.getId()).size());
            responseList.add(userService.convertToUserResponse(user));
        }
        return responseList;
    }

    @PutMapping("/user/{userId}/status")
    public ResponseEntity<String> updateUserStatus(@PathVariable long userId, @RequestBody UserStatusRequest request) {
        if (request.isActive()) {
            userService.activateUser(userId);
        } else {
            userService.deactivateUser(userId);
        }
        return ResponseEntity.ok().body("User updated successfully");
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<UserResponse> getCustomer(@PathVariable long userId){
        User user = userService.getProfile(userId);
        user.setEventCount(eventService.listEventsByOrganizer(user.getId()).size());
        UserResponse response = userService.convertToUserResponse(user);
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok().body("User deleted successfully");
    }

    @GetMapping("/events")
    public List<Event> viewAllEvents() {
        List<Event> events = eventService.listAllEvents();
        for (Event event : events) {
            event.setRevenue(bookingService.getRevenueForEvent(event.getEventId()));
        }
        return events;
    }

    @GetMapping("/active-events")
    public List<Event> viewAvailableEvents(){
        return eventService.listActiveEvents();
    }

    @GetMapping("/bookings")
    public List<Booking> viewAllBookings() {
        return bookingService.listAllBookings();
    }

    @GetMapping("/transactions")
    public List<AccountTransaction> viewAllTransactions() {
        return accountService.listAllTransactionsEnriched();
    }

    @GetMapping("/dashboard")
    public Map<String, Long> viewDashboardSummary() {
        Map<String, Long> summary = new HashMap<>();
        summary.put("totalCustomers", userService.countByRole(Role.CUSTOMER));
        summary.put("totalOrganizers", userService.countByRole(Role.ORGANIZER));
        summary.put("totalEvents", eventService.countAllEvents());
        summary.put("totalBookings", bookingService.countAllBookings());
        return summary;
    }
}
