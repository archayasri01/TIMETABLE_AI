
package com.example.demo.entity;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class Classroom {

    private String id;

    @NotBlank(message = "Building is required")
    private String building;

    @NotBlank(message = "Room number is required")
    private String roomNumber;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Classroom capacity must be positive")
    private Integer capacity;

    public Classroom() {
    }

    public Classroom(String id, String building,
                     String roomNumber, Integer capacity) {
        this.id = id;
        this.building = building;
        this.roomNumber = roomNumber;
        this.capacity = capacity;
    }

    public Classroom(String building, String roomNumber,
                     Integer capacity) {
        this.building = building;
        this.roomNumber = roomNumber;
        this.capacity = capacity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
}
