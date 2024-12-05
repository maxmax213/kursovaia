package com.example.kurs2;

import java.util.ArrayList;
import java.util.List;

public class JourneyLog {


    private int TrainId;

    public static class StationLogEntry {

        private final String stationName;
        private final String arrivalTime;
        private String departureTime;
        private String status;
        private final int trainId;

        public StationLogEntry(String stationName, String arrivalTime, String status, int trainId) {
            this.stationName = stationName;
            this.arrivalTime = arrivalTime;
            this.status = status;
            this.trainId = trainId;
        }


        public String getStationName() {
            return stationName;
        }

        public String getArrivalTime() {
            return arrivalTime;
        }

        public String getDepartureTime() {
            return departureTime != null ? departureTime : "N/A";
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

        public int getTrainId() {
            return trainId;
        }

        @Override
        public String toString() {
            return String.format("Station: %s, Arrival: %s, Departure: %s, Status: %s",
                    stationName, arrivalTime, departureTime == null ? "N/A" : departureTime, status);
        }
    }


    public void setTrainId(int trainId) {
        TrainId = trainId;
    }


    private final List<StationLogEntry> logEntries = new ArrayList<>(); // Список записей путевой карты


    public void addEntry(String stationName, String arrivalTime, String status) {
        System.out.println(TrainId);
        logEntries.add(new StationLogEntry(stationName, arrivalTime, status, TrainId));
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