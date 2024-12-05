package com.example.kurs2;


import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;
import java.util.List;

public class TrainInfo {

    @FXML private TextField trainIdField;
    @FXML private TableView<JourneyLog.StationLogEntry> tableTrainDetails;
    @FXML private TableColumn<JourneyLog.StationLogEntry, String> trainStationColumn;
    @FXML private TableColumn<JourneyLog.StationLogEntry, String> trainArrivalColumn;
    @FXML private TableColumn<JourneyLog.StationLogEntry, String> trainDepartureColumn;
    @FXML private TableColumn<JourneyLog.StationLogEntry, String> trainStatusColumn;
    @FXML private TableColumn<JourneyLog.StationLogEntry, String> trainIdColumn;

    @FXML private RadioButton swapPlan;
    @FXML private RadioButton swapEntries;

    @FXML private Label totalEventsLabel;
    @FXML private Label totalDelayTimeLabel;
    @FXML private Label totalBreaksLabel;


    JourneyLog log = new JourneyLog();

    private List<Train> trains;  // Список всех поездов

    public void setTrains(List<Train> trains) {
        this.trains = trains;
        displayAllTrains();
        updateSummary();
    }

    @FXML
    public void initialize() {

        trainStationColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStationName()));
        trainArrivalColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getArrivalTime()));
        trainDepartureColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDepartureTime()));
        trainStatusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        trainIdColumn.setCellValueFactory(data -> new SimpleStringProperty(Integer.toString(data.getValue().getTrainId())));

        ToggleGroup toggleGroup = new ToggleGroup();
        swapPlan.setToggleGroup(toggleGroup);
        swapEntries.setToggleGroup(toggleGroup);

        swapEntries.setSelected(true);

        toggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> displayAllTrains());
    }

    private void displayAllTrains() {
        tableTrainDetails.getItems().clear();

        if (trains == null || trains.isEmpty()) {
            System.out.println("Поезда отсутствуют.");
            return;
        }

        if (swapPlan.isSelected()) {
            System.out.println("displayAllTrains: Отображаем записи без событий.");
            for (Train train : trains) {
                List<JourneyLog.StationLogEntry> entries = train.getJourneyLogWithNoEvent();
                if (entries.isEmpty()) {
                    System.out.println("Поезд " + train.getId() + " не имеет записей без событий.");
                } else {
                    System.out.println("Поезд " + train.getId() + " записи без событий: " + entries.size());
                }
                tableTrainDetails.getItems().addAll(entries);
            }
        } else {
            System.out.println("displayAllTrains: Отображаем все записи.");
            for (Train train : trains) {
                List<JourneyLog.StationLogEntry> entries = train.getJourneyLog();
                if (entries.isEmpty()) {
                    System.out.println("Поезд " + train.getId() + " не имеет записей.");
                } else {
                    System.out.println("Поезд " + train.getId() + " записи: " + entries.size());
                }
                tableTrainDetails.getItems().addAll(entries);
            }
        }
    }


    @FXML
    public void searchTrain() {
        String trainIdText = trainIdField.getText().trim();
        if (trainIdText.isEmpty()) {
            showAlert("Ошибка", "Введите ID поезда для поиска.");
            return;
        }

        try {
            int trainId = Integer.parseInt(trainIdText);
            for (Train train : trains) {
                int id = Integer.parseInt(train.getId());
                if (id == trainId) {
                    displayTrainDetails(train);
                    return;
                }
            }
            showAlert("Поиск", "Поезд с ID " + trainId + " не найден.");
        } catch (NumberFormatException e) {
            showAlert("Ошибка", "ID поезда должен быть числом.");
        }
    }

    private void displayTrainDetails(Train train) {
        tableTrainDetails.getItems().clear();

        if (swapPlan.isSelected()) {
            tableTrainDetails.getItems().addAll(train.getJourneyLogWithNoEvent());
        } else {
            tableTrainDetails.getItems().addAll(train.getJourneyLog());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void updateSummary() {
        if (trains == null || trains.isEmpty()) {
            totalEventsLabel.setText("Общее количество аварий: 0");
            totalDelayTimeLabel.setText("Общее время задержек (мин): 0");
            totalBreaksLabel.setText("Количество поломок: 0");
            return;
        }

        int totalAccidents = 0;
        int totalDelayTime = 0;
        int totalBreaks = 0;

        for (Train train : trains) {
            if (train.getAccident() != null) {
                totalAccidents++;
            }

            totalDelayTime += train.getSummaryDelay();

            totalBreaks += train.sizeOfBreak();
        }

        totalEventsLabel.setText("Общее количество аварий: " + totalAccidents);
        totalDelayTimeLabel.setText("Общее время задержек (мин): " + totalDelayTime);
        totalBreaksLabel.setText("Количество поломок: " + totalBreaks);
    }

}
