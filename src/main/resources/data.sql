-- Sample Product Inventory Data
-- Using IGNORE to avoid duplicate inserts on restart

INSERT IGNORE INTO products (name, description, price, quantity, sku, category) VALUES
('MacBook Pro 16"', 'Apple MacBook Pro 16-inch M3 Pro', 2499.99, 25, 'APPLE-MBP16-001', 'Electronics'),
('Dell Monitor 27"', 'Dell UltraSharp 27 4K USB-C Hub Monitor', 549.99, 40, 'DELL-MON27-001', 'Electronics'),
('Logitech MX Master 3S', 'Wireless Performance Mouse', 99.99, 150, 'LOGI-MXM3S-001', 'Accessories'),
('Samsung SSD 1TB', 'Samsung 990 PRO 1TB NVMe SSD', 129.99, 80, 'SAM-SSD1T-001', 'Storage'),
('Corsair RAM 32GB', 'Corsair Vengeance DDR5 32GB Kit', 89.99, 60, 'COR-RAM32-001', 'Components'),
('Sony WH-1000XM5', 'Wireless Noise Cancelling Headphones', 349.99, 35, 'SONY-WH5-001', 'Audio'),
('Keychron K2 Pro', 'Wireless Mechanical Keyboard', 89.99, 5, 'KEY-K2P-001', 'Accessories'),
('Anker USB-C Hub', '7-in-1 USB-C Hub Adapter', 35.99, 200, 'ANK-HUB7-001', 'Accessories'),
('WD External HDD 4TB', 'Western Digital My Passport 4TB', 109.99, 3, 'WD-EXT4T-001', 'Storage'),
('LG Monitor 34"', 'LG 34" UltraWide QHD Curved Monitor', 449.99, 15, 'LG-MON34-001', 'Electronics');
