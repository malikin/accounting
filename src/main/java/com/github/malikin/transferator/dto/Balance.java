package com.github.malikin.transferator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Balance {

    private Long accountId;
    private BigDecimal amount;
}
