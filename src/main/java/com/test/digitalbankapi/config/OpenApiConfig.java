package com.test.digitalbankapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Digital Bank API")
                        .version("1.0.0")
                        .description("API RESTful para simulação de operações bancárias essenciais, " +
                                "incluindo criação de contas, listagem e transferências financeiras " +
                                "com garantia de consistência transacional e integridade dos dados.")
                        .contact(new Contact()
                                .name("Gabriel Siqueira")
                                .email("gabriel.siqueira1267@gmail.com")));
    }
}