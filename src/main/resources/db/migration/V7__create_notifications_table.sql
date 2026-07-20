CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    notification_type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    title VARCHAR(120) NOT NULL,
    message VARCHAR(500) NOT NULL,
    reference_number VARCHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    read_at TIMESTAMPTZ,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT ck_notifications_type CHECK (
        notification_type IN ('REGISTRATION', 'DEPOSIT', 'WITHDRAW', 'PAYMENT_SENT', 'PAYMENT_RECEIVED')
    ),
    CONSTRAINT ck_notifications_status CHECK (status IN ('UNREAD', 'READ')),
    CONSTRAINT ck_notifications_read_state CHECK (
        (status = 'UNREAD' AND read_at IS NULL)
        OR (status = 'READ' AND read_at IS NOT NULL)
    ),
    CONSTRAINT ck_notifications_title_not_blank CHECK (length(trim(title)) > 0),
    CONSTRAINT ck_notifications_message_not_blank CHECK (length(trim(message)) > 0),
    CONSTRAINT ck_notifications_reference_number_not_blank CHECK (length(trim(reference_number)) > 0)
);

CREATE INDEX idx_notifications_user_created_at ON notifications (user_id, created_at DESC);
