CREATE TABLE oauth_connections_db (

    id                  UUID PRIMARY KEY,
    provider            VARCHAR(255) NOT NULL,
    provider_id         VARCHAR(255) NOT NULL,
    access_token        TEXT,
    refresh_token       TEXT,
    expires_at_token    TIMESTAMP WITH TIME ZONE,
    chat_id             BIGINT NOT NULL

    CONSTRAINT fk_oauth_chat
        FOREIGN KEY (chat_id)
            REFERENCES telegram_chat_db(id)
);

