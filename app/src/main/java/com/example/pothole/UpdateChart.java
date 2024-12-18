package com.example.pothole;

public interface UpdateChart {
    void DataChartAfterAdd(PotholeClass potholeClass);
    void DataChartAfterUpdate(Integer old_type, Integer new_type);
    void DataChartAfterDelete(Integer type, String Date);
}
