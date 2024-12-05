package com.example.kurs2;

import javafx.application.Platform;
import javafx.scene.control.Label;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class Modelling {
    private Timetable timetable;
    private Label LabletimeOfSimulation;
    private ScheduledExecutorService simulationExecutor;

    private int currentTimeInMinutes;
    private List<Train> trains;
    private List<Route> routes;

    private List<Break> breaks;


    /**
     * Параметры случайных событий симуляции
     */

    private int eventPercentage;
    private int event1Chance;
    private int event2Chance;
    private int event3Chance;


    public void setEventChances(int eventPercentage, int event1Chance, int event2Chance, int event3Chance) {
        this.eventPercentage = eventPercentage;
        this.event1Chance = event1Chance;
        this.event2Chance = event2Chance;
        this.event3Chance = event3Chance;
    }

    public void setEvents() {
        Event eventGenerator = new Event(routes, trains, eventPercentage, event1Chance, event2Chance, event3Chance);
        eventGenerator.generateEvents();
    }

    /**
     * --------------------------------------------------
     */


    public void updateTrains() {
        this.trains = new ArrayList<>(timetable.getTrains());
        this.routes = new ArrayList<>(timetable.getRoutes());
    }

    public Modelling(Timetable timetable, Label timeOfSimulation, String timeStart) {
        this.timetable = timetable;
        this.LabletimeOfSimulation = timeOfSimulation;
        this.currentTimeInMinutes = timeToMinutes(timeStart);
        this.trains = new ArrayList<>(timetable.getTrains());
        this.routes = new ArrayList<>(timetable.getRoutes());

        System.out.println("общее количество поездов " + routes.size());
        setEvents();
    }

    public Modelling(Label LabeltimeOfSimulation) {
        this.LabletimeOfSimulation = LabeltimeOfSimulation;
    }

    public void setTimetable(Timetable timetable) {
        this.timetable = timetable;
        this.trains = new ArrayList<>(timetable.getTrains());
        this.routes = new ArrayList<>(timetable.getRoutes());

    }

    public void setTimeStart(String timeStart) {
        this.currentTimeInMinutes = timeToMinutes(timeStart);
    }




    public void startSimulation() {


        if (simulationExecutor != null && !simulationExecutor.isShutdown()) {
            simulationExecutor.shutdownNow(); // Остановка текущего, если он ещё активен
        }
        simulationExecutor = new ScheduledThreadPoolExecutor(1); // Создаём новый экземпляр

        updateTrains();
        setEvents();

        simulationExecutor.scheduleAtFixedRate(this::updateSimulation, 0, 1, TimeUnit.SECONDS);
    }

    private void updateSimulation() {
        Platform.runLater(() -> {
            LabletimeOfSimulation.setText(timetable.minutesToTime(currentTimeInMinutes));

            if (currentTimeInMinutes >= 1440) {
                System.out.println("Симуляция остановлена прошло 24 часа");
                stopSimulation();
                return;
            }

            currentTimeInMinutes += timetable.getSimulationStepMinutes();

            for (Train train : trains) {
                if (train.hasFinished()) continue;
                if (train.isReadyToStart(currentTimeInMinutes) && !train.isRunning()) {
                    train.createPathWithStops();
                    train.startSimulation();
                }
            }

            resolveAccidentsAndCreateNewTrains();

            checkTrainEvents();
        });
    }

    private void resolveAccidentsAndCreateNewTrains() {
        for (Train train : trains) {
            Accident accident = train.getAccident();
            if (accident != null && !accident.isResolved() && accident.getDelayTime() > 3) {

                String startStation = accident.getDepartureStationId();
                String endStation = accident.getArrivalStationId();
                String lastStation = train.getRoute().getStationList().get(train.getRoute().getStationList().size() - 1);
                int startTime = currentTimeInMinutes + 180; // 3 часа = 180 минут потом

                System.out.println("Создается новый поезд из-за длительной аварии между " + startStation + " и " + endStation);
                timetable.addRouteWithTrainPlusRT(startStation, lastStation, endStation, startTime);
                updateTrains();

                accident.resolve();
            }
        }
    }


    private void checkTrainEvents() {
        for (Train train : trains) {
            Route route = train.getRoute();
            if (route == null) continue;


            Accident accident = train.getAccident();
            if (accident != null && !train.hasFinished()) {
                //System.out.println("Accident detected on route for train " + train.getId());
            }
        }
    }


    private int timeToMinutes(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    public void stopSimulation() {
        simulationExecutor.shutdownNow();
    }

    public void resetSimulation() {

        for(Train train : trains) {
            train.generateJourneyLog();
            train.generateJourneyLogWithoutEvent();
            train.clearAllLines();
        }

        simulationExecutor.shutdownNow();
        timetable.clearAnimation();

        stopSimulation();
        trains.clear();
        routes.clear();
        currentTimeInMinutes = 0;
        Platform.runLater(() -> LabletimeOfSimulation.setText("00:00"));
    }

    public void show(){

    }

    public List<Train> getTrains() {
        updateTrains();
        return trains;
    }
}
