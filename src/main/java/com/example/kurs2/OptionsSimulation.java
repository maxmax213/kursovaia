package com.example.kurs2;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OptionsSimulation {

    @FXML
    private TextField startTimeField;
    @FXML
    private ComboBox<String> simulationStepBox;
    @FXML
    private TextField eventPercentageField;
    @FXML
    private TextField event1ChanceField;
    @FXML
    private TextField event2ChanceField;
    @FXML
    private TextField event3ChanceField;
    @FXML
    private TableView<ScheduleRecord> scheduleTable;
    @FXML
    private TableColumn<ScheduleRecord, String> startStationColumn;
    @FXML
    private TableColumn<ScheduleRecord, String> endStationColumn;
    @FXML
    private TableColumn<ScheduleRecord, String> departureTimeColumn;

    private final ObservableList<ScheduleRecord> scheduleData = FXCollections.observableArrayList();

    private final Map<String, List<String>> stationMap = new HashMap<>();
    private String startTime;
    private int simulationStep;
    private int eventPercentage;
    private int event1Chance;
    private int event2Chance;
    private int event3Chance;

    @FXML
    public void initialize() {
        initializeStationMap();

        simulationStepBox.setItems(FXCollections.observableArrayList("15 минут", "30 минут"));
        setupScheduleTable();
        scheduleTable.setItems(scheduleData);
        addScheduleRecord();
    }

    private void initializeStationMap() {
        stationMap.put("st1", List.of("st2"));
        stationMap.put("st2", List.of("st1", "st3"));
        stationMap.put("st3", List.of("st2", "st4"));
        stationMap.put("st4", List.of("st3", "generalST"));
        stationMap.put("generalST", List.of("st4", "st5", "st10", "st11"));
        stationMap.put("st5", List.of("generalST", "st6"));
        stationMap.put("st6", List.of("st5", "st7"));
        stationMap.put("st7", List.of("st6"));
        stationMap.put("st8", List.of("st9"));
        stationMap.put("st9", List.of("st8", "st10"));
        stationMap.put("st10", List.of("generalST", "st9"));
        stationMap.put("st11", List.of("st12"));
        stationMap.put("st12", List.of("st11"));
    }

    private void setupScheduleTable() {

        scheduleTable.setEditable(true);

        // Указываем, как значения отображаются
        startStationColumn.setCellValueFactory(data -> data.getValue().startStationProperty());
        endStationColumn.setCellValueFactory(data -> data.getValue().endStationProperty());
        departureTimeColumn.setCellValueFactory(data -> data.getValue().departureTimeProperty());


        startStationColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        startStationColumn.setOnEditCommit(event -> {
            String newValue = event.getNewValue();
            if (isValidStation(newValue)) {
                event.getRowValue().setStartStation(newValue);
            } else {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Некорректная станция: " + newValue);
                scheduleTable.refresh();
            }
        });

        endStationColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        endStationColumn.setOnEditCommit(event -> {
            String newValue = event.getNewValue();
            if (isValidStation(newValue)) {
                event.getRowValue().setEndStation(newValue);
            } else {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Некорректная станция: " + newValue);
                scheduleTable.refresh();
            }
        });

        departureTimeColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        departureTimeColumn.setOnEditCommit(event -> {
            String newValue = event.getNewValue();
            if (isValidTime(newValue)) {
                event.getRowValue().setDepartureTime(newValue);
            } else {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Некорректное время: " + newValue);
                scheduleTable.refresh();
            }
        });
    }



    @FXML
    private void saveStartTime() {
        String inputTime = startTimeField.getText();
        if (isValidTime(inputTime)) {
            startTime = inputTime;
            simulationStep = simulationStepBox.getValue().equals("15 минут") ? 15 : 30;
            showAlert(Alert.AlertType.INFORMATION, "Сохранено", "Время начала: " + startTime + ", шаг: " + simulationStep);
        } else {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Некорректное время. Используйте формат HH:MM.");
        }
    }


    @FXML
    private void addScheduleRecord() {
        ScheduleRecord newRecord = new ScheduleRecord("", "", "");
        scheduleData.add(newRecord);
        scheduleTable.scrollTo(newRecord); // Прокрутка к добавленной строке
    }


    @FXML
    public void validateAndSaveParameters() {
        try {
            eventPercentage = Integer.parseInt(eventPercentageField.getText());
            event1Chance = Integer.parseInt(event1ChanceField.getText());
            event2Chance = Integer.parseInt(event2ChanceField.getText());
            event3Chance = Integer.parseInt(event3ChanceField.getText());

            if (isValidPercentage(eventPercentage) && isValidPercentage(event1Chance)
                    && isValidPercentage(event2Chance) && isValidPercentage(event3Chance)) {
                showAlert(Alert.AlertType.INFORMATION, "Сохранено", "Параметры событий успешно сохранены.");
            } else {
                throw new NumberFormatException("Значения должны быть от 0 до 100.");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Некорректные данные. Убедитесь, что значения от 0 до 100.");
        }
    }

    private boolean isValidTime(String time) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime.parse(time, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }


    private boolean isValidPercentage(int value) {
        return value >= 0 && value <= 100;
    }

    @FXML
    private void validateAndSaveSchedule() {
        for (ScheduleRecord record : scheduleData) {
            String startStation = record.getStartStation();
            String endStation = record.getEndStation();
            String departureTime = record.getDepartureTime();

            if (!stationMap.containsKey(startStation) || !stationMap.get(startStation).contains(endStation)) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Недопустимые станции: " + startStation + " -> " + endStation);
                return;
            }

            if (!isValidTime(departureTime)) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Некорректное время отправления: " + departureTime);
                return;
            }
        }
        showAlert(Alert.AlertType.INFORMATION, "Сохранено", "Все записи успешно сохранены.");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public ObservableList<ScheduleRecord> getScheduleData() {
        return scheduleData;
    }

    public String getStartTime() {
        return startTime;
    }

    public int getSimulationStep() {
        return simulationStep;
    }

    public int getEventPercentage() {
        return eventPercentage;
    }

    public int getEvent1Chance() {
        return event1Chance;
    }

    public int getEvent2Chance() {
        return event2Chance;
    }

    public int getEvent3Chance() {
        return event3Chance;
    }

    public void saveOptions(ActionEvent actionEvent) {
    }

    private final List<String> validStations = List.of(
            "st1", "st2", "st3", "st4", "st5", "st6",
            "st7", "st8", "st9", "st10", "st11", "st12", "generalST"
    );

    private boolean isValidStation(String station) {
        return validStations.contains(station);
    }


}
