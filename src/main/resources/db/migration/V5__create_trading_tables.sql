-- Trading System Tables
-- V5__create_trading_tables.sql

-- 거래 쌍 정보
CREATE TABLE IF NOT EXISTS trading_pairs (
    id BIGSERIAL PRIMARY KEY,
    symbol VARCHAR(20) NOT NULL,
    base_asset VARCHAR(10) NOT NULL,
    quote_asset VARCHAR(10) NOT NULL,
    exchange VARCHAR(50) NOT NULL,
    min_order_size DECIMAL(20, 8) NOT NULL,
    max_order_size DECIMAL(20, 8),
    tick_size DECIMAL(20, 8) NOT NULL,
    maker_fee DECIMAL(10, 6) DEFAULT 0,
    taker_fee DECIMAL(10, 6) DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (symbol, exchange)
);

CREATE INDEX idx_trading_pairs_symbol ON trading_pairs(symbol);
CREATE INDEX idx_trading_pairs_exchange ON trading_pairs(exchange);
CREATE INDEX idx_trading_pairs_active ON trading_pairs(is_active);

-- 주문 내역
CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    order_id VARCHAR(100) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    symbol VARCHAR(20) NOT NULL,
    exchange VARCHAR(50) NOT NULL,
    order_type VARCHAR(20) NOT NULL,
    order_side VARCHAR(10) NOT NULL,
    price DECIMAL(20, 8),
    stop_price DECIMAL(20, 8),
    quantity DECIMAL(20, 8) NOT NULL,
    executed_qty DECIMAL(20, 8) DEFAULT 0,
    cummulative_quote_qty DECIMAL(20, 8) DEFAULT 0,
    status VARCHAR(20) NOT NULL,
    strategy_id BIGINT,
    time_in_force VARCHAR(10) DEFAULT 'GTC',
    client_order_id VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    filled_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_symbol ON orders(symbol);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_strategy_id ON orders(strategy_id);
CREATE INDEX idx_orders_created_at ON orders(created_at DESC);

-- 포트폴리오
CREATE TABLE IF NOT EXISTS portfolios (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    symbol VARCHAR(20) NOT NULL,
    quantity DECIMAL(20, 8) NOT NULL,
    avg_buy_price DECIMAL(20, 8),
    current_price DECIMAL(20, 8),
    unrealized_pnl DECIMAL(20, 8),
    realized_pnl DECIMAL(20, 8) DEFAULT 0,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, symbol),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_portfolios_user_id ON portfolios(user_id);

-- 트레이딩 전략
CREATE TABLE IF NOT EXISTS trading_strategies (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    strategy_type VARCHAR(50) NOT NULL,
    config JSONB NOT NULL,
    is_active BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_strategies_user_id ON trading_strategies(user_id);
CREATE INDEX idx_strategies_active ON trading_strategies(is_active);

-- 백테스팅 결과
CREATE TABLE IF NOT EXISTS backtest_results (
    id BIGSERIAL PRIMARY KEY,
    strategy_id BIGINT NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    initial_capital DECIMAL(20, 8) NOT NULL,
    final_capital DECIMAL(20, 8) NOT NULL,
    total_return DECIMAL(10, 4),
    sharpe_ratio DECIMAL(10, 4),
    max_drawdown DECIMAL(10, 4),
    win_rate DECIMAL(10, 4),
    total_trades INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (strategy_id) REFERENCES trading_strategies(id) ON DELETE CASCADE
);

CREATE INDEX idx_backtest_strategy_id ON backtest_results(strategy_id);

-- 뉴스 소스 (PostgreSQL에도 저장)
CREATE TABLE IF NOT EXISTS news_sources (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    api_key VARCHAR(255) NOT NULL,
    base_url VARCHAR(255) NOT NULL,
    request_limit_per_minute INTEGER DEFAULT 100,
    is_active BOOLEAN DEFAULT true,
    last_fetched_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_news_sources_active ON news_sources(is_active);

-- 트리거: updated_at 자동 업데이트
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_trading_pairs_updated_at BEFORE UPDATE ON trading_pairs
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_orders_updated_at BEFORE UPDATE ON orders
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_portfolios_updated_at BEFORE UPDATE ON portfolios
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_trading_strategies_updated_at BEFORE UPDATE ON trading_strategies
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_news_sources_updated_at BEFORE UPDATE ON news_sources
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- 초기 데이터: 인기 거래 쌍
INSERT INTO trading_pairs (symbol, base_asset, quote_asset, exchange, min_order_size, tick_size, maker_fee, taker_fee)
VALUES
    ('BTC/USDT', 'BTC', 'USDT', 'binance', 0.00001, 0.01, 0.001, 0.001),
    ('ETH/USDT', 'ETH', 'USDT', 'binance', 0.0001, 0.01, 0.001, 0.001),
    ('BNB/USDT', 'BNB', 'USDT', 'binance', 0.001, 0.01, 0.001, 0.001),
    ('BTC/KRW', 'BTC', 'KRW', 'upbit', 0.0001, 1000, 0.0005, 0.0005),
    ('ETH/KRW', 'ETH', 'KRW', 'upbit', 0.001, 100, 0.0005, 0.0005)
ON CONFLICT (symbol, exchange) DO NOTHING;
