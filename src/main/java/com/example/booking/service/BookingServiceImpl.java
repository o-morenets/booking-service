package com.example.booking.service;

import com.example.booking.dto.BookingResponse;
import com.example.booking.dto.CreateBookingRequest;
import com.example.booking.entity.Booking;
import com.example.booking.entity.Payment;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.example.booking.entity.BookingStatus.PAID;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public BookingResponse createBooking(CreateBookingRequest request) {
        return null;
    }

    @Override
    public void cancelBooking(UUID bookingId) {

    }

    @Override
    @Transactional
    public void pay(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        booking.setStatus(PAID);

        paymentRepository.save(new Payment());
    }
}
