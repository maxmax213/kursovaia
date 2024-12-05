package com.example.kurs2;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
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
    private OptionsSimulation options;

    private List<Train> trains = new ArrayList<>();

    private Route route;

    private final Map<String, Circle> stations = new HashMap<>();

    private Timetable timetable;

    private Modelling modelling ;

    private OptionsSimulation optionsSimulation;


    /**
     * Параметры симуляции которые загружаются из настроек симуляции
     */


    String startTime = "00:00";
    int step = 15;
    int eventPercentage = 100;
    int event1Chance = 100;
    int event2Chance = 0;
    int event3Chance = 0;
    List<RowTimetable> rows = new ArrayList<>();




    /**
     * --------------------------------------------------
     */

    @FXML
    public void initialize() {

        setDefaultValues();


        modelling  = new Modelling(LabeltimeOfSimulation);

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

    private void setDefaultValues() {

        rows.add(new RowTimetable("st1", "st7",  timeToMinutes(startTime)));
    }

    private int timeToMinutes(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
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
        timetable = new Timetable(step,stations);
        timetable.setFormOfSimulation(formOfSimulation);

        modelling.setTimetable(timetable);
        modelling.setTimeStart(startTime); //Определние начала симуляции(HH:MM)


        System.out.println("Полученные настройки:");
        System.out.println("Время начала: " + startTime);
        System.out.println("Процент событий: " + eventPercentage);
        System.out.println("Шанс события 1: " + event1Chance);
        System.out.println("Шанс события 2: " + event2Chance);
        System.out.println("Шанс события 3: " + event3Chance);

        modelling.setEventChances(eventPercentage, event1Chance, event2Chance, event3Chance);


        for (RowTimetable row : rows) {
            timetable.addRouteWithTrain(row.getStartStation(), row.getEndStation(), row.getDepartureTime());
        }


        //timetable.addRouteWithTrain("st3", "st4",0);



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

            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("optionsSimulation.fxml"));
            Parent root = fxmlLoader.load();

            optionsSimulation = fxmlLoader.getController();



            Stage stage = new Stage();
            stage.setTitle("Настройки симуляции");
            stage.initModality(Modality.APPLICATION_MODAL); // Блокирует основное окно, пока открыто окно настроек
            stage.setScene(new Scene(root));
            stage.showAndWait();

            if (optionsSimulation != null && optionsSimulation.isSettingsSaved()) {
                processOptions();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processOptions() {

        startTime = optionsSimulation.getStartTime();
        step = optionsSimulation.getSimulationStep();
        eventPercentage = optionsSimulation.getEventPercentage();
        event1Chance = optionsSimulation.getEvent1Chance();
        event2Chance = optionsSimulation.getEvent2Chance();
        event3Chance = optionsSimulation.getEvent3Chance();

        rows.clear();
        rows = optionsSimulation.getRows();

//        // Вывод в консоль
//        System.out.println("Полученные настройки:");
//        System.out.println("Время начала: " + startTime);
//        System.out.println("Процент событий: " + eventPercentage);
//        System.out.println("Шанс события 1: " + event1Chance);
//        System.out.println("Шанс события 2: " + event2Chance);
//        System.out.println("Шанс события 3: " + event3Chance);

//        System.out.println("Маршруты:");
//        for (RowTimetable row : rows) {
//            System.out.println(row);
//        }

    }

    public void showTimetable(ActionEvent actionEvent) {
        if(timetable == null){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText(null);
            alert.setContentText("Симуляция еще не была запущена.");
            alert.showAndWait();
            return;
        }

        trains = new ArrayList<>(modelling.getTrains());

        if (trains == null || trains.isEmpty() || timetable == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText(null);
            alert.setContentText("Список поездов пуст.");
            alert.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("trainInfo.fxml"));
            Parent root = loader.load();

            TrainInfo controller = loader.getController();

            // Устанавливаем список поездов перед отображением окна
            if (trains != null) {
                controller.setTrains(trains);
            } else {
                System.out.println("Список поездов пуст (trains == null)");
            }

            Stage stage = new Stage();
            stage.setTitle("Информация о поездах");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}