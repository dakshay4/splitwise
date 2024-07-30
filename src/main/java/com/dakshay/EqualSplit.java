package com.dakshay;

import com.dakshay.dto.Split;

import java.math.BigDecimal;

public class EqualSplit extends Split {

    private final BigDecimal amount;


    public EqualSplit(String userId ,BigDecimal amount) {
        super(userId);
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
