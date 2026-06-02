package com.test.digitalbankapi.controller.openapi;

import com.test.digitalbankapi.dto.request.AccountRequestDTO;
import com.test.digitalbankapi.dto.response.AccountResponseDTO;
import com.test.digitalbankapi.exception.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Contas", description = "Gerenciamento de contas bancárias dos clientes")
public interface AccountApi {

    @Operation(
            summary = "Criar uma nova conta bancária",
            description = "Cadastra uma nova conta no sistema vinculada ao nome de um titular e com um saldo inicial configurado."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Conta criada e armazenada com sucesso."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados malformados ou falha nas validações dos campos obrigatórios.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "1. Validation Error",
                                            summary = "Campos inválidos ou ausentes (MethodArgumentNotValidException)",
                                            value = """
                                                    {
                                                      "timestamp": "2026-06-02T17:50:00",
                                                      "status": 400,
                                                      "error": "Bad Request",
                                                      "message": "One or more fields in the request are invalid.",
                                                      "type": "MethodArgumentNotValidException",
                                                      "details": [
                                                        "Field 'ownerName': must not be blank",
                                                        "Field 'initialBalance': must be greater than or equal to 0"
                                                      ]
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "2. Malformed JSON",
                                            summary = "JSON inválido ou erro de tipo (HttpMessageNotReadableException)",
                                            value = """
                                                    {
                                                      "timestamp": "2026-06-02T17:50:00",
                                                      "status": 400,
                                                      "error": "Bad Request",
                                                      "message": "The request body is malformed or contains invalid data types.",
                                                      "type": "HttpMessageNotReadableException"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<AccountResponseDTO> create(@Valid @RequestBody AccountRequestDTO request);

    @Operation(
            summary = "Listar todas as contas",
            description = "Retorna uma listagem completa de todas as contas bancárias existentes na base de dados, ordenadas crescentemente por ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de contas retornada com sucesso."
            )
    })
    ResponseEntity<List<AccountResponseDTO>> findAll();
}