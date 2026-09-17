package com.EventManagementSystem.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.EventManagementSystem.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByBookingId(long bookingId);

    List<Payment> findByCustomerIdOrderByPaymentTimestampDesc(long customerId);
}