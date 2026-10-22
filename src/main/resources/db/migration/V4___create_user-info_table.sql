ALTER TABLE telegram_chat_db
DROP COLUMN ogs_account_name;

ALTER TABLE telegram_chat_db
DROP COLUMN ogs_account_id;

ALTER TABLE telegram_chat_db
DROP COLUMN email;


CREATE TABLE user_info_db (
    id                  UUID PRIMARY KEY,
    ogs_account_name    VARCHAR(255),
    ogs_account_id      BIGINT,
    email_address               VARCHAR(255),
);
