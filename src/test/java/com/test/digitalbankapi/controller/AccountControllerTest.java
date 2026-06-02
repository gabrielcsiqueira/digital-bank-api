package com.test.digitalbankapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.digitalbankapi.dto.request.AccountRequestDTO;
import com.test.digitalbankapi.dto.response.AccountResponseDTO;
import com.test.digitalbankapi.service.AccountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AccountService accountService;

    @Test
    @DisplayName("Should create an account and return status 200 when request is valid")
    void createAccountSuccess() throws Exception {
        AccountRequestDTO request = new AccountRequestDTO("Gabriel Siqueira", new BigDecimal("1000.00"));
        AccountResponseDTO response = new AccountResponseDTO(1L, "Gabriel Siqueira", new BigDecimal("1000.00"));

        when(accountService.create(any(AccountRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.ownerName").value("Gabriel Siqueira"))
                .andExpect(jsonPath("$.balance").value(1000.00));
    }

    @Test
    @DisplayName("Should return a list of accounts and status 200")
    void findAllAccountsSuccess() throws Exception {
        AccountResponseDTO account1 = new AccountResponseDTO(1L, "Gabriel Siqueira", new BigDecimal("1000.00"));
        AccountResponseDTO account2 = new AccountResponseDTO(2L, "João Silva", new BigDecimal("500.00"));

        when(accountService.findAll()).thenReturn(List.of(account1, account2));

        mockMvc.perform(get("/accounts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    @DisplayName("Should return status 400 when account request payload is invalid")
    void createAccountThrowsBadRequest() throws Exception {
        AccountRequestDTO invalidRequest = new AccountRequestDTO("", new BigDecimal("100.00"));

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}