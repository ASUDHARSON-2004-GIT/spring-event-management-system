package com.EventManagementSystem.service;

import java.math.BigDecimal;
import java.util.List;

import com.EventManagementSystem.model.Booking;

public interface BookingService {

    long createBooking(long customerId, long eventId, int seats);

    Booking getBookingById(long bookingId);

    List<Booking> listBookingsForCustomer(long customerId);

    List<Booking> listBookingsForEvent(long eventId);

    List<Booking> listAllBookings();

    void cancelBooking(long bookingId, long requestingCustomerId);

    long countAllBookings();

    BigDecimal getRevenueForEvent(long eventId);
}
