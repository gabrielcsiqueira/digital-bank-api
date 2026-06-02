package com.test.digitalbankapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.digitalbankapi.dto.request.TransferRequestDTO;
import com.test.digitalbankapi.dto.response.TransferResponseDTO;
import com.test.digitalbankapi.service.TransferService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransferController.class)
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TransferService transferService;

    @Test
    @DisplayName("Should return 200 OK and transfer details when request payload is valid")
    void transferSuccess() throws Exception {
        TransferRequestDTO request = new TransferRequestDTO(1L, 2L, new BigDecimal("150.00"));
        TransferResponseDTO response = new TransferResponseDTO(100L, 1L, 2L, new BigDecimal("150.00"), LocalDateTime.now());

        when(transferService.transfer(any(TransferRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transferId").value(100L))
                .andExpect(jsonPath("$.amount").value(150.00));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when validation constraints fail")
    void transferThrowsBadRequestWhenPayloadIsInvalid() throws Exception {
        TransferRequestDTO invalidRequest = new TransferRequestDTO(null, 2L, new BigDecimal("-10.00"));

        mockMvc.perform(post("/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
