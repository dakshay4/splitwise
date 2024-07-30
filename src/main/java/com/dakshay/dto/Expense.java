package com.dakshay.dto;

import com.dakshay.enums.SplitType;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@RequiredArgsConstructor
public abstract class Expense {

    private final String paidByUser;
    private final BigDecimal amount;
    private final List<Split> splits;


    public abstract SplitType getSplitType();
    public abstract boolean validate();
}
