package com.dakshay.dto;

import com.dakshay.enums.SplitType;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class PercentageExpense extends Expense{


    public PercentageExpense(String userId, BigDecimal amount, List<Split> splits) {
        super(userId, amount, splits);
    }


    @Override
    public SplitType getSplitType() {
        return SplitType.PERCENT;
    }

    @Override
    public boolean validate() {
        return this.getSplits().stream().mapToInt(Split::getPercent).sum() == 100;
    }
}
