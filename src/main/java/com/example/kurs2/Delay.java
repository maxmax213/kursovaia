package com.example.kurs2;

public class Delay {
    private final int delayTime; // Задержка в минутах
    private final String stationId; // ID станции

    public Delay(int delayTime, String stationId) {
        this.delayTime = delayTime;
        this.stationId = stationId;
    }

    public int getDelayTime() {
        return delayTime;
    }

    public String getStationId() {
        return stationId;
    }
}
