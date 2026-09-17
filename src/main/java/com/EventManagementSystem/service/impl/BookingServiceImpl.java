package com.EventManagementSystem.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.EventManagementSystem.exception.BookingNotFoundException;
import com.EventManagementSystem.exception.EventNotFoundException;
import com.EventManagementSystem.exception.InsufficientSeatsException;
import com.EventManagementSystem.exception.InvalidBookingException;
import com.EventManagementSystem.exception.ValidationException;
import com.EventManagementSystem.model.Booking;
import com.EventManagementSystem.model.BookingStatus;
import com.EventManagementSystem.model.Event;
import com.EventManagementSystem.model.EventStatus;
import com.EventManagementSystem.model.Payment;
import com.EventManagementSystem.model.PaymentMethod;
import com.EventManagementSystem.model.PaymentStatus;
import com.EventManagementSystem.repository.BookingRepository;
import com.EventManagementSystem.repository.EventRepository;
import com.EventManagementSystem.repository.PaymentRepository;
import com.EventManagementSystem.repository.UserRepository;
import com.EventManagementSystem.service.BookingService;
import com.EventManagementSystem.service.PaymentService;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final PaymentService paymentService;

    @Autowired
    public BookingServiceImpl(BookingRepository bookingRepository, EventRepository eventRepository,
                              PaymentRepository paymentRepository, UserRepository userRepository,
                              @Lazy PaymentService paymentService) {
        this.bookingRepository = bookingRepository;
        this.eventRepository = eventRepository;
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.paymentService = paymentService;
    }

    @Override
    @Transactional
    public long createBooking(long customerId, long eventId, int seats) {

        if (seats <= 0) {
            throw new ValidationException("Number of seats must be greater than zero");
        }

        Event event = eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id " + eventId));

        EventStatus liveStatus = EventStatus.deriveStatus(event.getEventDate(), event.getStatus());
        if (liveStatus != EventStatus.UPCOMING) {
            throw new ValidationException("Bookings are only allowed for upcoming events");
        }
        if (event.getAvailableSeats() < seats) {
            throw new InsufficientSeatsException("Only " + event.getAvailableSeats()
                    + " seats are available for this event");
        }

        BigDecimal totalAmount = event.getTicketPrice().multiply(BigDecimal.valueOf(seats));

        Booking booking = new Booking();
        booking.setUserId(customerId);
        booking.setEventId(eventId);
        booking.setSeatsBooked(seats);
        booking.setTotalAmount(totalAmount);
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        booking = bookingRepository.save(booking);

        Payment payment = new Payment();
        payment.setBookingId(booking.getBookingId());
        payment.setCustomerId(customerId);
        payment.setAmount(totalAmount);
        payment.setPaymentMethod(PaymentMethod.CASH);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        paymentRepository.save(payment);

        event.setAvailableSeats(event.getAvailableSeats() - seats);
        eventRepository.save(event);

        return booking.getBookingId();
    }

    @Override
    public Booking getBookingById(long bookingId) {
        return enrich(bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with id " + bookingId)));
    }

    @Override
    public List<Booking> listBookingsForCustomer(long customerId) {
        List<Booking> bookings = bookingRepository.findByUserIdOrderByBookingTimestampDesc(customerId);
        bookings.forEach(this::enrichInPlace);
        return bookings;
    }

    @Override
    public List<Booking> listBookingsForEvent(long eventId) {
        List<Booking> bookings = bookingRepository.findByEventIdOrderByBookingTimestampDesc(eventId);
        bookings.forEach(this::enrichInPlace);
        return bookings;
    }

    @Override
    public List<Booking> listAllBookings() {
        List<Booking> bookings = bookingRepository.findAllByOrderByBookingTimestampDesc();
        bookings.forEach(this::enrichInPlace);
        return bookings;
    }

    @Override
    @Transactional
    public void cancelBooking(long bookingId, long requestingCustomerId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with id " + bookingId));

        if (booking.getUserId() != requestingCustomerId) {
            throw new InvalidBookingException("You can only cancel your own bookings");
        }
        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new InvalidBookingException("This booking is already cancelled");
        }

        Event event = eventRepository.findByIdForUpdate(booking.getEventId())
                .orElseThrow(() -> new EventNotFoundException("Event not found with id " + booking.getEventId()));

        event.setAvailableSeats(event.getAvailableSeats() + booking.getSeatsBooked());
        eventRepository.save(event);

        booking.setBookingStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(java.time.LocalDateTime.now());
        bookingRepository.save(booking);

        paymentService.refundPayment(bookingId);
    }

    @Override
    public long countAllBookings() {
        return bookingRepository.count();
    }

    @Override
    public BigDecimal getRevenueForEvent(long eventId) {
        return bookingRepository.getRevenueByEvent(eventId);
    }

    private Booking enrich(Booking booking) {
        enrichInPlace(booking);
        return booking;
    }

    private void enrichInPlace(Booking booking) {
        userRepository.findById(booking.getUserId())
                .ifPresent(user -> booking.setUserName(user.getName()));
        eventRepository.findById(booking.getEventId())
                .ifPresent(event -> booking.setEventName(event.getEventName()));
    }
}