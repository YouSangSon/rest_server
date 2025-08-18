-- PostgreSQL 데이터베이스 초기화 스크립트
-- REST Server 프로젝트용

-- 사용자 테이블 생성
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    username VARCHAR(100) NOT NULL,
    password VARCHAR(255),
    provider VARCHAR(20) DEFAULT 'local',
    provider_id VARCHAR(255),
    profile_image VARCHAR(500),
    is_enabled BOOLEAN DEFAULT TRUE,
    is_account_non_expired BOOLEAN DEFAULT TRUE,
    is_account_non_locked BOOLEAN DEFAULT TRUE,
    is_credentials_non_expired BOOLEAN DEFAULT TRUE,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 로또 테이블 생성
CREATE TABLE IF NOT EXISTS lotto (
    id BIGSERIAL PRIMARY KEY,
    drw_no INTEGER UNIQUE NOT NULL,
    drw_no_date DATE NOT NULL,
    drwt_no1 INTEGER NOT NULL,
    drwt_no2 INTEGER NOT NULL,
    drwt_no3 INTEGER NOT NULL,
    drwt_no4 INTEGER NOT NULL,
    drwt_no5 INTEGER NOT NULL,
    drwt_no6 INTEGER NOT NULL,
    bnus_no INTEGER NOT NULL,
    first_przwner_co INTEGER NOT NULL,
    first_accumamnt BIGINT NOT NULL,
    first_winamnt BIGINT NOT NULL,
    tot_sellamnt BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 연금 로또 테이블 생성
CREATE TABLE IF NOT EXISTS annuity_lotto (
    id BIGSERIAL PRIMARY KEY,
    drw_no INTEGER UNIQUE NOT NULL,
    drw_no_date DATE NOT NULL,
    drwt_no1 INTEGER NOT NULL,
    drwt_no2 INTEGER NOT NULL,
    drwt_no3 INTEGER NOT NULL,
    drwt_no4 INTEGER NOT NULL,
    drwt_no5 INTEGER NOT NULL,
    drwt_no6 INTEGER NOT NULL,
    bnus_no INTEGER NOT NULL,
    first_przwner_co INTEGER NOT NULL,
    first_accumamnt BIGINT NOT NULL,
    first_winamnt BIGINT NOT NULL,
    tot_sellamnt BIGT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_provider_provider_id ON users(provider, provider_id);
CREATE INDEX IF NOT EXISTS idx_lotto_drw_no ON lotto(drw_no);
CREATE INDEX IF NOT EXISTS idx_lotto_drw_no_date ON lotto(drw_no_date);
CREATE INDEX IF NOT EXISTS idx_annuity_lotto_drw_no ON annuity_lotto(drw_no);
CREATE INDEX IF NOT EXISTS idx_annuity_lotto_drw_no_date ON annuity_lotto(drw_no_date);

-- 시퀀스 생성 (필요한 경우)
CREATE SEQUENCE IF NOT EXISTS users_id_seq START 1;
CREATE SEQUENCE IF NOT EXISTS lotto_id_seq START 1;
CREATE SEQUENCE IF NOT EXISTS annuity_lotto_id_seq START 1;

-- 테이블에 시퀀스 연결
ALTER TABLE users ALTER COLUMN id SET DEFAULT nextval('users_id_seq');
ALTER TABLE lotto ALTER COLUMN id SET DEFAULT nextval('lotto_id_seq');
ALTER TABLE annuity_lotto ALTER COLUMN id SET DEFAULT nextval('annuity_lotto_id_seq');

-- 권한 설정
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO postgres;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO postgres;

-- 샘플 데이터 삽입 (테스트용)
INSERT INTO users (email, username, provider, provider_id) VALUES
('admin@example.com', '관리자', 'local', NULL),
('test@example.com', '테스트사용자', 'local', NULL)
ON CONFLICT (email) DO NOTHING;

-- 샘플 로또 데이터 삽입 (테스트용)
INSERT INTO lotto (drw_no, drw_no_date, drwt_no1, drwt_no2, drwt_no3, drwt_no4, drwt_no5, drwt_no6, bnus_no, first_przwner_co, first_accumamnt, first_winamnt, tot_sellamnt) VALUES
(1001, '2024-01-01', 1, 2, 3, 4, 5, 6, 7, 10, 1000000000, 100000000, 10000000000),
(1002, '2024-01-08', 8, 9, 10, 11, 12, 13, 14, 15, 2000000000, 200000000, 20000000000)
ON CONFLICT (drw_no) DO NOTHING;

-- 테이블 정보 조회 뷰 생성
CREATE OR REPLACE VIEW table_info AS
SELECT 
    schemaname,
    tablename,
    tableowner,
    tablespace,
    hasindexes,
    hasrules,
    hastriggers
FROM pg_tables
WHERE schemaname = 'public';

-- 사용자 통계 뷰 생성
CREATE OR REPLACE VIEW user_stats AS
SELECT 
    provider,
    COUNT(*) as user_count,
    COUNT(CASE WHEN is_enabled THEN 1 END) as active_users,
    COUNT(CASE WHEN last_login_at IS NOT NULL THEN 1 END) as logged_in_users,
    MAX(last_login_at) as last_login
FROM users
GROUP BY provider;

-- 로또 통계 뷰 생성
CREATE OR REPLACE VIEW lotto_stats AS
SELECT 
    COUNT(*) as total_draws,
    MIN(drw_no) as first_draw,
    MAX(drw_no) as last_draw,
    MIN(drw_no_date) as earliest_date,
    MAX(drw_no_date) as latest_date,
    AVG(first_winamnt) as avg_first_prize,
    SUM(tot_sellamnt) as total_sales
FROM lotto;

-- 함수 생성: 사용자 마지막 로그인 업데이트
CREATE OR REPLACE FUNCTION update_user_last_login(user_email VARCHAR)
RETURNS VOID AS $$
BEGIN
    UPDATE users 
    SET last_login_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP
    WHERE email = user_email;
END;
$$ LANGUAGE plpgsql;

-- 함수 생성: 로또 당첨번호 검증
CREATE OR REPLACE FUNCTION validate_lotto_numbers(
    num1 INTEGER, num2 INTEGER, num3 INTEGER, 
    num4 INTEGER, num5 INTEGER, num6 INTEGER, bonus INTEGER
)
RETURNS BOOLEAN AS $$
BEGIN
    -- 1-45 범위 검증
    IF num1 < 1 OR num1 > 45 OR num2 < 1 OR num2 > 45 OR
       num3 < 1 OR num3 > 45 OR num4 < 1 OR num4 > 45 OR
       num5 < 1 OR num5 > 45 OR num6 < 1 OR num6 > 45 OR
       bonus < 1 OR bonus > 45 THEN
        RETURN FALSE;
    END IF;
    
    -- 중복 번호 검증
    IF num1 = num2 OR num1 = num3 OR num1 = num4 OR num1 = num5 OR num1 = num6 OR
       num2 = num3 OR num2 = num4 OR num2 = num5 OR num2 = num6 OR
       num3 = num4 OR num3 = num5 OR num3 = num6 OR
       num4 = num5 OR num4 = num6 OR num5 = num6 THEN
        RETURN FALSE;
    END IF;
    
    -- 보너스 번호가 당첨번호와 중복되지 않는지 검증
    IF bonus = num1 OR bonus = num2 OR bonus = num3 OR 
       bonus = num4 OR bonus = num5 OR bonus = num6 THEN
        RETURN FALSE;
    END IF;
    
    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;

-- 트리거 함수: updated_at 자동 업데이트
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 트리거 생성
CREATE TRIGGER update_users_updated_at 
    BEFORE UPDATE ON users 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_lotto_updated_at 
    BEFORE UPDATE ON lotto 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_annuity_lotto_updated_at 
    BEFORE UPDATE ON annuity_lotto 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- 댓글 추가
COMMENT ON TABLE users IS '사용자 정보 테이블';
COMMENT ON TABLE lotto IS '로또 당첨 정보 테이블';
COMMENT ON TABLE annuity_lotto IS '연금 로또 당첨 정보 테이블';
COMMENT ON COLUMN users.provider IS '인증 제공자 (local, google, github, kakao)';
COMMENT ON COLUMN users.provider_id IS 'OAuth2 제공자의 사용자 ID';
COMMENT ON COLUMN lotto.drw_no IS '로또 회차 번호';
COMMENT ON COLUMN lotto.drwt_no1 TO lotto.drwt_no6 IS '당첨번호 1-6';
COMMENT ON COLUMN lotto.bnus_no IS '보너스 번호';
COMMENT ON COLUMN lotto.first_przwner_co IS '1등 당첨자 수';
COMMENT ON COLUMN lotto.first_accumamnt IS '1등 당첨금액';
COMMENT ON COLUMN lotto.first_winamnt IS '1등 1인당 당첨금액';
COMMENT ON COLUMN lotto.tot_sellamnt IS '총 판매금액';
