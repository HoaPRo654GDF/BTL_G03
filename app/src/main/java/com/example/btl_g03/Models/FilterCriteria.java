package com.example.btl_g03.Models;

import java.util.Date;

public class FilterCriteria {
    private String category;
    private double maxDistance;
    private Date postedAfter;

    public FilterCriteria() {
    }

    public FilterCriteria(String category, double maxDistance, Date postedAfter) {
        this.category = category;
        this.maxDistance = maxDistance;
        this.postedAfter = postedAfter;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getMaxDistance() {
        return maxDistance;
    }

    public void setMaxDistance(double maxDistance) {
        this.maxDistance = maxDistance;
    }

    public Date getPostedAfter() {
        return postedAfter;
    }

    public void setPostedAfter(Date postedAfter) {
        this.postedAfter = postedAfter;
    }
}
