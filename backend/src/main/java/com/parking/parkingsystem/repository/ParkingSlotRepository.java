package com.parking.parkingsystem.repository;

import java.util.List;
import com.parking.parkingsystem.model.ParkingSlot;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ParkingSlotRepository
        extends MongoRepository<ParkingSlot, String> {

    List<ParkingSlot> findByMallIdAndActiveTrue(String mallId);
}