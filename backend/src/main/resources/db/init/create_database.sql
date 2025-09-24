-- 数据库创建脚本（手动执行）
-- 作者: xiaolei
-- 说明: 在首次部署时手动执行此脚本创建数据库

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `badminton`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci
    COMMENT '约球系统数据库';

-- 使用数据库
USE `badminton`;

-- 显示创建结果
SHOW DATABASES LIKE 'badminton';
SHOW VARIABLES LIKE 'character_set_database';
SHOW VARIABLES LIKE 'collation_database';