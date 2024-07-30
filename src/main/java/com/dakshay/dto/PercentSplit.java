package com.dakshay.dto;

import lombok.Data;

import java.math.BigDecimal;

public class PercentSplit extends Split{

    private int percent;

    public PercentSplit(String userId, int percent) {
        super(userId);
        this.percent = percent;
    }

    @Override
    public BigDecimal getAmount() {
        return null;
    }

    public int getPercent() {
        return percent;
    }
}
