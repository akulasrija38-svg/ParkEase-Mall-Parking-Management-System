
package com.parking.parkingsystem.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "bookings")
public class Booking {

    @Id
    private String id;

    // Old booking reference and user details
    private String bookingId;
    private String userId;
    private String userName;
    private String userEmail;
    private String userPhone;

    // Mall and parking slot details
    private String mallId;
    private String slotId;
    private String slotNumber;

    // Vehicle details
    private String vehicleType;
    private String vehicleNumber;
    private String vehicleModel;
    private String vehicleColor;

    // Booking period
    private LocalDateTime bookingDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // Actual parking times
    private LocalDateTime actualCheckIn;
    private LocalDateTime actualCheckOut;
    private LocalDateTime actualExitTime;

    // Status and payment details
    private String status = "BOOKED";
    private double hourlyRate = 40.0;
    private long durationHours;
    private double amount;

    // Record creation time
    private LocalDateTime createdAt;

    public Booking() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }

    public String getMallId() { return mallId; }
    public void setMallId(String mallId) { this.mallId = mallId; }

    public String getSlotId() { return slotId; }
    public void setSlotId(String slotId) { this.slotId = slotId; }

    public String getSlotNumber() { return slotNumber; }
    public void setSlotNumber(String slotNumber) { this.slotNumber = slotNumber; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public String getVehicleModel() { return vehicleModel; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }

    public String getVehicleColor() { return vehicleColor; }
    public void setVehicleColor(String vehicleColor) { this.vehicleColor = vehicleColor; }

    public LocalDateTime getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDateTime bookingDate) { this.bookingDate = bookingDate; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public LocalDateTime getActualCheckIn() { return actualCheckIn; }
    public void setActualCheckIn(LocalDateTime actualCheckIn) { this.actualCheckIn = actualCheckIn; }

    public LocalDateTime getActualCheckOut() { return actualCheckOut; }
    public void setActualCheckOut(LocalDateTime actualCheckOut) { this.actualCheckOut = actualCheckOut; }

    public LocalDateTime getActualExitTime() { return actualExitTime; }
    public void setActualExitTime(LocalDateTime actualExitTime) { this.actualExitTime = actualExitTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(double hourlyRate) { this.hourlyRate = hourlyRate; }

    public long getDurationHours() { return durationHours; }
    public void setDurationHours(long durationHours) { this.durationHours = durationHours; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
