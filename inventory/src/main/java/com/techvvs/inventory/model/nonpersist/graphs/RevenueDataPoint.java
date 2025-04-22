package com.techvvs.inventory.model.nonpersist.graphs;

import java.time.LocalDate;

public class RevenueDataPoint {
    public String date;
    public Double total;

    public RevenueDataPoint(LocalDate date, Double total) {
        this.date = date.toString();
        this.total = total;
    }
}
