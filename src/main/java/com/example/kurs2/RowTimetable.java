package com.example.kurs2;

public class RowTimetable
{

    private String first;
    private String second;
    private int third;

    public RowTimetable(String first, String second, int third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public String getFirst() {
        return first;
    }

    public void setFirst(String first) {
        this.first = first;
    }

    public String getSecond() {
        return second;
    }

    public void setSecond(String second) {
        this.second = second;
    }

    public int getThird() {
        return third;
    }

    public void setThird(int third) {
        this.third = third;
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ", " + third + ")";
    }

    public String getStartStation() {
        return first;
    }

    public String getEndStation() {
        return second;
    }


    public int getDepartureTime() {
        return third;
    }
}
