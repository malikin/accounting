package com.github.malikin.transferator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class Transaction {

    private Long id;
    private UUID operationUuid;
    private Long senderId;
    private Long recipientId;
    private BigDecimal amount;
    private Instant timestamp;
}
