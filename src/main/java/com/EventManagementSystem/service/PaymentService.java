package com.EventManagementSystem.service;

import com.EventManagementSystem.model.Payment;
import com.EventManagementSystem.model.PaymentMethod;

public interface PaymentService {

    void confirmPayment(long bookingId, PaymentMethod method);

    void failPayment(long bookingId);

    void refundPayment(long bookingId);

    Payment getPaymentForBooking(long bookingId);
}
