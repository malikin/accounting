package com.github.malikin.transferator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Transaction {

    private Long id;
    private UUID operationUuid;
    private Long senderId;
    private Long recipientId;
    private Double amount;
    private Instant timestamp;
}
