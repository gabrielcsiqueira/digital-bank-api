# Digital Bank API

API REST de transferências financeiras construída com Spring Boot 3, Java 17 e PostgreSQL. O projeto simula um cenário real de banco digital, com foco em consistência de dados sob alta concorrência.

---

## Exemplo rápido

Criar uma conta:

```bash
curl --location 'localhost:8080/api/accounts' \
--header 'Content-Type: application/json' \
--data '{
  "ownerName": "Maria Rodrigues",
  "initialBalance": 1000.00
}'
```

Executar uma transferência bancária:

```bash
curl --location 'http://localhost:8080/api/transfers' \
--header 'Content-Type: application/json' \
--data '{
  "sourceAccountId": 1,
  "destinationAccountId": 2,
  "amount": 250.00
}'
```

---

## Rodando o projeto

A aplicação sobe inteira via Docker — não é necessário ter Java, Maven ou PostgreSQL instalados.

```bash
docker-compose up --build
```

Isso vai buildar a imagem, subir o PostgreSQL, aguardar o banco ficar saudável e iniciar a API em `http://localhost:8080/api`.

Para encerrar e limpar os volumes:

```bash
docker-compose down -v
```

### Testes

Com o container do banco ativo, rode:

```bash
mvn clean test
```

> O build do Docker usa `-DskipTests` propositalmente — a ideia é não deixar variações de ambiente (porta ocupada, memória apertada no host) atrapalharem o setup inicial. Os testes rodam separadamente.

A suite foi organizada seguindo a pirâmide de testes, do mais rápido e isolado ao mais completo:

**Unitários** — JUnit 5 + Mockito cobrindo as regras de negócio dos services em isolamento: validação de saldo, fluxos de transferência, cálculo de taxas. Rodam sem banco, sem contexto Spring, feedback imediato.

**Integração** — Testcontainers sobe um PostgreSQL real e temporário para cada execução. Isso valida as migrações do Flyway e as queries do Spring Data JPA no mesmo ambiente de produção, sem os atalhos que um H2 em memória permitiria passar despercebidos.

**API** — `@SpringBootTest` + `MockMvc` cobrindo os controllers: status HTTP corretos, tratamento de exceções pelo `@RestControllerAdvice` e respeito ao context path configurado.

---

## Documentação da API

Com a aplicação no ar, o Swagger fica disponível em:

**http://localhost:8080/api/swagger-ui/index.html**

Todos os endpoints estão documentados em português, com exemplos de payload e os principais cenários de erro.

---

## Arquitetura e decisões de design

### Estrutura em camadas

Separação clássica entre Controller, Service e Repository. Os controllers só lidam com HTTP — toda a lógica de negócio vive nos services, e o acesso ao banco é responsabilidade exclusiva dos repositories. Isso torna cada camada testável de forma isolada.

### DTOs com Java Records

As entidades JPA nunca saem direto pela API. Todos os dados expostos usam Java Records como DTOs, que são imutáveis por padrão e não precisam de getters, setters ou construtores escritos à mão. Mantém o modelo interno protegido de vazamentos acidentais.

### Tratamento de erros centralizado

Um `@RestControllerAdvice` captura qualquer exceção lançada na aplicação e retorna sempre o mesmo formato de resposta de erro — timestamp, status HTTP, tipo da exceção e detalhes de validação campo a campo. Nenhum stacktrace chega ao cliente.

### O problema central: concorrência nas transferências

Esse foi o ponto mais crítico do projeto. Numa API de transferências sob carga, duas requisições podem tentar debitar a mesma conta ao mesmo tempo — se não houver controle, os saldos ficam corrompidos.

A solução foi usar **lock pessimista** (`SELECT ... FOR UPDATE`) no banco. Antes de qualquer movimentação, a linha da conta é travada. Nenhuma outra transação toca naquele registro até o commit.

Mas isso cria um segundo problema: se a Conta A está transferindo para B enquanto B transfere para A, cada transação trava uma conta esperando a outra liberar — deadlock clássico. A saída foi simples: **os locks são sempre adquiridos em ordem crescente de ID**, independente de quem é origem e quem é destino. Com isso, as duas transações concorrem pela mesma conta primeiro, e o deadlock nunca se forma.

### Notificações assíncronas

Após a transferência ser persistida, um `TransferCreatedEvent` é publicado via `ApplicationEventPublisher` do Spring. O listener que processa a notificação roda em thread separada, então a resposta ao cliente não fica esperando o envio do e-mail ou qualquer outro efeito colateral.

### Logs

SLF4J em três níveis: `INFO` para o fluxo normal de negócio, `WARN` para violações de regra (saldo insuficiente, conta não encontrada) e `DEBUG` para os detalhes de aquisição e liberação de locks.

---

## Variáveis de ambiente

Todas já vêm configuradas no `docker-compose.yml` para rodar localmente sem nenhum ajuste. Para customizar:

| Variável | Padrão | Descrição |
|---|---|---|
| `POSTGRES_DB` | `digitalbank` | Nome do banco de dados |
| `POSTGRES_USER` | `postgres` | Usuário do banco |
| `POSTGRES_PASSWORD` | `postgres` | Senha do banco |
| `SERVER_PORT` | `8080` | Porta da API |

---

## Stack

| | |
|---|---|
| Java 17 | LTS com suporte a Records, base do Spring Boot 3 |
| Spring Boot 3 | Injeção de dependência, transações declarativas, ecossistema consolidado |
| PostgreSQL 17 | ACID + `SELECT FOR UPDATE` para controle de concorrência a nível de linha |
| Springdoc OpenAPI | Swagger UI gerado a partir das anotações, sem duplicar contratos |
| Docker + Compose | Multi-stage build, healthcheck no banco, zero dependências locais |
