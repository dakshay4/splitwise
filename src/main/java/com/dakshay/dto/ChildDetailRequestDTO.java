package com.dakshay.dto;


import com.dakshay.enums.SplitType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ChildDetailRequestDTO {

    private String userId;
    private BigDecimal amount;
    private int percentage;
}
