package com.example.kurs2;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ScheduleRecord {
    private final StringProperty startStation;
    private final StringProperty endStation;
    private final StringProperty departureTime;

    public ScheduleRecord(String startStation, String endStation, String departureTime) {
        this.startStation = new SimpleStringProperty(startStation);
        this.endStation = new SimpleStringProperty(endStation);
        this.departureTime = new SimpleStringProperty(departureTime);
    }

    public String getStartStation() {
        return startStation.get();
    }

    public void setStartStation(String startStation) {
        this.startStation.set(startStation);
    }

    public StringProperty startStationProperty() {
        return startStation;
    }

    public String getEndStation() {
        return endStation.get();
    }

    public void setEndStation(String endStation) {
        this.endStation.set(endStation);
    }

    public StringProperty endStationProperty() {
        return endStation;
    }

    public String getDepartureTime() {
        return departureTime.get();
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime.set(departureTime);
    }

    public StringProperty departureTimeProperty() {
        return departureTime;
    }
}
