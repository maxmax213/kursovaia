package com.example.kurs2;

import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;

import java.util.*;

public class Timetable {

    /**
     * Добавлям базовые параметры расписания инцелизируемые вне конструктора: список всех маршрутов поездов и формы анимации
     */

    private final List<Train> trains = new ArrayList<>();
    private final List<Route> routes = new ArrayList<>();//Список всех маршрутов по расписанию
    private AnchorPane formOfSimulation;//Контейнер в котором происходит симуляция

    /**
     * simulationStepMinutes - указывается шаг симуляции(15 или 30 минут)
     * Map stations - это карта которая связывает id станции(типа string) c точкой(координатоми)
     */

    private final int simulationStepMinutes;
    private final Map<String, Circle> stations;


    public void setFormOfSimulation(AnchorPane formOfSimulation) {
        this.formOfSimulation = formOfSimulation;
    }

    public Timetable(int simulationStepMinutes, Map<String, Circle> stations) {
        this.simulationStepMinutes = simulationStepMinutes;
        this.stations = stations;
    }

    public void addRouteWithTrain(String startStationId, String endStationId, int startTime) {
        Route newRoute = new Route(stations);
        List<String> listST = newRoute.buildDirectRoute(startStationId, endStationId);
        System.out.println("buildDirectRoute result: " + listST);
        newRoute.setLiastST(listST);
        System.out.println("Route after setLiastST: " + newRoute.getStationList());
        routes.add(newRoute);

        Train train = new Train(trains.size() + 1, formOfSimulation, stations, startTime, simulationStepMinutes);
        train.setRoute(newRoute);
        trains.add(train);
    }

    public void addRouteWithTrainPlusRT(String startStationId, String endStationId, String arrivalStation ,int startTime) {
        Route newRoute = new Route(stations);
        List<String> listST = newRoute.buildRouteWithIntermediate(startStationId, endStationId, arrivalStation);
        System.out.println("buildDirectRoute result: " + listST);
        newRoute.setLiastST(listST);
        System.out.println("Route after setLiastST: " + newRoute.getStationList());
        routes.add(newRoute);

        Train train = new Train(trains.size() + 1, formOfSimulation, stations, startTime, simulationStepMinutes);
        train.setRoute(newRoute);
        trains.add(train);

//        train.createPathWithStops();
//        train.startSimulation();
    }


    public List<Route> getRoutes() {
        return routes;
    }

    public List<Train> getTrains() {
        return trains;
    }

    public int getSimulationStepMinutes() {
        return simulationStepMinutes;
    }

    private int timeToMinutes(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    public String minutesToTime(int minutes) {
        int hours = minutes / 60;
        int mins = minutes % 60;
        return String.format("%02d:%02d", hours, mins);
    }

    public void clearAnimation(){
        if (formOfSimulation != null) {
            formOfSimulation.getChildren().removeIf(node ->
                    node instanceof Circle && trains.stream().anyMatch(train -> train.getTrainNode() == node)
            );
        }
    }

}