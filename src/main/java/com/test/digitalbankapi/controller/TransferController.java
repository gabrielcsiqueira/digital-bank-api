package com.test.digitalbankapi.controller;

import com.test.digitalbankapi.controller.openapi.TransferApi;
import com.test.digitalbankapi.dto.request.TransferRequestDTO;
import com.test.digitalbankapi.dto.response.TransferResponseDTO;
import com.test.digitalbankapi.service.TransferService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transfers")
public class TransferController implements TransferApi {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    @Override
    public ResponseEntity<TransferResponseDTO> transfer(@Valid @RequestBody TransferRequestDTO request) {
        return ResponseEntity.ok(transferService.transfer(request));
    }
}