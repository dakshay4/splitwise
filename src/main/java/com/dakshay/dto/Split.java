package com.dakshay.dto;


import lombok.Data;

import java.math.BigDecimal;
import java.util.Calendar;

@Data
public abstract class Split {

    private final String userId;

    public abstract BigDecimal getAmount();
    public abstract int getPercent();
}
