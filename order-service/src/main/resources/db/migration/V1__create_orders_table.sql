CREATE TABLE orders
(
    id               BIGSERIAL PRIMARY KEY,
    user_id          VARCHAR(255)   NOT NULL,
    status           VARCHAR(50)    NOT NULL DEFAULT 'PENDING',
    total_amount     DECIMAL(19, 2) NOT NULL DEFAULT 0,
    shipping_address VARCHAR(500)   NOT NULL,
    created_at       TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_orders_user_id ON orders (user_id);
CREATE INDEX idx_orders_status ON orders (status);