-- FinVault Database Schema
-- Version 1 - Initial Schema

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(100) NOT NULL UNIQUE,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(150),
    profile_picture VARCHAR(500),
    phone_number VARCHAR(20),
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    email_verification_token VARCHAR(255),
    password_reset_token VARCHAR(255),
    password_reset_expires TIMESTAMP,
    last_login_at TIMESTAMP,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_username ON users(username);
CREATE INDEX idx_user_email_token ON users(email_verification_token);
CREATE INDEX idx_user_reset_token ON users(password_reset_token);

-- Portfolios table
CREATE TABLE portfolios (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    initial_investment DECIMAL(18,6) NOT NULL DEFAULT 0,
    total_invested DECIMAL(18,6) NOT NULL DEFAULT 0,
    current_value DECIMAL(18,6) NOT NULL DEFAULT 0,
    type VARCHAR(20) NOT NULL DEFAULT 'MIXED',
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_portfolio_user ON portfolios(user_id);

-- Assets table
CREATE TABLE assets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    portfolio_id UUID NOT NULL REFERENCES portfolios(id) ON DELETE CASCADE,
    symbol VARCHAR(20) NOT NULL,
    name VARCHAR(150) NOT NULL,
    asset_type VARCHAR(20),
    quantity DECIMAL(18,8) NOT NULL,
    average_buy_price DECIMAL(18,6) NOT NULL,
    current_price DECIMAL(18,6),
    price_updated_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_asset_portfolio ON assets(portfolio_id);
CREATE INDEX idx_asset_symbol ON assets(symbol);

-- Transactions table
CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    asset_id UUID REFERENCES assets(id) ON DELETE SET NULL,
    type VARCHAR(20) NOT NULL,
    quantity DECIMAL(18,8) NOT NULL,
    price DECIMAL(18,6) NOT NULL,
    fee DECIMAL(18,6),
    currency VARCHAR(10) DEFAULT 'USD',
    notes VARCHAR(500),
    executed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_transaction_user ON transactions(user_id);
CREATE INDEX idx_transaction_asset ON transactions(asset_id);
CREATE INDEX idx_transaction_type ON transactions(type);

-- Notifications table
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    type VARCHAR(20) NOT NULL DEFAULT 'INFO',
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    action_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notification_user ON notifications(user_id);
CREATE INDEX idx_notification_unread ON notifications(user_id, is_read);

-- Auto-update updated_at trigger
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_portfolios_updated_at
    BEFORE UPDATE ON portfolios
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_assets_updated_at
    BEFORE UPDATE ON assets
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Default admin user (password: Admin@1234)
INSERT INTO users (id, email, username, password, full_name, email_verified, role)
VALUES (
    uuid_generate_v4(),
    'admin@finvault.io',
    'admin',
    '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', -- password
    'FinVault Admin',
    TRUE,
    'ADMIN'
);

COMMENT ON TABLE users IS 'Platform user accounts with authentication and role information';
COMMENT ON TABLE portfolios IS 'User investment portfolios - can have multiple portfolios';
COMMENT ON TABLE assets IS 'Individual asset holdings within a portfolio';
COMMENT ON TABLE transactions IS 'Financial transaction history for all asset operations';
COMMENT ON TABLE notifications IS 'User notification system for alerts and updates';
