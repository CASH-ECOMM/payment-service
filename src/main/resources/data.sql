-- Test Data for Payment Service Database
-- Run this script after the database schema is created

-- Note: The tables will be auto-created by Hibernate based on the entity annotations
-- This script provides sample data for testing

-- Sample Payment Records (These would be inserted through the application)
-- The following are examples of what the data would look like

-- Example 1: Completed Regular Shipping Payment
INSERT INTO payments (payment_id, user_id, item_id, item_cost, shipping_cost, shipping_type,
                      estimated_shipping_days, hst_amount, total_amount, payment_status,
                      transaction_reference, first_name, last_name, street, street_number,
                      province, country, postal_code, card_number_last_four, name_on_card,
                      card_type, expiry_date, created_at, updated_at)
VALUES ('pay-001', 'user-123', 'item-456', 99.99, 15.00, 'REGULAR', 5,
        14.95, 129.94, 'COMPLETED', 'TXN-1698765432100',
        'John', 'Doe', 'Main Street', '123', 'Ontario', 'Canada', 'M5H 2N2',
        '1234', 'John Doe', 'VISA', '12/25', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Example 2: Completed Expedited Shipping Payment
INSERT INTO payments (payment_id, user_id, item_id, item_cost, shipping_cost, shipping_type,
                      estimated_shipping_days, hst_amount, total_amount, payment_status,
                      transaction_reference, first_name, last_name, street, street_number,
                      province, country, postal_code, card_number_last_four, name_on_card,
                      card_type, expiry_date, created_at, updated_at)
VALUES ('pay-002', 'user-456', 'item-789', 149.99, 25.00, 'EXPEDITED', 2,
        22.75, 197.74, 'COMPLETED', 'TXN-1698765432200',
        'Jane', 'Smith', 'Oak Avenue', '456', 'British Columbia', 'Canada', 'V6B 1A1',
        '5678', 'Jane Smith', 'MASTERCARD', '06/26', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Sample Receipt Records
INSERT INTO receipts (receipt_id, payment_id, receipt_number, customer_name, customer_address,
                      item_id, item_cost, shipping_cost, hst_amount, total_paid,
                      payment_method, shipping_estimate_days, receipt_date)
VALUES ('rcp-001', 'pay-001', 'RCP-1698765432100', 'John Doe',
        'John Doe\n123 Main Street\nOntario, Canada M5H 2N2',
        'item-456', 99.99, 15.00, 14.95, 129.94, 'VISA', 5, CURRENT_TIMESTAMP);

INSERT INTO receipts (receipt_id, payment_id, receipt_number, customer_name, customer_address,
                      item_id, item_cost, shipping_cost, hst_amount, total_paid,
                      payment_method, shipping_estimate_days, receipt_date)
VALUES ('rcp-002', 'pay-002', 'RCP-1698765432200', 'Jane Smith',
        'Jane Smith\n456 Oak Avenue\nBritish Columbia, Canada V6B 1A1',
        'item-789', 149.99, 25.00, 22.75, 197.74, 'MASTERCARD', 2, CURRENT_TIMESTAMP);

-- Query examples for testing:

-- Get all completed payments
-- SELECT * FROM payments WHERE payment_status = 'COMPLETED';

-- Get payment history for a user
-- SELECT * FROM payments WHERE user_id = 'user-123' ORDER BY created_at DESC;

-- Get receipt for a payment
-- SELECT r.* FROM receipts r
-- JOIN payments p ON r.payment_id = p.payment_id
-- WHERE p.payment_id = 'pay-001';

-- Calculate total revenue
-- SELECT SUM(total_amount) as total_revenue
-- FROM payments
-- WHERE payment_status = 'COMPLETED';
