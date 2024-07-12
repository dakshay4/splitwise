package com.dakshay.dto;


import com.dakshay.enums.SplitType;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class RootDetailsRequestDTO {

    private String userId;
    private BigDecimal amount;
    private SplitType splitType;
    private List<ChildDetailRequestDTO> childDetailRequests;
}
