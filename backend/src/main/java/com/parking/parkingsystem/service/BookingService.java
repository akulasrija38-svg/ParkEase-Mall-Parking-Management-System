
package com.parking.parkingsystem.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.parking.parkingsystem.model.Booking;
import com.parking.parkingsystem.model.Mall;
import com.parking.parkingsystem.model.ParkingSlot;
import com.parking.parkingsystem.repository.BookingRepository;
import com.parking.parkingsystem.repository.MallRepository;
import com.parking.parkingsystem.repository.ParkingSlotRepository;

@Service
public class BookingService {

    private final BookingRepository bookings;
    private final MallRepository malls;
    private final ParkingSlotRepository slots;

    public BookingService(
            BookingRepository bookings,
            MallRepository malls,
            ParkingSlotRepository slots) {
        this.bookings = bookings;
        this.malls = malls;
        this.slots = slots;
    }

    public Booking createBooking(
            Booking booking, String email, String name, String phone) {

        if (email == null || email.isBlank()
                || name == null || name.isBlank()
                || booking.getSlotId() == null
                || booking.getStartTime() == null
                || booking.getEndTime() == null) {
            throw new IllegalArgumentException(
                    "Required booking details are missing");
        }

        if (booking.getVehicleType() == null
                || booking.getVehicleType().isBlank()
                || booking.getVehicleNumber() == null
                || booking.getVehicleNumber().isBlank()
                || booking.getVehicleModel() == null
                || booking.getVehicleModel().isBlank()
                || booking.getVehicleColor() == null
                || booking.getVehicleColor().isBlank()) {
            throw new IllegalArgumentException(
                    "Please provide all vehicle details");
        }

        LocalDateTime now = LocalDateTime.now();

        if (!booking.getEndTime().isAfter(booking.getStartTime())) {
            throw new IllegalArgumentException(
                    "End time must be after start time");
        }

        if (!booking.getStartTime().isAfter(now)) {
            throw new IllegalArgumentException(
                    "Booking must start in the future");
        }

        ParkingSlot slot = slots.findById(booking.getSlotId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Parking slot not found"));

        if (!slot.isActive()) {
            throw new IllegalArgumentException("Parking slot is inactive");
        }

        Mall mall = malls.findById(slot.getMallId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Mall not found"));

        if (!mall.isActive()) {
            throw new IllegalArgumentException("Mall is inactive");
        }

        List<String> blockingStatuses = List.of("BOOKED", "ACTIVE");

        boolean overlapping = !bookings
                .findBySlotIdAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
                        slot.getId(),
                        blockingStatuses,
                        booking.getEndTime(),
                        booking.getStartTime())
                .isEmpty();

        if (overlapping) {
            throw new IllegalArgumentException(
                    "This slot is already booked for the selected time");
        }

        // Authenticated user details
        booking.setUserEmail(email.trim().toLowerCase());
        booking.setUserName(name);
        booking.setUserPhone(phone);
        booking.setMallId(mall.getId());

        // Booking identification and dates
        booking.setBookingId("BK" +
                UUID.randomUUID().toString()
                        .replace("-", "").substring(0, 10).toUpperCase());
        booking.setBookingDate(now);
        booking.setCreatedAt(now);

        // Parking slot details
        booking.setSlotNumber(slot.getSlotNumber());
        booking.setHourlyRate(slot.getHourlyRate());

        // Normalize vehicle number
        booking.setVehicleNumber(
                booking.getVehicleNumber().trim().toUpperCase());

        // Calculate reserved duration and estimated amount
        long reservedMinutes = Duration.between(
                booking.getStartTime(), booking.getEndTime()).toMinutes();
        long reservedHours = (reservedMinutes + 59) / 60;

        booking.setDurationHours(reservedHours);
        booking.setAmount(reservedHours * slot.getHourlyRate());

        // Initialize tracking fields
        booking.setStatus("BOOKED");
        booking.setActualCheckIn(null);
        booking.setActualCheckOut(null);
        booking.setActualExitTime(null);

        return bookings.save(booking);
    }

    public List<Booking> getUserBookings(String email) {
        return bookings.findByUserEmailIgnoreCase(email);
    }

    public Booking cancelBooking(String id, String email) {
        Booking booking = findOwnedBooking(id, email);
        LocalDateTime now = LocalDateTime.now();

        if (!"BOOKED".equals(booking.getStatus())
                || booking.getStartTime() == null
                || !booking.getStartTime().isAfter(now)) {
            throw new IllegalArgumentException(
                    "Only future bookings can be cancelled");
        }

        booking.setStatus("CANCELLED");
        return bookings.save(booking);
    }

    public Booking recordEarlyExit(String id, String email) {
        Booking booking = findOwnedBooking(id, email);
        LocalDateTime now = LocalDateTime.now();

        if (!"BOOKED".equals(booking.getStatus())
                || booking.getStartTime() == null
                || booking.getEndTime() == null
                || now.isBefore(booking.getStartTime())
                || !now.isBefore(booking.getEndTime())) {
            throw new IllegalArgumentException(
                    "Early exit is available only during a booked parking session");
        }

        booking.setActualExitTime(now);
        booking.setActualCheckOut(now);
        booking.setEndTime(now);
        booking.setStatus("COMPLETED");

        // Charge for actual parking time, rounded up to the next hour.
        long actualMinutes = Math.max(
                1, Duration.between(
                        booking.getStartTime(), now).toMinutes());
        long actualHours = (actualMinutes + 59) / 60;

        booking.setDurationHours(actualHours);
        booking.setAmount(actualHours * booking.getHourlyRate());

        return bookings.save(booking);
    }

    private Booking findOwnedBooking(String id, String email) {
        Booking booking = bookings.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Booking not found"));

        if (email == null
                || booking.getUserEmail() == null
                || !booking.getUserEmail().equalsIgnoreCase(email)) {
            throw new IllegalArgumentException("Booking not found");
        }

        return booking;
    }

    public boolean isSlotAvailable(
            String slotId,
            List<String> blockingStatuses,
            LocalDateTime start,
            LocalDateTime end) {

        return bookings
                .findBySlotIdAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
                        slotId, blockingStatuses, end, start)
                .isEmpty();
    }
}
