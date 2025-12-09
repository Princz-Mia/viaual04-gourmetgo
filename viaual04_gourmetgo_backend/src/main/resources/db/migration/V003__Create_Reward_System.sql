-- Create reward_points table
CREATE TABLE reward_points (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL UNIQUE,
    balance DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE CASCADE
);

-- Create reward_transactions table
CREATE TABLE reward_transactions (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('EARNED_ORDER', 'REDEEMED_ORDER', 'ADMIN_CREDIT', 'COMPENSATION')),
    amount DECIMAL(10,2) NOT NULL,
    description VARCHAR(255),
    order_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE CASCADE,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE SET NULL
);

-- Create indexes for better performance
CREATE INDEX idx_reward_points_customer_id ON reward_points(customer_id);
CREATE INDEX idx_reward_transactions_customer_id ON reward_transactions(customer_id);
CREATE INDEX idx_reward_transactions_created_at ON reward_transactions(created_at);
CREATE INDEX idx_reward_transactions_type ON reward_transactions(type);