-- 1. Create Users
-- Passwords are in plain text to match your current UserDAO login logic
INSERT INTO users (user_id, password) VALUES
('user1', 'password1'),
('user2', 'password2'),
('user3', 'password3');

-- 2. Create Accounts for these users
-- Alice has two accounts to test internal transfers
INSERT INTO accounts (user_id, balance) VALUES
('alice_wonder', 1500.00), -- Alice's main account (ID will be 1)
('alice_wonder', 250.00),  -- Alice's savings (ID will be 2)
('bob_builder', 5000.00),  -- Bob's account (ID will be 3)
('charlie_bank', 10.00);   -- Charlie's account (ID will be 4)

-- 3. Record initial transactions
-- A deposit (from_account is NULL)
INSERT INTO transactions (from_account, to_account, amount)
VALUES (NULL, 1, 1500.00);

-- A transfer from Bob to Charlie
INSERT INTO transactions (from_account, to_account, amount)
VALUES (3, 4, 100.00);