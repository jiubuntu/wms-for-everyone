-- Mock 데이터: 로그인 테스트용 시스템관리자 계정
-- 로그인 정보: admin@wms.com / Admin1234!
-- (비밀번호는 BCryptPasswordEncoder로 이미 해시된 값입니다)
-- 서버 기동 시마다 실행되므로(spring.sql.init) 중복 삽입되지 않도록 WHERE NOT EXISTS로 가드합니다.

INSERT INTO companies (name, business_number, status, active, created_at, updated_at)
SELECT '모두의 WMS', '000-00-00000', 'ACTIVE', true, now(), now()
WHERE NOT EXISTS (
    SELECT 1 FROM companies WHERE business_number = '000-00-00000'
);

INSERT INTO users (company_id, warehouse_id, email, password, name, phone, role, status, login_failed_count, active, created_at, updated_at)
SELECT
    (SELECT id FROM companies WHERE business_number = '000-00-00000'),
    NULL,
    'admin@wms.com',
    '$2a$10$TG6Tu.hL5OdKFzB1Igh60.RrZrw8pYIb/Y11C.E3BfT87iUMM9PVO',
    '시스템관리자',
    '010-0000-0000',
    'SYSTEM_ADMIN',
    'ACTIVE',
    0,
    true,
    now(),
    now()
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'admin@wms.com'
);
