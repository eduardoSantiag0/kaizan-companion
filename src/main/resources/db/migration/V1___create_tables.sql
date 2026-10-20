CREATE TABLE telegram_chat_db (

    id                  BIGINT PRIMARY KEY,
    first_name          VARCHAR(255) NOT NULL,
    username            VARCHAR(255) NOT NULL,
    type                VARCHAR(255),
    ogs_account_name    VARCHAR(255),
    ogs_account_id      BIGINT,
    email               VARCHAR(255),
    waiting_for_analysis BOOLEAN NOT NULL DEFAULT FALSE
);


CREATE TABLE analysis_job_db (

    id                  UUID PRIMARY KEY,
    correlation_id      UUID,
    chat_id             BIGINT NOT NULL,
    original_game       TEXT,
    link_source         VARCHAR(255),
    created_at          TIMESTAMP WITH TIME ZONE,
    started_at          TIMESTAMP WITH TIME ZONE,
    last_modified_at    TIMESTAMP WITH TIME ZONE,
    finished_at         TIMESTAMP WITH TIME ZONE,
    job_status          VARCHAR(255),

    CONSTRAINT fk_analysis_job_chat
        FOREIGN KEY (chat_id)
            REFERENCES telegram_chat_db(id)
);


CREATE TABLE game_analysis_results_db (

   id                  UUID PRIMARY KEY,
   job_id              UUID NOT NULL UNIQUE,
   chat_id             BIGINT NOT NULL,
   opening_analysed    TEXT,
   midgame_analysed    TEXT,
   endgame_analysed    TEXT,
   full_analysed       TEXT,
     CONSTRAINT fk_game_analysis_job
       FOREIGN KEY (job_id)
           REFERENCES analysis_job_db(id),
     CONSTRAINT fk_game_analysis_chat
       FOREIGN KEY (chat_id)
           REFERENCES telegram_chat_db(id)
);
