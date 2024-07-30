package com.dakshay.dto;

import java.math.BigDecimal;

public class ExactSplit extends Split{

    private final BigDecimal amount;

    public ExactSplit(String userId, BigDecimal amount) {
        super(userId);
        this.amount = amount;
    }

}
