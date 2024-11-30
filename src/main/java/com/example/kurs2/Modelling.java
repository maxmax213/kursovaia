package com.example.kurs2;

import javafx.application.Platform;
import javafx.scene.control.Label;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class Modelling {
    private final Timetable timetable;
    private final Label timeOfSimulation;
    private final ScheduledExecutorService simulationExecutor;

    private int currentTimeInMinutes;
    private List<Train> trains;
    private List<Route> routes;

    private List<Break> breaks;

    public void updateTrains(){
        this.trains = new ArrayList<>(timetable.getTrains());
    }

    public Modelling(Timetable timetable, Label timeOfSimulation, String timeStart) {
        this.timetable = timetable;
        this.timeOfSimulation = timeOfSimulation;
        this.currentTimeInMinutes = timeToMinutes(timeStart);
        this.simulationExecutor = Executors.newScheduledThreadPool(1);
        this.trains = new ArrayList<>(timetable.getTrains());
        this.routes = new ArrayList<>(timetable.getRoutes());

        System.out.println("общее количество поездов " + routes.size());
        setEvents();
    }

    public void setEvents() {
        Event eventGenerator = new Event(routes, trains, 50, 30, 20, 20);
        eventGenerator.generateEvents();
    }

    public void startSimulation() {
        simulationExecutor.scheduleAtFixedRate(this::updateSimulation, 0, 1, TimeUnit.SECONDS);
    }

    private void updateSimulation() {
        Platform.runLater(() -> {
            updateTrains();
            timeOfSimulation.setText(timetable.minutesToTime(currentTimeInMinutes));

            if (currentTimeInMinutes >= 1440) {
                System.out.println("Simulation stopped: Time exceeded 24 hours.");
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
                int startTime = currentTimeInMinutes + 180; // 3 часа = 180 минут

                System.out.println("Создается новый поезд из-за длительной аварии между " + startStation + " и " + endStation);
                timetable.addRouteWithTrainPlusRT(startStation, lastStation, endStation, startTime);

                // Отмечаем аварию как разрешенную
                accident.resolve();
            }
        }
    }



    private void checkTrainEvents() {
        for (Train train : trains) {
            Route route = train.getRoute();
            if (route == null) continue;

            // Проверяем наличие аварии
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
}
