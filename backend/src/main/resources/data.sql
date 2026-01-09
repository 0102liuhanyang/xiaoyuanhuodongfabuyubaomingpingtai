INSERT IGNORE INTO roles (id, code, name) VALUES
 (1, 'admin', '管理员'),
 (2, 'organizer', '组织者'),
 (3, 'student', '学生');

INSERT INTO users (id, username, password_hash, name, email, phone, enabled, created_at)
VALUES
 (1, 'admin', '$2a$10$7EqJtq98hPqEX7fNZaFWoO.k9t1u78r1e5XguVzKk1pVcTQ3F0bBm', '管理员', 'admin@example.com', '13000000000', 1, NOW()),
 (2, 'org1', '$2a$10$7EqJtq98hPqEX7fNZaFWoO.k9t1u78r1e5XguVzKk1pVcTQ3F0bBm', '组织者A', 'org1@example.com', '13100000000', 1, NOW()),
 (3, 'stu1', '$2a$10$7EqJtq98hPqEX7fNZaFWoO.k9t1u78r1e5XguVzKk1pVcTQ3F0bBm', '学生A', 'stu1@example.com', '13200000000', 1, NOW())
ON DUPLICATE KEY UPDATE username=username;

INSERT IGNORE INTO user_role (user_id, role_id) VALUES
 (1, 1), (2, 2), (3, 3);

INSERT IGNORE INTO organizations (id, name, description, contact, created_at) VALUES
 (1, '学生会', '校级学生会', 'student@campus.com', NOW()),
 (2, '音乐社', '校园音乐社团', 'music@campus.com', NOW());

INSERT IGNORE INTO events (id, org_id, creator_id, title, description, category, tags, location, start_time, end_time, capacity, status, cover_url, created_at, updated_at) VALUES
 (1, 1, 2, '校园音乐节', '周五晚间音乐演出，欢迎报名', '演出', '音乐,夜场', '礼堂', NOW() + INTERVAL 2 DAY, NOW() + INTERVAL 2 DAY + INTERVAL 2 HOUR, 100, 'published', NULL, NOW(), NOW()),
 (2, 2, 2, '吉他体验营', '零基础吉他体验课', '培训', '吉他,体验', '活动室203', NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 3 DAY + INTERVAL 2 HOUR, 30, 'published', NULL, NOW(), NOW());
