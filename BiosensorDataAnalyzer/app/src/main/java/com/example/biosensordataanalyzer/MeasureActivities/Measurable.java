package com.example.biosensordataanalyzer.MeasureActivities;

public interface Measurable {
    void startMeasurement();
    void stopMeasurement();
    void showPopUp();
    String checkNorms();
}
