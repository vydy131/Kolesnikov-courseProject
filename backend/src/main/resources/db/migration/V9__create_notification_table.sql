CREATE TABLE notification (
    id                UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id           UUID        NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    title             VARCHAR(255) NOT NULL,
    body              TEXT,
    is_read           BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE INDEX idx_notification_user_id ON notification(user_id);
CREATE INDEX idx_notification_unread  ON notification(user_id, is_read) WHERE is_read = FALSE;
