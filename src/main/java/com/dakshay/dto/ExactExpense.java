package com.dakshay.dto;

import com.dakshay.enums.SplitType;

import java.math.BigDecimal;
import java.util.List;

public class ExactExpense extends Expense{

    public ExactExpense(String paidByUser, BigDecimal amount, List<Split> splits) {
        super(paidByUser, amount, splits);
    }

    @Override
    public SplitType getSplitType() {
        return SplitType.EXACT;
    }

    @Override
    public boolean validate() {
        return this.getAmount().doubleValue() == this.getSplits().stream().mapToDouble(e->e.getAmount().doubleValue()).sum();
    }
}
