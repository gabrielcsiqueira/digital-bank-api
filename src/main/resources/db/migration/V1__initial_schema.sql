CREATE TABLE t_account
(
    id         BIGSERIAL PRIMARY KEY,
    owner_name VARCHAR(255)   NOT NULL,
    balance    NUMERIC(19, 2) NOT NULL CHECK (balance >= 0),

    created_at TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE t_transfer
(
    id                     BIGSERIAL PRIMARY KEY,

    source_account_id      BIGINT         NOT NULL,
    destination_account_id BIGINT         NOT NULL,

    amount                 NUMERIC(19, 2) NOT NULL CHECK (amount > 0),
    status                 VARCHAR(30)    NOT NULL,

    created_at             TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_transfer_source
        FOREIGN KEY (source_account_id)
            REFERENCES t_account (id),

    CONSTRAINT fk_transfer_destination
        FOREIGN KEY (destination_account_id)
            REFERENCES t_account (id),

    CONSTRAINT chk_transfer_different_accounts
        CHECK (source_account_id <> destination_account_id)
);


CREATE TABLE t_notification
(
    id          BIGSERIAL PRIMARY KEY,

    account_id  BIGINT      NOT NULL,
    transfer_id BIGINT      NOT NULL,

    message     TEXT        NOT NULL,
    status      VARCHAR(30) NOT NULL,

    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_notification_account
        FOREIGN KEY (account_id)
            REFERENCES t_account (id),

    CONSTRAINT fk_notification_transfer
        FOREIGN KEY (transfer_id)
            REFERENCES t_transfer (id)
);