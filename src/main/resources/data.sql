-- Seed Products (added 'active' column)
INSERT INTO products (id, sku, name, price, popularity_score, active, created_at)
VALUES (1, 'SKU-001', 'Wireless Noise-Canceling Headphones', 299.99, 1500, true, CURRENT_TIMESTAMP());

INSERT INTO products (id, sku, name, price, popularity_score, active, created_at)
VALUES (2, 'SKU-002', 'Ergonomic Mechanical Keyboard', 149.50, 850, true, CURRENT_TIMESTAMP());

INSERT INTO products (id, sku, name, price, popularity_score, active, created_at)
VALUES (3, 'SKU-003', 'Gaming Mouse', 89.99, 1200, true, CURRENT_TIMESTAMP());

-- Seed Inventory (ID, Product ID, Available Quantity, Reserved Quantity, Location, Version)
INSERT INTO inventory (id, product_id, available_quantity, reserved_quantity, location, version)
VALUES (1, 1, 10, 0, 'LONDON', 0);

INSERT INTO inventory (id, product_id, available_quantity, reserved_quantity, location, version)
VALUES (2, 2, 5, 0, 'LONDON', 0);

INSERT INTO inventory (id, product_id, available_quantity, reserved_quantity, location, version)
VALUES (3, 3, 1, 0, 'LONDON', 0);

-- Seed Discounts (added 'starts_at' column)
INSERT INTO discounts (id, code, title, active, starts_at)
VALUES (1, 'LONDON20', '20% off for London customers', true, CURRENT_TIMESTAMP());

INSERT INTO discounts (id, code, title, active, starts_at)
VALUES (2, 'SUMMER50', '50% off clearout sales', false, CURRENT_TIMESTAMP());

-- Seed User Liked Items (ID, User ID, Product ID, Liked At)
INSERT INTO user_liked_items (id, user_id, product_id, liked_at)
VALUES (1, 1, 1, CURRENT_TIMESTAMP());