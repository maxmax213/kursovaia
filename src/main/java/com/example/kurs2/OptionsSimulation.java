package com.example.kurs2;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import java.util.HashSet;
import java.util.Set;

public class OptionsSimulation {

    @FXML private TextField startTimeField;
    @FXML private ComboBox<String> simulationStepBox;
    @FXML private TextField eventPercentageField;
    @FXML private TextField event1ChanceField;
    @FXML private TextField event2ChanceField;
    @FXML private TextField event3ChanceField;

    private String startTime;  // Время начала симуляции
    private int simulationStep;  // Шаг симуляции в минутах
    private int eventPercentage;  // Процент поездов с событиями
    private int event1Chance;  // Шанс события 1
    private int event2Chance;  // Шанс события 2
    private int event3Chance;  // Шанс события 3

    private List<RowTimetable> rows = new ArrayList<>();
    private final List<String> validStations = List.of(
            "st1", "st2", "st3", "st4", "st5", "st6",
            "st7", "st8", "st9", "st10", "st11", "st12", "generalST"
    );

    /**
     * Добавление таблциы
     */

    @FXML private TableView<ScheduleRecord> scheduleTable;
    @FXML private TableColumn<ScheduleRecord, String> startStationColumn;
    @FXML private TableColumn<ScheduleRecord, String> endStationColumn;
    @FXML private TableColumn<ScheduleRecord, String> departureTimeColumn;

    private ObservableList<ScheduleRecord> scheduleData;

    private boolean settingsSaved = false;//сохранение настроек

    @FXML
    public void initialize() {

        scheduleData = FXCollections.observableArrayList();
        scheduleTable.setItems(scheduleData);

        startStationColumn.setCellValueFactory(cellData -> cellData.getValue().startStationProperty());
        endStationColumn.setCellValueFactory(cellData -> cellData.getValue().endStationProperty());
        departureTimeColumn.setCellValueFactory(cellData -> cellData.getValue().departureTimeProperty());


        startStationColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        endStationColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        departureTimeColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        scheduleTable.setEditable(true);

        simulationStepBox.setValue("15 минут");
    }

    @FXML
    public void saveOptions(ActionEvent actionEvent) {

        Set<String> uniqueRecords = new HashSet<>();

        rows.clear();
        StringBuilder errors = new StringBuilder();

        startTime = validateTime(startTimeField.getText(), "Время начала", errors);
        simulationStep = parseSimulationStep(simulationStepBox.getValue(), errors);
        eventPercentage = validatePercentage(eventPercentageField, "Процент событий", errors);
        event1Chance = validatePercentage(event1ChanceField, "Шанс события 1", errors);
        event2Chance = validatePercentage(event2ChanceField, "Шанс события 2", errors);
        event3Chance = validatePercentage(event3ChanceField, "Шанс события 3", errors);

        if(scheduleData.isEmpty()){
            errors.append("Необходимо добавить на маршрут хотя бы один поезд").append("\n");
        }

        for (ScheduleRecord record : scheduleData) {
            String startStation = record.getStartStation();
            String endStation = record.getEndStation();
            String departureTime = record.getDepartureTime();

            if (!validStations.contains(startStation)) {
                errors.append("Некорректная станция отправления: ").append(startStation).append("\n");
                continue;
            }

            if (!validStations.contains(endStation)) {
                errors.append("Некорректная конечная станция: ").append(endStation).append("\n");
                continue;
            }

            int departureTimeInMinutes;
            try {
                departureTimeInMinutes = timeToMinutes(departureTime);
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                errors.append("Некорректное время отправления: ").append(departureTime).append("\n");
                continue;
            }

            String uniqueKey = startStation + "|" + endStation + "|" + departureTime;

            if (uniqueRecords.contains(uniqueKey)) {
                errors.append("Дублирующаяся запись: ").append(uniqueKey).append("\n");
                continue;
            }

            uniqueRecords.add(uniqueKey);
            rows.add(new RowTimetable(startStation, endStation, departureTimeInMinutes));
        }

        if (errors.length() > 0) {
            showAlert("Ошибки ввода", errors.toString());
        } else {

            settingsSaved = true;

            Stage stage = (Stage) startTimeField.getScene().getWindow();
            stage.close();

            resetFields();
        }
    }

    private String validateTime(String time, String fieldName, StringBuilder errors) {
        if (time == null || time.isEmpty()) {
            errors.append(fieldName).append(" не может быть пустым.\n");
            return null;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime.parse(time, formatter);
            return time;
        } catch (DateTimeParseException e) {
            errors.append(fieldName).append(" должно быть в формате HH:mm.\n");
            return null;
        }
    }

    private int parseSimulationStep(String stepText, StringBuilder errors) {
        if (stepText == null) {
            errors.append("Шаг симуляции не выбран.\n");
            return 0;
        }
        switch (stepText) {
            case "15 минут": return 15;
            case "30 минут": return 30;
            default:
                errors.append("Некорректный шаг симуляции.\n");
                return 0;
        }
    }

    private int validatePercentage(TextField field, String fieldName, StringBuilder errors) {
        String text = field.getText();
        if (text.isEmpty()) {
            errors.append(fieldName).append(" не может быть пустым.\n");
            return 0;
        }
        if (!text.matches("\\d+")) {
            errors.append(fieldName).append(" должно быть числом.\n");
            field.clear();
            return 0;
        }
        int value = Integer.parseInt(text);
        if (value < 0 || value > 100) {
            errors.append(fieldName).append(" должно быть в диапазоне от 0 до 100.\n");
            field.clear();
            return 0;
        }
        return value;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void addScheduleRecord(ActionEvent actionEvent) {
        if (scheduleData.size() >= 10) {
            showAlert("Ошибка", "Нельзя добавить больше 10 записей.");
            return;
        }
        scheduleData.add(new ScheduleRecord("st1", "st7", "00:00"));
    }


    private int timeToMinutes(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    private void resetFields() {
        startTimeField.clear();
        eventPercentageField.clear();
        event1ChanceField.clear();
        event2ChanceField.clear();
        event3ChanceField.clear();
    }

    public int getSimulationStep() {
        return simulationStep;
    }

    public String getStartTime() {
        return startTime;
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

    public List<RowTimetable> getRows() {
        return new ArrayList<>(rows); // Возвращаем копию списка
    }


    public void cancelOptions(ActionEvent actionEvent) {
        settingsSaved = false;
        Stage stage = (Stage) startTimeField.getScene().getWindow();
        stage.close();
    }


    public void removeScheduleRecord(ActionEvent actionEvent) {
        if (!scheduleData.isEmpty()) {
            scheduleData.remove(scheduleData.size() - 1);
        } else {
            showAlert("Ошибка", "Таблица пуста.");
        }
    }

    public boolean isSettingsSaved() {
        return settingsSaved;
    }
}
