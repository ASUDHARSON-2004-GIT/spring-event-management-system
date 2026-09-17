package com.EventManagementSystem.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.EventManagementSystem.model.Account;
import com.EventManagementSystem.model.AccountTransaction;
import com.EventManagementSystem.model.Booking;
import com.EventManagementSystem.model.Event;
import com.EventManagementSystem.model.Payment;
import com.EventManagementSystem.model.User;
import com.EventManagementSystem.repository.AccountRepository;
import com.EventManagementSystem.repository.AccountTransactionRepository;
import com.EventManagementSystem.repository.BookingRepository;
import com.EventManagementSystem.repository.EventRepository;
import com.EventManagementSystem.repository.PaymentRepository;
import com.EventManagementSystem.repository.UserRepository;
import com.EventManagementSystem.service.AccountService;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountTransactionRepository accountTransactionRepository;
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Autowired
    public AccountServiceImpl(AccountRepository accountRepository,
                               AccountTransactionRepository accountTransactionRepository,
                               PaymentRepository paymentRepository,
                               BookingRepository bookingRepository,
                               EventRepository eventRepository,
                               UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.accountTransactionRepository = accountTransactionRepository;
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Account createAccountForOrganizer(long organizerId) {
        return accountRepository.findByOrganizerId(organizerId)
                .orElseGet(() -> {
                    Account account = new Account();
                    account.setOrganizerId(organizerId);
                    account.setBalance(BigDecimal.ZERO);
                    return accountRepository.save(account);
                });
    }

    @Override
    public Account getAccountForOrganizer(long organizerId) {
        return accountRepository.findByOrganizerId(organizerId).orElse(null);
    }

    @Override
    public List<AccountTransaction> listAllTransactions() {
        return accountTransactionRepository.findAllByOrderByTransactionTimestampDesc();
    }

    @Override
    public List<AccountTransaction> listAllTransactionsEnriched() {
        List<AccountTransaction> transactions = accountTransactionRepository.findAllByOrderByTransactionTimestampDesc();

        for (AccountTransaction transaction : transactions) {
            Payment payment = paymentRepository.findById(transaction.getPaymentId()).orElse(null);
            if (payment == null) {
                continue;
            }
            Booking booking = bookingRepository.findById(payment.getBookingId()).orElse(null);
            if (booking == null) {
                continue;
            }
            Event event = eventRepository.findById(booking.getEventId()).orElse(null);
            if (event != null) {
                transaction.setEventName(event.getEventName());
                User organizer = userRepository.findById(event.getOrganizerId()).orElse(null);
                if (organizer != null) {
                    transaction.setOrganizerName(organizer.getName());
                }
            }
        }
        return transactions;
    }
}
