CREATE TABLE games_chat
(
    game_id UUID NOT NULL REFERENCES game_analysis_results_db (id),
    chat_id INT NOT NULL REFERENCES telegram_chat_db (id),
    PRIMARY KEY (game_id, chat_id)
);

CREATE UNIQUE INDEX ON games_chat (game_id, chat_id);
