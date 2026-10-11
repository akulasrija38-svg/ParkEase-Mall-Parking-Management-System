
package com.parking.parkingsystem.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.parking.parkingsystem.model.Booking;

@Repository
public interface BookingRepository extends MongoRepository<Booking, String> {

    /*
     * Finds bookings that overlap the requested time.
     */
    @Query("{ 'slotId': ?0, 'status': { '$in': ?1 }, 'startTime': { '$lt': ?2 }, 'endTime': { '$gt': ?3 } }")
    List<Booking> findBySlotIdAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
            String slotId,
            List<String> statuses,
            LocalDateTime requestedEnd,
            LocalDateTime requestedStart);

    List<Booking> findByUserName(String userName);

    List<Booking> findByUserEmailIgnoreCase(String userEmail);

}
