package com.test.digitalbankapi.controller.openapi;

import com.test.digitalbankapi.dto.request.TransferRequestDTO;
import com.test.digitalbankapi.dto.response.TransferResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Transferências", description = "Operações de movimentação financeira entre contas")
public interface TransferApi {

    @Operation(
            summary = "Executar transferência entre contas",
            description = "Realiza uma transferência financeira com controle estrito de concorrência, " +
                    "travas de segurança contra deadlocks e disparos de notificações assíncronas."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Transferência concluída e registrada com sucesso."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados malformados, falha de validação ou violação de regras de negócio.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "1. Validation Error",
                                            summary = "Erro de validação de campos (MethodArgumentNotValidException)",
                                            value = """
                                                    {
                                                      "timestamp": "2026-06-02T17:21:17",
                                                      "status": 400,
                                                      "error": "Bad Request",
                                                      "message": "One or more fields in the request are invalid.",
                                                      "type": "MethodArgumentNotValidException",
                                                      "details": ["Field 'amount': must not be null"]
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "2. Malformed JSON",
                                            summary = "JSON malformado ou tipos inválidos (HttpMessageNotReadableException)",
                                            value = """
                                                    {
                                                      "timestamp": "2026-06-02T17:21:17",
                                                      "status": 400,
                                                      "error": "Bad Request",
                                                      "message": "The request body is malformed or contains invalid data types.",
                                                      "type": "HttpMessageNotReadableException"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "3. Same Account",
                                            summary = "Transferência para a mesma conta (SameAccountTransferException)",
                                            value = """
                                                    {
                                                      "timestamp": "2026-06-02T17:21:17",
                                                      "status": 400,
                                                      "error": "Bad Request",
                                                      "message": "Source and destination accounts must be different.",
                                                      "type": "SameAccountTransferException"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "4. Invalid Amount",
                                            summary = "Valor menor ou igual a zero (InvalidTransferAmountException)",
                                            value = """
                                                    {
                                                      "timestamp": "2026-06-02T17:21:17",
                                                      "status": 400,
                                                      "error": "Bad Request",
                                                      "message": "Transfer amount must be greater than zero.",
                                                      "type": "InvalidTransferAmountException"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "5. Insufficient Balance",
                                            summary = "Saldo insuficiente na conta de origem (InsufficientBalanceException)",
                                            value = """
                                                    {
                                                      "timestamp": "2026-06-02T17:21:17",
                                                      "status": 400,
                                                      "error": "Bad Request",
                                                      "message": "Insufficient balance. Available: 500.00, required: 1000.00",
                                                      "type": "InsufficientBalanceException"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Conta de origem ou conta de destino não foi encontrada no sistema.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Account Not Found",
                                            summary = "Conta inexistente (AccountNotFoundException)",
                                            value = """
                                                    {
                                                      "timestamp": "2026-06-02T17:21:17",
                                                      "status": 404,
                                                      "error": "Not Found",
                                                      "message": "Account not found: 10",
                                                      "type": "AccountNotFoundException"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<TransferResponseDTO> transfer(@Valid @RequestBody TransferRequestDTO request);
}