package com.EventManagementSystem.service.impl;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.EventManagementSystem.exception.BookingNotFoundException;
import com.EventManagementSystem.exception.InvalidBookingException;
import com.EventManagementSystem.model.Account;
import com.EventManagementSystem.model.AccountTransaction;
import com.EventManagementSystem.model.Booking;
import com.EventManagementSystem.model.Event;
import com.EventManagementSystem.model.Payment;
import com.EventManagementSystem.model.PaymentMethod;
import com.EventManagementSystem.model.PaymentStatus;
import com.EventManagementSystem.model.TransactionType;
import com.EventManagementSystem.repository.AccountRepository;
import com.EventManagementSystem.repository.AccountTransactionRepository;
import com.EventManagementSystem.repository.BookingRepository;
import com.EventManagementSystem.repository.EventRepository;
import com.EventManagementSystem.repository.PaymentRepository;
import com.EventManagementSystem.service.PaymentService;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final PaymentRepository paymentRepository;
    private final AccountRepository accountRepository;
    private final AccountTransactionRepository accountTransactionRepository;

    @Autowired
    public PaymentServiceImpl(BookingRepository bookingRepository, EventRepository eventRepository,
                              PaymentRepository paymentRepository, AccountRepository accountRepository,
                              AccountTransactionRepository accountTransactionRepository) {
        this.bookingRepository = bookingRepository;
        this.eventRepository = eventRepository;
        this.paymentRepository = paymentRepository;
        this.accountRepository = accountRepository;
        this.accountTransactionRepository = accountTransactionRepository;
    }

    @Override
    @Transactional
    public void confirmPayment(long bookingId, PaymentMethod method) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with id " + bookingId));

        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new InvalidBookingException("No payment record found for this booking"));

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new InvalidBookingException("This payment has already been processed");
        }

        Event event = eventRepository.findById(booking.getEventId())
                .orElseThrow(() -> new InvalidBookingException("Event no longer exists for this booking"));

        Account account = accountRepository.findByOrganizerIdForUpdate(event.getOrganizerId())
                .orElseThrow(() -> new InvalidBookingException("No account exists for the organizer of this event"));

        payment.setPaymentMethod(method);
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        paymentRepository.save(payment);

        account.setBalance(account.getBalance().add(payment.getAmount()));
        accountRepository.save(account);

        AccountTransaction transaction = new AccountTransaction();
        transaction.setAccountId(account.getAccountId());
        transaction.setPaymentId(payment.getPaymentId());
        transaction.setCustomerId(payment.getCustomerId());
        transaction.setTransactionType(TransactionType.CREDIT);
        transaction.setAmount(payment.getAmount());
        accountTransactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public void failPayment(long bookingId) {
        bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with id " + bookingId));

        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new InvalidBookingException("No payment record found for this booking"));

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new InvalidBookingException("This payment has already been processed");
        }

        payment.setPaymentStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public void refundPayment(long bookingId) {

        bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with id " + bookingId));

        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new InvalidBookingException("No payment record found for this booking"));

        if (payment.getPaymentStatus() != PaymentStatus.SUCCESS) {
            throw new InvalidBookingException("Only a successful payment can be refunded");
        }

        Booking booking = bookingRepository.findById(bookingId).get();
        Event event = eventRepository.findById(booking.getEventId())
                .orElseThrow(() -> new InvalidBookingException("Event no longer exists for this booking"));

        Account account = accountRepository.findByOrganizerIdForUpdate(event.getOrganizerId())
                .orElseThrow(() -> new InvalidBookingException("No account exists for the organizer of this event"));

        payment.setPaymentStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);

        BigDecimal newBalance = account.getBalance().subtract(payment.getAmount());
        account.setBalance(newBalance);
        accountRepository.save(account);

        AccountTransaction transaction = new AccountTransaction();
        transaction.setAccountId(account.getAccountId());
        transaction.setPaymentId(payment.getPaymentId());
        transaction.setCustomerId(payment.getCustomerId());
        transaction.setTransactionType(TransactionType.DEBIT);
        transaction.setAmount(payment.getAmount());
        accountTransactionRepository.save(transaction);
    }

    @Override
    public Payment getPaymentForBooking(long bookingId) {
        bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with id " + bookingId));

        return paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("No payment record found for this booking"));
    }
}