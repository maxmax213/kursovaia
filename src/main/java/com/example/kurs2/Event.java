package com.example.kurs2;

import java.util.*;


public class Event {

    public enum EventType {
        DELAY,       // Задержка на станции
        ACCIDENT,// Авария
        BREAK //Поломка пути
    }

    /**
     * Добавляем маршруты и поезда для определения случайных событий для них
     */

    private final List<Route> routes;
    private final List<Train> trains;
    private final Random random;
    
    /**
     * Инцилизация парметров системы,которые будут отвечать за то сколько будет событий и как они будут распределнны между собой
     */

    private final int totalEventPercentage; // Общий процент маршрутов с событиями
    private final int delayEventCount;      // Количество событий типа "задержка"
    private final int accidentEventCount;   // Количество событий типа "авария"
    private final int breakEventCount; // Шанс возникновения события "Поломка пути"

    List<Break> breaks = new ArrayList<>();
    Set<String> uniqueBreaks = new HashSet<>();


    public Event(List<Route> routes, List<Train> trains, int totalEventPercentage,
                 int delayEventCount, int accidentEventCount, int breakEventCount) {
        this.routes = routes;
        this.trains = trains;
        this.totalEventPercentage = totalEventPercentage;
        this.delayEventCount = delayEventCount;
        this.accidentEventCount = accidentEventCount;
        this.breakEventCount = breakEventCount;
        this.random = new Random();
    }
    /**
     * Генерация событий для поездов.
     */
    public void generateEvents() {


        int totalTrainsWithEvents = (routes.size() * totalEventPercentage) / 100;

        System.out.println(totalTrainsWithEvents + " trains with " );


        List<Train> eligibleTrains = new ArrayList<>(trains);
        for (int i = 0; i < totalTrainsWithEvents && !eligibleTrains.isEmpty(); i++) {

            boolean flag = false;

            while (!flag && !eligibleTrains.isEmpty()) {
                Train selectedTrain = eligibleTrains.remove(random.nextInt(eligibleTrains.size()));

                int randomChance = random.nextInt(99);

                if (randomChance <= delayEventCount) {
                    generateDelayEvent(selectedTrain); // Событие "задержка"
                    System.out.println("Задержка");
                    flag = true;

                } else if (randomChance <= accidentEventCount) {
                    generateAccidentEvent(selectedTrain); // Событие "авария"
                    System.out.println("Авария");
                    flag = true;

                } else if (randomChance <= breakEventCount) {
                    generateBreakEvent();
                    System.out.println("Поломка пути");
                    flag = true;
                }
            }


        }
    }


    /**
     * Генерация события "задержка" для конкретного поезда.
     */
    private void generateDelayEvent(Train train) {
        Route route = train.getRoute();
        if (route == null || route.getStationList().isEmpty()) return;

        String randomStation;

        List<String> stationList = route.getStationList();
        if (stationList.size() > 2) {
            randomStation = stationList.get(random.nextInt(stationList.size() - 2) + 1);
        } else {
            randomStation = route.getStationList().get(random.nextInt(route.getStationList().size()-1));
        }



        int delayTime = 15 + random.nextInt(46); // Задержка от 15 до 60 минут

        train.addDelay(new Delay(delayTime, randomStation));
        System.out.println("Задержка сгенерирована для поезда " + train.getId() + " на станции " + randomStation + " на " + delayTime + " минут.");
    }

    /**
     * Генерация события "авария" для конкретного поезда.
     */
    private void generateAccidentEvent(Train train) {
        Route route = train.getRoute();
        if (route == null || route.getStationList().size() < 2) return;

        // Случайные станции для аварии (отправления и прибытия)
        int stationIndex = random.nextInt(route.getStationList().size() - 1);
        String departureStation = route.getStationList().get(stationIndex);
        String arrivalStation = route.getStationList().get(stationIndex + 1);
        int delayTime = 1 + random.nextInt(7)  ; // Задержка от 1 до 7 часов не забыть исправить потом на + random.nextInt(7)

        train.setAccident(new Accident(departureStation, arrivalStation, delayTime));
        System.out.println("Авария сгенерирована для поезда " + train.getId() +
                " между станциями " + departureStation + " и " + arrivalStation +
                " с задержкой " + delayTime + " часов.");
    }

    /**
     * Генерация события "Поломка пути" для случайного участка маршрута.
     */

    private void generateBreakEvent() {


        for (Route route : routes) {
            if (route.getStationList().size() < 2) continue;


            int stationIndex = random.nextInt(route.getStationList().size() - 1);
            String departureStationId = route.getStationList().get(stationIndex);
            String arrivalStationId = route.getStationList().get(stationIndex + 1);

            String path = departureStationId  + "->" +  arrivalStationId;
            String pathAround =  arrivalStationId  + "->" + departureStationId;

            if (breaks.size() < 2 && !uniqueBreaks.contains(path) && !uniqueBreaks.contains(pathAround)) {

                breaks.add(new Break(departureStationId, arrivalStationId));
                uniqueBreaks.add(path);

                breaks.add(new Break(arrivalStationId, departureStationId));
                uniqueBreaks.add(pathAround);

//                System.out.println("Поломка пути сгенерирована на участке между станциями " +
//                        departureStationId + " и " + arrivalStationId);
            }

        }



        for(Train train : trains) {
            train.setBreaks(breaks);
            System.out.println("1");
        }

    }
}
