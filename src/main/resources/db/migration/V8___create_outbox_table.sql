CREATE TABLE outbox_event_db (

    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(255),
    aggregate_id VARCHAR(255),
    event_type VARCHAR(255),
    payload JSONB,
    created_at TIMESTAMP DEFAULT now(),
    published BOOLEAN DEFAULT false,
    correlation_id UUID NOT NULL
);
