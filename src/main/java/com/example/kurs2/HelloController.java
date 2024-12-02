package com.example.kurs2;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloController {

    @FXML public AnchorPane formOfSimulation;
    @FXML private Button start;
    @FXML private Button stopButton;
    @FXML public Label LabeltimeOfSimulation;

    private final List<Train> trains = new ArrayList<>();

    private Route route;

    private final Map<String, Circle> stations = new HashMap<>();

    private Timetable timetable;

    private Modelling modelling ;

    private OptionsSimulation optionsSimulation;

    @FXML
    public void initialize() {

        modelling  = new Modelling();

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

        start.setDisable(false);
        stopButton.setDisable(true);

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


        /**
         * Инцилизируем каждый раз расписание внутри фнкции для удобства(полного обновления данных) и определяем его для моделирования
         */
        timetable = new Timetable(15,stations);
        timetable.setFormOfSimulation(formOfSimulation);

        modelling.setTimetable(timetable);
        modelling.setTimeStart("00:00"); //Определние начала симуляции(HH:MM)
        modelling.setLabelOfTime(LabeltimeOfSimulation);

        timetable.addRouteWithTrain("st1", "st8",15);
        //timetable.addRouteWithTrain("st1", "st12",0);
        //timetable.addRouteWithTrain("st9", "st1",60);
        //timetable.addRouteWithTrain("st10", "st7",100);



        modelling.startSimulation();

        start.setDisable(true);
        stopButton.setDisable(false);

    }

    public void stopSimulation(ActionEvent actionEvent) {
        modelling.resetSimulation();


        start.setDisable(false);
        stopButton.setDisable(true);
    }

    public void options(ActionEvent actionEvent) {
        try {
            // Загрузка FXML файла для окна настроек
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("optionsSimulation.fxml"));
            Parent root = fxmlLoader.load();

            // Создание нового окна
            Stage stage = new Stage();
            stage.setTitle("Настройки симуляции");
            stage.initModality(Modality.APPLICATION_MODAL); // Блокирует основное окно, пока открыто окно настроек
            stage.setScene(new Scene(root));

            // Получение контроллера окна настроек
            OptionsSimulation optionsController = fxmlLoader.getController();

            // Отображение окна настроек
            stage.showAndWait();

//            // Получение данных, введённых пользователем
//            System.out.println("Начальное время: " + optionsController.getStartTime());
//            System.out.println("Шаг симуляции: " + optionsController.getSimulationStep());
//            System.out.println("Процент поездов с событиями: " + optionsController.getEventPercentage());
//            System.out.println("Шанс события 1: " + optionsController.getEvent1Chance());
//            System.out.println("Шанс события 2: " + optionsController.getEvent2Chance());
//            System.out.println("Шанс события 3: " + optionsController.getEvent3Chance());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}