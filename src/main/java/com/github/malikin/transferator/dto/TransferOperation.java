package com.github.malikin.transferator.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class TransferOperation {

    private Long senderId;
    private Long recipientId;
    private BigDecimal amount;
}
