CREATE DATABASE IF NOT EXISTS xiaoyuanhuodongfabuyubaomingpingtai DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE xiaoyuanhuodongfabuyubaomingpingtai;

CREATE TABLE IF NOT EXISTS roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(64) UNIQUE NOT NULL,
    name VARCHAR(64) NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(128),
    email VARCHAR(128),
    phone VARCHAR(32),
    enabled TINYINT(1) DEFAULT 1,
    created_at DATETIME,
    updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    UNIQUE KEY uk_user_role (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS organizations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(128) NOT NULL,
    description TEXT,
    contact VARCHAR(128),
    created_at DATETIME
);

CREATE TABLE IF NOT EXISTS events (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    org_id BIGINT,
    creator_id BIGINT,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    category VARCHAR(64),
    tags VARCHAR(255),
    location VARCHAR(255),
    signup_start_time DATETIME,
    signup_end_time DATETIME,
    start_time DATETIME,
    end_time DATETIME,
    capacity INT NOT NULL,
    status VARCHAR(32),
    checkin_code VARCHAR(64),
    checkin_valid_until DATETIME,
    created_at DATETIME,
    updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS registrations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(32),
    created_at DATETIME,
    canceled_at DATETIME,
    checkin_at DATETIME,
    UNIQUE KEY uk_event_user (event_id, user_id)
);

CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    type VARCHAR(64),
    title VARCHAR(200),
    content TEXT,
    read_flag TINYINT(1) DEFAULT 0,
    created_at DATETIME
);

CREATE TABLE IF NOT EXISTS registration_blacklist (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    reason VARCHAR(255),
    created_at DATETIME,
    UNIQUE KEY uk_user (user_id)
);
