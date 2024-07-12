package com.dakshay.utils;


import java.math.BigDecimal;
import java.math.RoundingMode;

public class NumberUtils {

    public static BigDecimal divideByInt(BigDecimal val, int divisor) {
        if(val == null || val.compareTo(BigDecimal.ZERO)==0)  return BigDecimal.ZERO;
        return val.divide(new BigDecimal(divisor),2, RoundingMode.HALF_EVEN);
    }

    public static BigDecimal percentage(BigDecimal val, int percent) {
        if(val == null || val.compareTo(BigDecimal.ZERO)==0)  return BigDecimal.ZERO;
        return val.multiply(new BigDecimal(percent)).divide(BigDecimal.valueOf(100),2, RoundingMode.HALF_EVEN);
    }
}
