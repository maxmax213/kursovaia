package com.example.kurs2;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HelloController {

    @FXML public AnchorPane formOfSimulation;
    @FXML private Button start;
    @FXML public Label timeOfSimulation;

    private final List<Train> trains = new ArrayList<>();

    private Route route;

    private final Map<String, Circle> stations = new HashMap<>();

    private Timetable timetable;

    @FXML
    public void initialize() {

        /**
         * Установка контенера для анимации и карты станций
         */

        route = new Route(stations);

        /**
         * Используем хэш-таблицу для связи точки(станции) с ее id
         */

        for (var node : formOfSimulation.getChildren()) {
            if (node instanceof Circle circle && circle.getFill().equals(Color.RED)) {
                String stationId = node.getId();
                if (stationId != null) {
                    stations.put(stationId, circle);
                }
            }
        }

        addIntermediateStations();

        timetable = new Timetable(15,stations);
        timetable.setFormOfSimulation(formOfSimulation);

    }

    private void addIntermediateStations() {
        Map<String, Circle> intermediateStations = new HashMap<>();

        intermediateStations.put("st1_st2", new Circle(91.0, 185.0, 1));
        intermediateStations.put("st2_st3", new Circle(143.0, 185.0, 1));
        intermediateStations.put("st3_st4", new Circle(198.0, 185.0, 1));
        intermediateStations.put("st4_generalST", new Circle(266.0, 185.0, 1));
        intermediateStations.put("st11_generalST", new Circle(308.0, 200.0, 1));
        intermediateStations.put("st11_st12", new Circle(317.0, 238.0, 1));
        intermediateStations.put("generalST_st5", new Circle(326.0, 185.0, 1));
        intermediateStations.put("st5_st6", new Circle(374.0, 185.0, 1));
        intermediateStations.put("st6_st7", new Circle(430.0, 185.0, 1));
        intermediateStations.put("st10_generalST", new Circle(301.0, 169.0, 1));
        intermediateStations.put("st9_st10", new Circle(293.0, 133.0, 1));
        intermediateStations.put("st8_st9", new Circle(285.0, 98.0, 1));


        for (var entry : intermediateStations.entrySet()) {
            String id = entry.getKey();
            Circle circle = entry.getValue();
            circle.setId(id); // Установка ID для промежуточной станции
            stations.put(id, circle);
        }
    }

    public void startSimulation(ActionEvent actionEvent) {
        List<String> directRoute = route.buildDirectRoute("st1", "st8");
        System.out.println("Прямой маршрут от st1 к st7: " + directRoute);
        timetable.addRouteWithTrain("st1", "st8",15);
        timetable.addRouteWithTrain("st1", "st12",0);
        timetable.addRouteWithTrain("st9", "st1",60);
        //timetable.addRouteWithTrain("st10", "st7",100);


        Modelling modelling = new Modelling(timetable, timeOfSimulation,"00:00");

        modelling.startSimulation();


//            List<String> routeWithIntermediate = route.buildRouteWithIntermediate("st1", "st12", "st2");
//        System.out.println("Маршрут с промежуточной станцией : " + routeWithIntermediate);

    }


}