package com.test.digitalbankapi.mapper;

import com.test.digitalbankapi.dto.response.TransferResponseDTO;
import com.test.digitalbankapi.entity.Transfer;
import org.springframework.stereotype.Component;

@Component
public class TransferMapper {

    public TransferResponseDTO toResponseDTO(Transfer transfer) {
        return new TransferResponseDTO(
                transfer.getId(),
                transfer.getSourceAccount().getId(),
                transfer.getDestinationAccount().getId(),
                transfer.getAmount(),
                transfer.getCreatedAt()
        );
    }
}