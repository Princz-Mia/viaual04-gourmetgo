-- Minimal sample data for GourmetGo application

-- Insert addresses
INSERT INTO address (id, unit_number, address_line, city, postal_code, region) VALUES
('550e8400-e29b-41d4-a716-446655440001', '101', '123 Main Street', 'New York', '10001', 'NY');

-- Insert restaurant categories
INSERT INTO restaurant_category (id, name) VALUES
('650e8400-e29b-41d4-a716-446655440001', 'Italian');

-- Insert admin user
INSERT INTO "user" (id, email_address, created_at, login_attempts, is_account_non_locked, is_enabled, deleted) VALUES
('750e8400-e29b-41d4-a716-446655440001', 'admin@gourmetgo.com', '2024-01-01 09:00:00', 0, true, true, false);

INSERT INTO admin (id, full_name) VALUES
('750e8400-e29b-41d4-a716-446655440001', 'Admin User');

-- Insert customer user
INSERT INTO "user" (id, email_address, created_at, login_attempts, is_account_non_locked, is_enabled, deleted) VALUES
('850e8400-e29b-41d4-a716-446655440001', 'john.doe@email.com', '2024-01-10 12:00:00', 0, true, true, false);

INSERT INTO customer (id, full_name, phone_number) VALUES
('850e8400-e29b-41d4-a716-446655440001', 'John Doe', '+1-555-0101');

-- Insert restaurant user
INSERT INTO "user" (id, email_address, created_at, login_attempts, is_account_non_locked, is_enabled, deleted) VALUES
('950e8400-e29b-41d4-a716-446655440001', 'mario@pizzapalace.com', '2024-01-02 08:00:00', 0, true, true, false);

INSERT INTO restaurant (id, name, phone_number, owner_name, delivery_fee, is_approved, address_id) VALUES
('950e8400-e29b-41d4-a716-446655440001', 'Mario''s Pizza Palace', '+1-555-1001', 'Mario Rossi', 3.99, true, '550e8400-e29b-41d4-a716-446655440001');

-- Insert product category
INSERT INTO product_category (id, name, restaurant_id) VALUES
('a50e8400-e29b-41d4-a716-446655440001', 'Pizzas', '950e8400-e29b-41d4-a716-446655440001');

-- Insert product
INSERT INTO product (id, name, description, price, inventory, product_category_id, restaurant_id, deleted) VALUES
('b50e8400-e29b-41d4-a716-446655440001', 'Margherita Pizza', 'Classic pizza', 14.99, 50, 'a50e8400-e29b-41d4-a716-446655440001', '950e8400-e29b-41d4-a716-446655440001', false);

-- Insert credentials
INSERT INTO credential (id, password, user_id) VALUES
('c50e8400-e29b-41d4-a716-446655440001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBaLzfKW4nAl4e', '750e8400-e29b-41d4-a716-446655440001'),
('c50e8400-e29b-41d4-a716-446655440002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBaLzfKW4nAl4e', '850e8400-e29b-41d4-a716-446655440001'),
('c50e8400-e29b-41d4-a716-446655440003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBaLzfKW4nAl4e', '950e8400-e29b-41d4-a716-446655440001');

-- Insert cart
INSERT INTO cart (id, total_amount, customer_id) VALUES
('d50e8400-e29b-41d4-a716-446655440001', 0.00, '850e8400-e29b-41d4-a716-446655440001');

-- Insert payment method
INSERT INTO payment_method (id, name) VALUES
('f50e8400-e29b-41d4-a716-446655440001', 'Credit Card');