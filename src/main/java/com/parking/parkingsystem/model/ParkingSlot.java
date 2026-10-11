
package com.parking.parkingsystem.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "parking_slots")
public class ParkingSlot {

    @Id
    private String id;

    private String mallId;
    private String mallName;
    private String slotNumber;

    // New ParkEase format
    private String type = "CAR";

    // Old parking-system format
    private String vehicleType;
    private String floor;
    private String status = "AVAILABLE";
    private double hourlyRate = 40.0;

    private boolean active = true;

    public ParkingSlot() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMallId() {
        return mallId;
    }

    public void setMallId(String mallId) {
        this.mallId = mallId;
    }

    public String getMallName() {
        return mallName;
    }

    public void setMallName(String mallName) {
        this.mallName = mallName;
    }

    public String getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(String slotNumber) {
        this.slotNumber = slotNumber;
    }

    public String getType() {
        return type != null ? type : vehicleType;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVehicleType() {
        return vehicleType != null ? vehicleType : type;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
