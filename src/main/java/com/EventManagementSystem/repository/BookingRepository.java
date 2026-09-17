package com.EventManagementSystem.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.EventManagementSystem.model.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserIdOrderByBookingTimestampDesc(long userId);

    List<Booking> findByEventIdOrderByBookingTimestampDesc(long eventId);

    List<Booking> findAllByOrderByBookingTimestampDesc();

    @Query("select coalesce(sum(b.totalAmount), 0) from Booking b, Payment p "
            + "where b.bookingId = p.bookingId and b.eventId = :eventId "
            + "and b.bookingStatus = com.EventManagementSystem.model.BookingStatus.CONFIRMED "
            + "and p.paymentStatus = com.EventManagementSystem.model.PaymentStatus.SUCCESS")
    BigDecimal getRevenueByEvent(@Param("eventId") long eventId);
}
