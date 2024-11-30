package com.example.kurs2;

public class Break {

    private final String departureStationId;
    private final String arrivalStationId;

    public Break(String departureStationId, String arrivalStationId) {
        this.departureStationId = departureStationId;
        this.arrivalStationId = arrivalStationId;
    }

    public String getDepartureStationId() {
        return departureStationId;
    }

    public String getArrivalStationId() {
        return arrivalStationId;
    }


}
