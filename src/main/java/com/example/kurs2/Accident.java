package com.example.kurs2;

public class Accident {
    private final String departureStationId; // Станция отправления
    private final String arrivalStationId;   // Станция прибытия
    private final int delayTime;             // Кол-во часов задердержка
    private boolean resolved;                // Флаг завершения поломки

    public Accident(String departureStationId, String arrivalStationId, int delayTime) {
        this.departureStationId = departureStationId;
        this.arrivalStationId = arrivalStationId;
        this.delayTime = delayTime;
        this.resolved = false;
    }

    public String getDepartureStationId() {
        return departureStationId;
    }

    public String getArrivalStationId() {
        return arrivalStationId;
    }

    public int getDelayTime() {
        return delayTime;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void resolve() {
        this.resolved = true;
    }
}
