SET TIME ZONE 'Asia/Kolkata';
CREATE SCHEMA IF NOT EXISTS MYBANK;

SET search_path = 'mybank', public;

CREATE TABLE users (
    user_id VARCHAR(50) PRIMARY KEY,
    password VARCHAR(100) NOT NULL
);

CREATE TABLE accounts (
    account_id SERIAL PRIMARY KEY,
    user_id VARCHAR(50) REFERENCES users(user_id) ON DELETE CASCADE,
    balance DECIMAL(15, 2) DEFAULT 0.00,
    CHECK (balance >= 0)
);
CREATE INDEX idx_accounts_user_id ON accounts(user_id);

CREATE TABLE transactions (
    transaction_id SERIAL PRIMARY KEY,
    from_account INT REFERENCES accounts(account_id) ON DELETE SET NULL,
    to_account INT REFERENCES accounts(account_id) ON DELETE SET NULL,
    amount DECIMAL(15, 2) NOT NULL,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_transactions_from_account ON transactions(from_account);
CREATE INDEX idx_transactions_to_account ON transactions(to_account);
