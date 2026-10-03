-- Seed Products (ID, Name, Views, Created At, Available Stock, Version)
INSERT INTO products (id, name, views, created_at, available_stock, version)
VALUES (1, 'Wireless Noise-Canceling Headphones', 1500, CURRENT_TIMESTAMP(), 10, 0);

INSERT INTO products (id, name, views, created_at, available_stock, version)
VALUES (2, 'Ergonomic Mechanical Keyboard', 850, CURRENT_TIMESTAMP(), 5, 0);

INSERT INTO products (id, name, views, created_at, available_stock, version)
VALUES (3, 'Ultra-Wide Gaming Monitor', 2300, CURRENT_TIMESTAMP(), 1, 0);

-- Seed Discounts
INSERT INTO discounts (id, code, description, active)
VALUES (1, 'LONDON20', '20% off for London customers', true);

INSERT INTO discounts (id, code, description, active)
VALUES (2, 'SUMMER50', '50% off clearout sales', false);

-- Seed User Liked Items (For User ID 1)
INSERT INTO user_liked_items (id, user_id, product_id)
VALUES (1, 1, 1);