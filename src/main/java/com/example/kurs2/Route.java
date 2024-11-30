package com.example.kurs2;

import javafx.scene.shape.Circle;

import java.util.*;

public class Route {
    private List<String> stationList;
    private final Map<String, Circle> stations;


    private final Map<String, List<String>> stationMap = new HashMap<>();//Карта основных станций
    private final Map<String, Circle> intermediateStations = new HashMap<>(); //Карта промежуточных остановок

    public Route(List<String> stationList, Map<String, Circle> stations) {
        this.stationList = new ArrayList<>(stationList);
        this.stations = stations;

        initializeStationMap();
        setIntermediateStations();

    }

    public Route(Map<String, Circle> stations) {
        this.stations = stations;
        this.stationList = new ArrayList<>();
        initializeStationMap();
        setIntermediateStations();

    }

    public Map<String, Circle> getIntermediateStations(){
        return intermediateStations;
    }

    public void setLiastST(List<String> list){

        this.stationList = new ArrayList<>(list);

    }

    public List<String> getStationList() {
        return stationList;
    }

    // Построение прямого маршрута
    public List<String> buildDirectRoute(String startStationId, String endStationId) {
        if (!stationMap.containsKey(startStationId) || !stationMap.containsKey(endStationId)) {
            System.out.println("Invalid station IDs.");
            return null;
        }

        Queue<List<String>> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();

        queue.add(List.of(startStationId));
        visited.add(startStationId);

        while (!queue.isEmpty()) {
            List<String> currentPath = queue.poll();
            String lastStation = currentPath.getLast();

            if (lastStation.equals(endStationId)) {
                return currentPath;
            }

            for (String neighbor : stationMap.getOrDefault(lastStation, List.of())) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    List<String> newPath = new ArrayList<>(currentPath);
                    newPath.add(neighbor);
                    queue.add(newPath);
                }
            }
        }

        System.out.println("No path found between " + startStationId + " and " + endStationId);
        return null;
    }

    public List<String> buildRouteWithIntermediate(String startStationId, String endStationId, String intermediateStationId) {
        List<String> route = new ArrayList<>();

        // Добавляем начальную станцию
        route.add(startStationId);

        // Проверяем промежуточную станцию в обоих направлениях
        String intermediateKey = startStationId + "_" + intermediateStationId;
        if (!stations.containsKey(intermediateKey)) {
            intermediateKey = intermediateStationId + "_" + startStationId;
        }

        // Если найдена промежуточная станция, добавляем её в маршрут
        if (stations.containsKey(intermediateKey)) {
            route.add(intermediateKey);
        }

        // Добавляем промежуточную станцию как отдельную точку
        if (stationMap.containsKey(intermediateStationId)) {
            route.add(intermediateStationId);
        }

        // Продолжение маршрута от промежуточной станции до конечной
        buildRoute(intermediateStationId, endStationId, route);

        return route;
    }


    private void buildRoute(String fromStation, String toStation, List<String> route) {
        Set<String> visited = new HashSet<>();
        Deque<String> stack = new ArrayDeque<>();
        Map<String, String> parentMap = new HashMap<>();

        stack.push(fromStation);
        visited.add(fromStation);

        // Поиск пути с использованием DFS
        while (!stack.isEmpty()) {
            String current = stack.pop();
            if (current.equals(toStation)) {
                break;
            }

            for (String neighbor : stationMap.getOrDefault(current, Collections.emptyList())) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    stack.push(neighbor);
                    parentMap.put(neighbor, current);
                }
            }
        }

        // Восстановление пути из parentMap
        LinkedList<String> path = new LinkedList<>();
        String current = toStation;
        while (current != null && !current.equals(fromStation)) {
            path.addFirst(current);
            current = parentMap.get(current);
        }
        if (current != null) {
            path.addFirst(current);
        }

        // Добавление восстановленного пути к общему маршруту
        if (!route.isEmpty()) {
            path.remove(0); // Удаляем дубликат последней добавленной станции
        }
        route.addAll(path);
    }



    // Инициализация карты станций
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

    // Установка промежуточных станций
    private void setIntermediateStations() {
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
    }


}


