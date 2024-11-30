package com.example.kurs2;

import java.util.ArrayList;
import java.util.List;

public class JourneyLog {

    public static class StationLogEntry {
        private final String stationName;
        private final String arrivalTime;
        private String departureTime;
        private String status;

        public StationLogEntry(String stationName, String arrivalTime, String status) {
            this.stationName = stationName;
            this.arrivalTime = arrivalTime;
            this.status = status;
        }

        public String getStationName() {
            return stationName;
        }

        public String getArrivalTime() {
            return arrivalTime;
        }

        public String getDepartureTime() {
            return departureTime;
        }

        public void setDepartureTime(String departureTime) {
            this.departureTime = departureTime;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        @Override
        public String toString() {
            return String.format("Station: %s, Arrival: %s, Departure: %s, Status: %s",
                    stationName, arrivalTime, departureTime == null ? "N/A" : departureTime, status);
        }
    }


    private final List<StationLogEntry> logEntries = new ArrayList<>(); // Список записей путевой карты

    // Добавление записи о посещение станции
    public void addEntry(String stationName, String arrivalTime, String status) {
        logEntries.add(new StationLogEntry(stationName, arrivalTime, status));
    }

    // Обновить запись о станции
    public void updateEntry(String stationName, String departureTime, String status) {
        for (StationLogEntry entry : logEntries) {
            if (entry.getStationName().equals(stationName)) {
                if (departureTime != null) entry.setDepartureTime(departureTime);
                if (status != null) entry.setStatus(status);
                return;
            }
        }
    }

    // Получить все записи путевой карты
    public List<StationLogEntry> getEntries() {
        return new ArrayList<>(logEntries);
    }



    @Override//печать для отладки
    public String toString() {
        StringBuilder builder = new StringBuilder("Journey Log:\n");
        for (StationLogEntry entry : logEntries) {
            builder.append(entry).append("\n");
        }
        return builder.toString();
    }
}