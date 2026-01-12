INSERT IGNORE INTO roles (id, code, name) VALUES
 (1, 'admin', '管理员'),
 (2, 'organizer', '组织者'),
 (3, 'student', '学生');

INSERT INTO users (id, username, password_hash, name, email, phone, enabled, created_at)
VALUES
 (1, 'admin', '$2a$10$/RcMQLLMr/KZop.cEWVD3.rEFDuehjniJ.x8a6eAmDm/FHyk7YG2m', '管理员', 'admin@example.com', '13000000000', 1, NOW()),
 (2, 'org1', '$2a$10$/RcMQLLMr/KZop.cEWVD3.rEFDuehjniJ.x8a6eAmDm/FHyk7YG2m', '组织者A', 'org1@example.com', '13100000000', 1, NOW()),
 (3, 'stu1', '$2a$10$/RcMQLLMr/KZop.cEWVD3.rEFDuehjniJ.x8a6eAmDm/FHyk7YG2m', '学生A', 'stu1@example.com', '13200000000', 1, NOW())
ON DUPLICATE KEY UPDATE username=username;

INSERT IGNORE INTO user_role (user_id, role_id) VALUES
 (1, 1), (2, 2), (3, 3);

INSERT IGNORE INTO organizations (id, name, description, contact, created_at) VALUES
 (1, '学生会', '校级学生会', 'student@campus.com', NOW()),
 (2, '音乐社', '校园音乐社团', 'music@campus.com', NOW());

INSERT IGNORE INTO events (id, org_id, creator_id, title, description, category, tags, location, signup_start_time, signup_end_time, start_time, end_time, capacity, status, created_at, updated_at) VALUES
 (1, 1, 2, '校园音乐节', '周五晚间音乐演出，欢迎报名', '演出', '音乐,夜场', '礼堂', NOW(), NOW() + INTERVAL 2 DAY - INTERVAL 2 HOUR, NOW() + INTERVAL 2 DAY, NOW() + INTERVAL 2 DAY + INTERVAL 2 HOUR, 100, 'published', NOW(), NOW()),
 (2, 2, 2, '吉他体验营', '零基础吉他体验课', '培训', '吉他,体验', '活动室203', NOW(), NOW() + INTERVAL 3 DAY - INTERVAL 2 HOUR, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 3 DAY + INTERVAL 2 HOUR, 30, 'published', NOW(), NOW());

INSERT IGNORE INTO registration_blacklist (user_id, reason, created_at) VALUES
 (9999, '示例黑名单用户', NOW());

