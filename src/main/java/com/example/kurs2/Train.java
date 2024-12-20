package com.example.kurs2;

import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.animation.PathTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Train {

    /**
     * Системные поля:
     * transition - инцилизация потоковой анимации
     * Circle train - точка которой будет поезд
     * AnchorPane formOfSimulation - контенер по которому будут двигаться поезда
     * stations - это карта которая связывает id станции(типа string) c точкой(координатоми)
     * first - флаг для сохранения корректной дорожной карты
     * hasFinished - флаг завершения симуляции для избежания ошибок
     * currentSimulationTime - отслеживания времени симуляции
     * journeyLog - фиксация времени прибытия на станцию
     */

    private final SequentialTransition transition;
    private final Circle train;
    private final AnchorPane formOfSimulation;
    private final Map<String, Circle> stations;
    boolean first = true;
    private boolean hasFinished = false;
    private int currentSimulationTime;
    private final JourneyLog journey = new JourneyLog();
    private final JourneyLog journeyNoEvent = new JourneyLog();


    public List<JourneyLog.StationLogEntry> getJourneyLog() {
        return journey.getEntries();
    }

    public List<JourneyLog.StationLogEntry> getJourneyLogWithNoEvent() {
        return journeyNoEvent.getEntries();
    }



    /**
     * Параметры поезда:
     * id - номер поезда
     * route - маршрут движения поезда
     * startTime - время начала симуляции(в формате int)
     * speed - параметр скорости перемешения поездов. Если simulationStepMinutes == 15 , то speed = 1 , иначе speed = 0.5
     */

    private int id;
    private Route route;
    public final int startTime;
    private double speed;

    /**
     * События:
     * Accident - поломка поезда
     * delays - задержки на станциях
     * break - участки, где поезду нужно снизить скорость
     */

    private Accident accident;
    private final List<Delay> delays = new ArrayList<>();
    private List<Break> breaks = new ArrayList<>();
    private final List<Line> pathLines = new ArrayList<>();

    public Train(int id, AnchorPane formOfSimulation, Map<String, Circle> stations, int startTime, int simulationStepMinutes) {
        this.id = id;
        this.train = new Circle(3, Color.GREEN);
        this.transition = new SequentialTransition();
        this.formOfSimulation = formOfSimulation;
        this.stations = stations;
        this.startTime = startTime;
        this.currentSimulationTime = startTime;
        if (simulationStepMinutes == 15) {
            this.speed = 1;
        } else this.speed = 0.5;


        journey.setTrainId(id);
        journeyNoEvent.setTrainId(id);
    }


    /**
     * Расписание поезда учитывая все события и без них
     * Для удобства будем считать что путь от одной станции к другой при нормальных условиях 60 минут а стоянка 15 минут
     */
    public void generateJourneyLogWithoutEvent() {

        List<String> stationIds = route.getStationList();
        int simulationTime = startTime;


        Circle startStation = stations.get(stationIds.get(0));
        String depTime = calculateSimulationTime(simulationTime);
        journeyNoEvent.addEntry(stationIds.get(0), "N/A", "Normal");
        journeyNoEvent.updateEntry(stationIds.get(0), depTime, "Normal");


        for (int i = 1; i < stationIds.size() - 1; i++) {
            String status = "Normal";
            int timeOfPath = 0;
            int timeOfStay = 0;

            String currentStationId = stationIds.get(i);
            String nextStationId = stationIds.get(i + 1);

            Circle currentStation = stations.get(currentStationId);


            timeOfPath += 60; // Стандартное время пути
            timeOfStay = 15;


            simulationTime += timeOfPath;

            String arrivalTime = calculateSimulationTime(simulationTime);
            journeyNoEvent.addEntry(currentStationId, arrivalTime, status);

            simulationTime += timeOfStay;

            String departureTime = calculateSimulationTime(simulationTime);
            journeyNoEvent.updateEntry(currentStationId, departureTime, status);
        }


        String secondLastStationId = stationIds.get(stationIds.size() - 2);
        String lastStationId = stationIds.get(stationIds.size() - 1);

        Circle secondLastStation = stations.get(secondLastStationId);

        String status = "Normal";
        int timeOfPath = 60;

        simulationTime += timeOfPath;
        String arrivalTime = calculateSimulationTime(simulationTime);
        journeyNoEvent.addEntry(lastStationId, arrivalTime, status);

    }

    public void generateJourneyLog() {


        List<String> stationIds = route.getStationList();
        int simulationTime = startTime;


        Circle startStation = stations.get(stationIds.get(0));
        String depTime = calculateSimulationTime(simulationTime);
        journey.addEntry(stationIds.get(0), "N/A", "Normal");
        journey.updateEntry(stationIds.get(0), depTime, "Normal");


        for (int i = 1; i < stationIds.size() - 1; i++) {
            String status = "Normal";
            int timeOfPath = 0;
            int timeOfStay = 0;

            String currentStationId = stationIds.get(i);
            String nextStationId = stationIds.get(i + 1);

            Circle currentStation = stations.get(currentStationId);

            // Проверка на аварии, задержки и ограничения скорости
            Break speedLimitBreak = breaks.stream()
                    .filter(b -> b.getDepartureStationId().equals(currentStationId) &&
                            b.getArrivalStationId().equals(nextStationId))
                    .findFirst()
                    .orElse(null);

            boolean hasAccident = accident != null &&
                    accident.getDepartureStationId().equals(currentStationId) &&
                    accident.getArrivalStationId().equals(nextStationId);

            Delay delay = delays.stream()
                    .filter(d -> d.getStationId().equals(currentStationId))
                    .findFirst()
                    .orElse(null);

            if (hasAccident) {
                if (accident.getDelayTime() > 3) {
                    journey.addEntry(currentStationId, calculateSimulationTime(simulationTime), "Breakdown");
                    break;
                } else {
                    status = "Breakdown";
                    timeOfPath += accident.getDelayTime() * 60;
                }
            }

            if (speedLimitBreak != null) {
                timeOfPath += 60 * 3; // Ограничение скорости
                status = "Break";
            } else {
                timeOfPath += 60; // Стандартное время пути
            }

            if (delay != null) {
                status = status.equals("Normal") ? "Delay" : status + "-Delay";
                timeOfStay = 15+ delay.getDelayTime();
            } else {
                timeOfStay = 15;
            }

            simulationTime += timeOfPath;

            String arrivalTime = calculateSimulationTime(simulationTime);
            journey.addEntry(currentStationId, arrivalTime, status);

            simulationTime += timeOfStay;

            String departureTime = calculateSimulationTime(simulationTime);
            journey.updateEntry(currentStationId, departureTime, status);
        }


        String secondLastStationId = stationIds.get(stationIds.size() - 2);
        String lastStationId = stationIds.get(stationIds.size() - 1);

        Circle secondLastStation = stations.get(secondLastStationId);

        String status = "Normal";
        int timeOfPath = 60; // Стандартное время пути до последней станции


        boolean hasAccidentBetweenLastStations = accident != null &&
                accident.getDepartureStationId().equals(secondLastStationId) &&
                accident.getArrivalStationId().equals(lastStationId);

        if (hasAccidentBetweenLastStations) {
            if (accident.getDelayTime() > 3) {
                journey.addEntry(secondLastStationId, calculateSimulationTime(simulationTime), "Breakdown");
                System.out.println("Generated Journey Log for Train " + id + ":");
                for (JourneyLog.StationLogEntry entry : journey.getEntries()) {
                    System.out.println(entry);
                }
                return;
            } else {
                status = "Breakdown";
                timeOfPath += accident.getDelayTime() * 60;
            }
        }

        Break speedLimitBreak = breaks.stream()
                .filter(b -> b.getDepartureStationId().equals(secondLastStationId) &&
                        b.getArrivalStationId().equals(lastStationId))
                .findFirst()
                .orElse(null);

        if (speedLimitBreak != null) {
            timeOfPath += 60 * 3;
            status = "Break";
        }

        simulationTime += timeOfPath;
        String arrivalTime = calculateSimulationTime(simulationTime);
        journey.addEntry(lastStationId, arrivalTime, status);

    }




    /**
     * --------------------------------------------------
     */


    public void createPathWithStops() {

        boolean flag = false;

        List<String> stationIds = route.getStationList();

        transition.getChildren().clear();

        Circle startStation = stations.get(stationIds.getFirst());
        train.setTranslateX(startStation.getLayoutX());
        train.setTranslateY(startStation.getLayoutY());

        if (!formOfSimulation.getChildren().contains(train)) {
            formOfSimulation.getChildren().add(train);
        }

        for (int i = 0; i < stationIds.size() - 1; i++) {
            String currentStationId = stationIds.get(i);
            String nextStationId = stationIds.get(i + 1);

            Circle currentStation = stations.get(currentStationId);
            Circle nextStation = stations.get(nextStationId);

            Delay delay = delays.stream()
                    .filter(d -> d.getStationId().equals(currentStationId))
                    .findFirst()
                    .orElse(null);

            /**
             * Проблема возникает из-за того, что у промежуточных станций и основных станций используются разные координаты.
             * Вычисляем коректные координаты точек
             */

            double currentX = isIntermediateStation(currentStationId) ? currentStation.getCenterX() : currentStation.getLayoutX();
            double currentY = isIntermediateStation(currentStationId) ? currentStation.getCenterY() : currentStation.getLayoutY();
            double nextX = isIntermediateStation(nextStationId) ? nextStation.getCenterX() : nextStation.getLayoutX();
            double nextY = isIntermediateStation(nextStationId) ? nextStation.getCenterY() : nextStation.getLayoutY();

            Path path = new Path(
                    new MoveTo(currentX, currentY),
                    new LineTo(nextX, nextY)
            );

            PathTransition pathTransition = new PathTransition();
            pathTransition.setNode(train);
            pathTransition.setPath(path);



            /**
             * Определение событий
             * Если simulationStepMinutes == 15 , то speed = 1 , иначе speed = 0.5
             */

            double travelDuration = 4 * speed;

            boolean hasAccident = accident != null
                    && accident.getDepartureStationId().equals(currentStationId)
                    && accident.getArrivalStationId().equals(nextStationId);

            if (hasAccident) {
                Circle intermediateStation = findIntermediateStation(stationIds.get(i), stationIds.get(i + 1));
                if (intermediateStation == null) {
                    System.out.println("Не найдена промежуточная станция");
                    return;
                }

                handleAccident(currentStation, nextStation, pathTransition, intermediateStation);

                if (accident.getDelayTime() > 3) {
                    System.out.println("Больше 4 часов");
                    break;
                }

                continue;
            }

            Break speedLimitBreak = breaks.stream()
                    .filter(b -> b.getDepartureStationId().equals(currentStationId) &&
                            b.getArrivalStationId().equals(nextStationId))
                    .findFirst()
                    .orElse(null);


            if (speedLimitBreak != null) {
                System.out.println("Ограничение скорости между станциями " + currentStationId + " и " + nextStationId);

                Line trackLine = new Line(currentX, currentY, nextX, nextY);

                boolean lineExists = pathLines.stream().anyMatch(line ->
                        line.getStartX() == trackLine.getStartX() &&
                                line.getStartY() == trackLine.getStartY() &&
                                line.getEndX() == trackLine.getEndX() &&
                                line.getEndY() == trackLine.getEndY()
                );



                if (!lineExists) {
                    pathLines.add(trackLine); // Добавляем линию в список
                    formOfSimulation.getChildren().add(1, trackLine); // Отображаем линию
                    trackLine.setStroke(Color.BLUE);
                    trackLine.setStrokeWidth(4);
                }

                pathTransition.setDuration(Duration.seconds(travelDuration * 3));

            } else {

                pathTransition.setDuration(Duration.seconds(travelDuration));

            }



            if (delay != null) {
                PauseTransition prePause = new PauseTransition(Duration.ZERO);
                prePause.setOnFinished(event -> train.setFill(Color.YELLOW));

                PauseTransition delayPause = new PauseTransition(Duration.seconds((double) delay.getDelayTime() / 15 * speed));
                delayPause.setOnFinished(event -> train.setFill(Color.GREEN));



                transition.getChildren().addAll(prePause,delayPause, pathTransition);

            } else {

                PauseTransition standardPause = new PauseTransition(Duration.seconds(speed)); // Стандартная пауза
                standardPause.setOnFinished(event -> train.setFill(Color.GREEN));

                transition.getChildren().addAll(pathTransition, standardPause);
            }
        }

        transition.setOnFinished(e -> {
            formOfSimulation.getChildren().remove(train);
            hasFinished = true;
        });
    }

    private void handleAccident(Circle currentStation, Circle nextStation, PathTransition pathTransition, Circle intermediateStation) {


        PathTransition toIntermediate = new PathTransition();
        toIntermediate.setNode(train);

        toIntermediate.setPath(new Path(
                new MoveTo(currentStation.getLayoutX(), currentStation.getLayoutY()),
                new LineTo(intermediateStation.getCenterX(), intermediateStation.getCenterY())
        ));

        toIntermediate.setDuration(Duration.seconds(speed));

        toIntermediate.setOnFinished(event -> {
            train.setFill(Color.BLACK);
        });


        PauseTransition delayPause = new PauseTransition(Duration.seconds(accident.getDelayTime() * 4));



        if (accident.getDelayTime() > 3) {


            PathTransition returnToCurrent = new PathTransition();
            returnToCurrent.setNode(train);

            returnToCurrent.setPath(new Path(
                    new MoveTo(intermediateStation.getCenterX(), intermediateStation.getCenterY()),
                    new LineTo(currentStation.getLayoutX(), currentStation.getLayoutY())
            ));


            returnToCurrent.setDuration(Duration.seconds(speed * 2));
            transition.getChildren().addAll(toIntermediate, delayPause, returnToCurrent);



            first = false;

        } else {

            delayPause.setOnFinished(event -> {
                train.setFill(Color.GREEN);
            });

            PathTransition returnToPath = new PathTransition();
            returnToPath.setNode(train);
            returnToPath.setPath(new Path(
                    new MoveTo(intermediateStation.getCenterX(), intermediateStation.getCenterY()),
                    new LineTo(nextStation.getLayoutX(), nextStation.getLayoutY())
            ));
            returnToPath.setDuration(Duration.seconds(speed));

            transition.getChildren().addAll(toIntermediate, delayPause, returnToPath);

        }
    }




    private String calculateSimulationTime(int simulationMinutes) {
        int hours = simulationMinutes / 60;
        int minutes = simulationMinutes % 60;
        return String.format("%02d:%02d", hours, minutes);
    }

    public boolean isReadyToStart(int currentTime) {
        return currentTime >= startTime;
    }

    public boolean isRunning() {
        return transition.getStatus() == Animation.Status.RUNNING;
    }

    public void setRoute(Route route) {
        this.route = route;
        System.out.println("== " + route.getStationList());
    }

    public void startSimulation() {
        transition.play();
    }

    public String getId() {
        return Integer.toString(id);
    }

    public Route getRoute() {
        return route;
    }

    public boolean hasFinished() {
        return hasFinished;
    }

    public void addDelay(Delay delay) {
        delays.add(delay);
    }

    private Circle findIntermediateStation(String departureStation, String arrivalStation) {

        Map<String, Circle> intermediateStations = route.getIntermediateStations();

        String key = departureStation + "_" + arrivalStation; // Ключ в прямом порядке
        Circle intermediate = intermediateStations.get(key);

        if (intermediate == null) {
            // Пробуем обратный порядок (на случай ошибок)
            key = arrivalStation + "_" + departureStation;
            intermediate = intermediateStations.get(key);
        }

        return intermediate;
    }

    public void setAccident(Accident accident) {
        this.accident = accident;
    }

    public Accident getAccident() {
        return accident;
    }

    public void setBreaks(List<Break> br) {
        this.breaks = new ArrayList<>(br);
    }

    public List<Delay> getDelays(List<Break> breaks) {
        return delays;
    }

    public void addBreak(Break br) {
        breaks.add(br);
    }

    private boolean isIntermediateStation(String stationId) {
        Map<String, Circle> intermediateStations = route.getIntermediateStations();
        return intermediateStations.containsKey(stationId);
    }

    public String getLastST() {
        return route.getStationList().getLast();
    }

    public int getStartTime() {
        return startTime;
    }

    public Circle getTrainNode() {
        return train;
    }

    public void setColor(){
        train.setFill(Color.YELLOWGREEN);
    }

    public void clearAllLines() {
        formOfSimulation.getChildren().removeAll(pathLines);
        pathLines.clear();
    }

    public int getSummaryDelay(){
        int sum = 0;
        for(Delay delay : delays){
            sum+=delay.getDelayTime();
        }

        return sum;
    }

    public int sizeOfBreak(){
        return breaks.size();
    }
}
