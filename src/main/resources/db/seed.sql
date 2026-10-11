-- ==============================================================================
-- OmegaMart Demonstration Seed Data
-- ==============================================================================

-- 1. SEED CATEGORIES
MERGE INTO categories (id, name, description) KEY(id) VALUES 
(1, 'Electronics', 'Smartphones, laptops, audio accessories, and smart devices'),
(2, 'Fashion & Apparel', 'Modern clothing, footwear, and designer accessories'),
(3, 'Home & Living', 'Furniture, kitchen appliances, and home decor'),
(4, 'Books & Stationery', 'Academic books, fiction, notebooks, and writing instruments'),
(5, 'Sports & Fitness', 'Fitness gear, gym equipment, and outdoor sportswear');

-- 2. SEED USERS (Password hashes for demonstration accounts: 'Admin@12345', 'Seller@12345', 'Buyer@12345')
-- Note: DatabaseInitializer also verifies and synchronizes these accounts on startup.
MERGE INTO users (id, email, password_hash, full_name, role) KEY(id) VALUES
(1, 'admin@omegamart.com', '$2a$10$yPsqd2cR/xS9aKxI/H8.AOkB2XgZ1bC9mN0uK1i2e3r4t5y6u7v8w', 'System Administrator', 'ADMIN'),
(2, 'seller1@omegamart.com', '$2a$10$yPsqd2cR/xS9aKxI/H8.AOkB2XgZ1bC9mN0uK1i2e3r4t5y6u7v8w', 'Apex Electronics Store', 'SELLER'),
(3, 'seller2@omegamart.com', '$2a$10$yPsqd2cR/xS9aKxI/H8.AOkB2XgZ1bC9mN0uK1i2e3r4t5y6u7v8w', 'Urban Threads Boutique', 'SELLER'),
(4, 'buyer1@omegamart.com', '$2a$10$yPsqd2cR/xS9aKxI/H8.AOkB2XgZ1bC9mN0uK1i2e3r4t5y6u7v8w', 'Alice Johnson', 'BUYER'),
(5, 'buyer2@omegamart.com', '$2a$10$yPsqd2cR/xS9aKxI/H8.AOkB2XgZ1bC9mN0uK1i2e3r4t5y6u7v8w', 'Bob Williams', 'BUYER');

-- 3. SEED PRODUCTS
MERGE INTO products (id, seller_id, category_id, name, description, price, stock_quantity, image_url, is_active) KEY(id) VALUES
(1, 2, 1, 'ProBook Ultra 15 Laptop', '15.6-inch FHD, Intel Core i7, 16GB RAM, 512GB NVMe SSD, Backlit Keyboard', 849.99, 15, 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=600', TRUE),
(2, 2, 1, 'AeroSound Noise-Cancelling Earbuds', 'Active Noise Cancelling, Bluetooth 5.3, 30-hour battery life with charging case', 79.50, 40, 'https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=600', TRUE),
(3, 2, 1, 'RGB Mechanical Gaming Keyboard', 'Custom tactile switches, per-key RGB backlighting, detachable braided cable', 59.99, 25, 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=600', TRUE),
(4, 3, 2, 'Classic Cotton Oxford Shirt', '100% breathable organic cotton, slim fit, button-down collar', 34.00, 50, 'https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=600', TRUE),
(5, 3, 2, 'Urban Velocity Running Shoes', 'Lightweight mesh construction, cushioned shock-absorbing sole', 64.95, 30, 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600', TRUE),
(6, 2, 3, 'Ergonomic Mesh Office Chair', 'Adjustable lumbar support, 3D armrests, high-density foam cushion', 189.00, 12, 'https://images.unsplash.com/photo-1580481077195-c3f87b8d4239?w=600', TRUE),
(7, 2, 4, 'Clean Code Architecture Handbook', 'Essential engineering patterns and best practices for modern developers', 39.99, 35, 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=600', TRUE),
(8, 3, 5, 'Adjustable Dumbbell Set (20kg)', 'Quick-adjust dial system, non-slip textured grip, compact storage tray', 119.50, 18, 'https://images.unsplash.com/photo-1584735935682-2f2b69dff9d2?w=600', TRUE);

-- 4. SEED DEMO ORDERS (Completed purchase for buyer1 to enable review eligibility)
MERGE INTO orders (id, order_number, buyer_id, total_amount, status, shipping_address, payment_method) KEY(id) VALUES
(1, 'ORD-2026-1001', 4, 849.99, 'DELIVERED', '123 College Road, Guindy, Chennai, Tamil Nadu 600025', 'SIMULATED_MOCK_PAYMENT');

-- 5. SEED ORDER ITEMS
MERGE INTO order_items (id, order_id, product_id, seller_id, product_name, unit_price, quantity, subtotal) KEY(id) VALUES
(1, 1, 1, 2, 'ProBook Ultra 15 Laptop', 849.99, 1, 849.99);

-- 6. SEED REVIEWS (Qualifying purchase review)
MERGE INTO reviews (id, product_id, user_id, order_id, rating, comment) KEY(id) VALUES
(1, 1, 4, 1, 5, 'Superb build quality and blazingly fast performance for programming and college lab tasks!');
