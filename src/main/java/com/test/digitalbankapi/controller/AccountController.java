package com.test.digitalbankapi.controller;

import com.test.digitalbankapi.dto.request.AccountRequestDTO;
import com.test.digitalbankapi.dto.response.AccountResponseDTO;
import com.test.digitalbankapi.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<AccountResponseDTO> create(@Valid @RequestBody AccountRequestDTO request) {
        return ResponseEntity.ok(accountService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<AccountResponseDTO>> findAll() {
        return ResponseEntity.ok(accountService.findAll());
    }
}
