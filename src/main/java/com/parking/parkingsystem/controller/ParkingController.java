
package com.parking.parkingsystem.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.parking.parkingsystem.model.Booking;
import com.parking.parkingsystem.model.Mall;
import com.parking.parkingsystem.model.ParkingSlot;
import com.parking.parkingsystem.model.User;
import com.parking.parkingsystem.repository.BookingRepository;
import com.parking.parkingsystem.repository.MallRepository;
import com.parking.parkingsystem.repository.ParkingSlotRepository;
import com.parking.parkingsystem.repository.UserRepository;
import com.parking.parkingsystem.service.BookingService;

@RestController
@RequestMapping("/api")
public class ParkingController {

    private final MallRepository malls;
    private final ParkingSlotRepository slots;
    private final BookingService bookingService;
    private final UserRepository users;
    private final BookingRepository bookings;

    public ParkingController(
            MallRepository malls,
            ParkingSlotRepository slots,
            BookingService bookingService,
            UserRepository users,
            BookingRepository bookings) {

        this.malls = malls;
        this.slots = slots;
        this.bookingService = bookingService;
        this.users = users;
        this.bookings = bookings;
    }

    private User currentUser(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("Please log in first");
        }

        return users.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User account not found"));
    }


    // Get active malls
    @GetMapping("/malls")
    public List<Mall> getMalls() {
        return malls.findAll().stream()
                .filter(Mall::isActive)
                .toList();
    }

    // Add a mall
    @PostMapping("/malls")
    @ResponseStatus(HttpStatus.CREATED)
    public Mall addMall(@RequestBody Mall mall) {
        if (mall.getName() == null || mall.getName().isBlank()
                || mall.getAddress() == null
                || mall.getAddress().isBlank()) {
            throw new IllegalArgumentException(
                    "Mall name and address are required");
        }

        mall.setId(null);
        mall.setActive(true);
        return malls.save(mall);
    }

    // Add a parking slot to a mall
    @PostMapping("/malls/{mallId}/slots")
    @ResponseStatus(HttpStatus.CREATED)
    public ParkingSlot addSlot(
            @PathVariable String mallId,
            @RequestBody ParkingSlot slot) {

        Mall mall = malls.findById(mallId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Mall not found"));

        if (!mall.isActive()) {
            throw new IllegalArgumentException("Mall is inactive");
        }

        if (slot.getSlotNumber() == null
                || slot.getSlotNumber().isBlank()) {
            throw new IllegalArgumentException(
                    "Slot number is required");
        }

        slot.setId(null);
        slot.setMallId(mallId);
        slot.setActive(true);
        return slots.save(slot);
    }

    // Get available slots for a requested time range
    @GetMapping("/malls/{mallId}/slots")
    public List<ParkingSlot> getAvailableSlots(
            @PathVariable String mallId,
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {

        if (start == null || end == null
                || !start.isAfter(LocalDateTime.now())
                || !end.isAfter(start)) {
            throw new IllegalArgumentException(
                    "Please provide a valid future time range");
        }

        Mall mall = malls.findById(mallId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Mall not found"));

        if (!mall.isActive()) {
            throw new IllegalArgumentException("Mall is inactive");
        }

        List<String> blockingStatuses = List.of("BOOKED", "ACTIVE");

        return slots.findByMallIdAndActiveTrue(mallId).stream()
                .filter(slot -> bookingService.isSlotAvailable(
                        slot.getId(),
                        blockingStatuses,
                        start,
                        end))
                .toList();
    }

    // Create a booking for the logged-in user
    @PostMapping("/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    public Booking createBooking(
            @RequestBody Booking booking,
            Authentication authentication) {

        User user = currentUser(authentication);

        return bookingService.createBooking(
                booking, user.getEmail(), user.getName(), user.getPhone());
    }

    // Get bookings belonging to the logged-in user
    @GetMapping("/bookings")
    public List<Booking> getMyBookings(
            Authentication authentication) {

        User user = currentUser(authentication);
        return bookingService.getUserBookings(user.getEmail());
    }

    // Admin: view all bookings
    @GetMapping("/admin/bookings")
    public List<Booking> getAllBookings(
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getAuthorities().stream().noneMatch(a ->
                    a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ADMIN"))) {
            throw new org.springframework.security.access.AccessDeniedException("Admin access required");
        }
        return bookings.findAll();
    }

    // Legacy route retained for compatibility
    @GetMapping("/bookings/user/{userName}")
    public List<Booking> oldUserBookingsRoute(
            @PathVariable String userName) {

        throw new IllegalArgumentException(
                "Use GET /api/bookings to view your own bookings");
    }

    // Cancel a future booking
    @PatchMapping("/bookings/{id}/cancel")
    public Booking cancelBooking(
            @PathVariable String id,
            Authentication authentication) {

        User user = currentUser(authentication);
        return bookingService.cancelBooking(id, user.getEmail());
    }

    // User-requested early exit
    @PatchMapping("/bookings/{id}/early-exit")
    public Booking earlyExit(
            @PathVariable String id,
            Authentication authentication) {

        User user = currentUser(authentication);
        return bookingService.recordEarlyExit(id, user.getEmail());
    }

}