-- 批量演示数据（业务表，各 50 条）
-- 仅扩充 events 50 条，复用现有组织/用户
INSERT IGNORE INTO events (id, org_id, creator_id, title, description, category, tags, location, signup_start_time, signup_end_time, start_time, end_time, capacity, status, created_at, updated_at) VALUES
 (101, 1, 2, '批量活动101', '批量活动描述', '演出', '批量,演出', '教学楼A-101', NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 2 HOUR, 80, 'draft', NOW(), NOW()),
 (102, 2, 2, '批量活动102', '批量活动描述', '培训', '批量,培训', '教学楼A-102', NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 2 HOUR, 60, 'pending_review', NOW(), NOW()),
 (103, 1, 2, '批量活动103', '批量活动描述', '演出', '批量,演出', '教学楼A-103', NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 2 HOUR, 80, 'published', NOW(), NOW()),
 (104, 2, 2, '批量活动104', '批量活动描述', '培训', '批量,培训', '教学楼A-104', NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 2 HOUR, 60, 'rejected', NOW(), NOW()),
 (105, 1, 2, '批量活动105', '批量活动描述', '演出', '批量,演出', '教学楼A-105', NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 2 HOUR, 80, 'canceled', NOW(), NOW()),
 (106, 2, 2, '批量活动106', '批量活动描述', '培训', '批量,培训', '教学楼A-106', NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 2 HOUR, 60, 'archived', NOW(), NOW()),
 (107, 1, 2, '批量活动107', '批量活动描述', '演出', '批量,演出', '教学楼A-107', NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 2 HOUR, 80, 'draft', NOW(), NOW()),
 (108, 2, 2, '批量活动108', '批量活动描述', '培训', '批量,培训', '教学楼A-108', NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 2 HOUR, 60, 'pending_review', NOW(), NOW()),
 (109, 1, 2, '批量活动109', '批量活动描述', '演出', '批量,演出', '教学楼A-109', NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 2 HOUR, 80, 'published', NOW(), NOW()),
 (110, 2, 2, '批量活动110', '批量活动描述', '培训', '批量,培训', '教学楼A-110', NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 2 HOUR, 60, 'rejected', NOW(), NOW()),
 (111, 1, 2, '批量活动111', '批量活动描述', '演出', '批量,演出', '教学楼A-111', NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 2 HOUR, 80, 'canceled', NOW(), NOW()),
 (112, 2, 2, '批量活动112', '批量活动描述', '培训', '批量,培训', '教学楼A-112', NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 2 HOUR, 60, 'archived', NOW(), NOW()),
 (113, 1, 2, '批量活动113', '批量活动描述', '演出', '批量,演出', '教学楼A-113', NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 2 HOUR, 80, 'draft', NOW(), NOW()),
 (114, 2, 2, '批量活动114', '批量活动描述', '培训', '批量,培训', '教学楼A-114', NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 2 HOUR, 60, 'pending_review', NOW(), NOW()),
 (115, 1, 2, '批量活动115', '批量活动描述', '演出', '批量,演出', '教学楼A-115', NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 2 HOUR, 80, 'published', NOW(), NOW()),
 (116, 2, 2, '批量活动116', '批量活动描述', '培训', '批量,培训', '教学楼A-116', NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 2 HOUR, 60, 'rejected', NOW(), NOW()),
 (117, 1, 2, '批量活动117', '批量活动描述', '演出', '批量,演出', '教学楼A-117', NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 2 HOUR, 80, 'canceled', NOW(), NOW()),
 (118, 2, 2, '批量活动118', '批量活动描述', '培训', '批量,培训', '教学楼A-118', NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 2 HOUR, 60, 'archived', NOW(), NOW()),
 (119, 1, 2, '批量活动119', '批量活动描述', '演出', '批量,演出', '教学楼A-119', NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 2 HOUR, 80, 'draft', NOW(), NOW()),
 (120, 2, 2, '批量活动120', '批量活动描述', '培训', '批量,培训', '教学楼A-120', NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 2 HOUR, 60, 'pending_review', NOW(), NOW()),
 (121, 1, 2, '批量活动121', '批量活动描述', '演出', '批量,演出', '教学楼A-121', NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 2 HOUR, 80, 'published', NOW(), NOW()),
 (122, 2, 2, '批量活动122', '批量活动描述', '培训', '批量,培训', '教学楼A-122', NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 2 HOUR, 60, 'rejected', NOW(), NOW()),
 (123, 1, 2, '批量活动123', '批量活动描述', '演出', '批量,演出', '教学楼A-123', NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 2 HOUR, 80, 'canceled', NOW(), NOW()),
 (124, 2, 2, '批量活动124', '批量活动描述', '培训', '批量,培训', '教学楼A-124', NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 2 HOUR, 60, 'archived', NOW(), NOW()),
 (125, 1, 2, '批量活动125', '批量活动描述', '演出', '批量,演出', '教学楼A-125', NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 2 HOUR, 80, 'draft', NOW(), NOW()),
 (126, 2, 2, '批量活动126', '批量活动描述', '培训', '批量,培训', '教学楼A-126', NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 2 HOUR, 60, 'pending_review', NOW(), NOW()),
 (127, 1, 2, '批量活动127', '批量活动描述', '演出', '批量,演出', '教学楼A-127', NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 2 HOUR, 80, 'published', NOW(), NOW()),
 (128, 2, 2, '批量活动128', '批量活动描述', '培训', '批量,培训', '教学楼A-128', NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 2 HOUR, 60, 'rejected', NOW(), NOW()),
 (129, 1, 2, '批量活动129', '批量活动描述', '演出', '批量,演出', '教学楼A-129', NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 2 HOUR, 80, 'canceled', NOW(), NOW()),
 (130, 2, 2, '批量活动130', '批量活动描述', '培训', '批量,培训', '教学楼A-130', NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 2 HOUR, 60, 'archived', NOW(), NOW()),
 (131, 1, 2, '批量活动131', '批量活动描述', '演出', '批量,演出', '教学楼A-131', NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 2 HOUR, 80, 'draft', NOW(), NOW()),
 (132, 2, 2, '批量活动132', '批量活动描述', '培训', '批量,培训', '教学楼A-132', NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 2 HOUR, 60, 'pending_review', NOW(), NOW()),
 (133, 1, 2, '批量活动133', '批量活动描述', '演出', '批量,演出', '教学楼A-133', NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 2 HOUR, 80, 'published', NOW(), NOW()),
 (134, 2, 2, '批量活动134', '批量活动描述', '培训', '批量,培训', '教学楼A-134', NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 2 HOUR, 60, 'rejected', NOW(), NOW()),
 (135, 1, 2, '批量活动135', '批量活动描述', '演出', '批量,演出', '教学楼A-135', NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 2 HOUR, 80, 'canceled', NOW(), NOW()),
 (136, 2, 2, '批量活动136', '批量活动描述', '培训', '批量,培训', '教学楼A-136', NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 2 HOUR, 60, 'archived', NOW(), NOW()),
 (137, 1, 2, '批量活动137', '批量活动描述', '演出', '批量,演出', '教学楼A-137', NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 2 HOUR, 80, 'draft', NOW(), NOW()),
 (138, 2, 2, '批量活动138', '批量活动描述', '培训', '批量,培训', '教学楼A-138', NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 2 HOUR, 60, 'pending_review', NOW(), NOW()),
 (139, 1, 2, '批量活动139', '批量活动描述', '演出', '批量,演出', '教学楼A-139', NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 2 HOUR, 80, 'published', NOW(), NOW()),
 (140, 2, 2, '批量活动140', '批量活动描述', '培训', '批量,培训', '教学楼A-140', NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 2 HOUR, 60, 'rejected', NOW(), NOW()),
 (141, 1, 2, '批量活动141', '批量活动描述', '演出', '批量,演出', '教学楼A-141', NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 2 HOUR, 80, 'canceled', NOW(), NOW()),
 (142, 2, 2, '批量活动142', '批量活动描述', '培训', '批量,培训', '教学楼A-142', NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 2 HOUR, 60, 'archived', NOW(), NOW()),
 (143, 1, 2, '批量活动143', '批量活动描述', '演出', '批量,演出', '教学楼A-143', NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 2 HOUR, 80, 'draft', NOW(), NOW()),
 (144, 2, 2, '批量活动144', '批量活动描述', '培训', '批量,培训', '教学楼A-144', NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 2 HOUR, 60, 'pending_review', NOW(), NOW()),
 (145, 1, 2, '批量活动145', '批量活动描述', '演出', '批量,演出', '教学楼A-145', NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 2 HOUR, 80, 'published', NOW(), NOW()),
 (146, 2, 2, '批量活动146', '批量活动描述', '培训', '批量,培训', '教学楼A-146', NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 2 HOUR, 60, 'rejected', NOW(), NOW()),
 (147, 1, 2, '批量活动147', '批量活动描述', '演出', '批量,演出', '教学楼A-147', NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 2 HOUR, 80, 'canceled', NOW(), NOW()),
 (148, 2, 2, '批量活动148', '批量活动描述', '培训', '批量,培训', '教学楼A-148', NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 2 HOUR, 60, 'archived', NOW(), NOW()),
 (149, 1, 2, '批量活动149', '批量活动描述', '演出', '批量,演出', '教学楼A-149', NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 2 HOUR, 80, 'draft', NOW(), NOW()),
 (150, 2, 2, '批量活动150', '批量活动描述', '培训', '批量,培训', '教学楼A-150', NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 2 HOUR, 60, 'pending_review', NOW(), NOW());

