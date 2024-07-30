package com.dakshay.dto;

import com.dakshay.enums.SplitType;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

public class EqualExpense extends Expense{


    public EqualExpense(String paidByUser, BigDecimal amount, List<Split> splits) {
        super(paidByUser, amount, splits);
    }

    @Override
    public SplitType getSplitType() {
        return SplitType.EQUAL;
    }

    @Override
    public boolean validate() {
        return this.getAmount().doubleValue() == this.getSplits().stream().mapToDouble(e->e.getAmount().doubleValue()).sum();
    }
}
