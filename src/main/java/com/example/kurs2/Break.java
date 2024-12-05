package com.example.kurs2;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Break aBreak = (Break) o;
        return Objects.equals(departureStationId, aBreak.departureStationId) &&
                Objects.equals(arrivalStationId, aBreak.arrivalStationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(departureStationId, arrivalStationId);
    }


}
