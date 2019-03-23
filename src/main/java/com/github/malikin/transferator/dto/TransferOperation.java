package com.github.malikin.transferator.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TransferOperation {

    private Long senderId;
    private Long recipientId;
    private Double amount;
}
