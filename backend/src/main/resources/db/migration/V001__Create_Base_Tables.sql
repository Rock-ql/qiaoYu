-- 创建MySQL数据库表结构
-- 作者: xiaolei
-- 说明: 将Redis存储的实体迁移到MySQL持久化存储

-- 1. 用户表
CREATE TABLE `user` (
    `id` varchar(36) NOT NULL COMMENT '用户ID',
    `phone` varchar(11) NOT NULL DEFAULT '' COMMENT '手机号',
    `nickname` varchar(50) NOT NULL DEFAULT '' COMMENT '用户昵称',
    `password` varchar(255) NOT NULL DEFAULT '' COMMENT '加密密码',
    `avatar` varchar(500) NOT NULL DEFAULT '' COMMENT '头像URL',
    `status` int(11) NOT NULL DEFAULT 1 COMMENT '用户状态：1-正常，2-禁用',
    `total_activities` int(11) NOT NULL DEFAULT 0 COMMENT '参与活动总数',
    `total_expense` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '总消费金额',
    `wx_open_id` varchar(50) NOT NULL DEFAULT '' COMMENT '微信OpenID',
    `wx_union_id` varchar(50) NOT NULL DEFAULT '' COMMENT '微信UnionID',

    -- 基础字段
    `tenant` int(11) NOT NULL DEFAULT 1 COMMENT '租户，为了应对私有部署',
    `state` int(11) NOT NULL DEFAULT 1 COMMENT '状态 0 未知 1 上架 2 下架',
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at` datetime DEFAULT NULL COMMENT '删除时间',
    `organization_id` int(11) DEFAULT 0 COMMENT '组织id',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_wx_open_id` (`wx_open_id`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 活动表
CREATE TABLE `booking_activity` (
    `id` varchar(36) NOT NULL COMMENT '活动ID',
    `title` varchar(100) NOT NULL DEFAULT '' COMMENT '活动标题',
    `organizer` varchar(36) NOT NULL DEFAULT '' COMMENT '发起人用户ID',
    `venue` varchar(100) NOT NULL DEFAULT '' COMMENT '场地名称',
    `address` varchar(200) NOT NULL DEFAULT '' COMMENT '详细地址',
    `start_time` datetime NOT NULL COMMENT '开始时间',
    `end_time` datetime NOT NULL COMMENT '结束时间',
    `max_players` int(11) NOT NULL DEFAULT 2 COMMENT '最大人数',
    `current_players` int(11) NOT NULL DEFAULT 1 COMMENT '当前人数',
    `fee` decimal(8,2) NOT NULL DEFAULT 0.00 COMMENT '预估费用',
    `description` text COMMENT '活动描述',
    `status` int(11) NOT NULL DEFAULT 1 COMMENT '活动状态：1-待确认 2-进行中 3-已完成 4-已取消',

    -- 基础字段
    `tenant` int(11) NOT NULL DEFAULT 1 COMMENT '租户，为了应对私有部署',
    `state` int(11) NOT NULL DEFAULT 1 COMMENT '状态 0 未知 1 上架 2 下架',
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at` datetime DEFAULT NULL COMMENT '删除时间',
    `organization_id` int(11) DEFAULT 0 COMMENT '组织id',

    PRIMARY KEY (`id`),
    KEY `idx_organizer` (`organizer`),
    KEY `idx_status` (`status`),
    KEY `idx_venue` (`venue`),
    KEY `idx_start_time` (`start_time`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='约球活动表';

-- 3. 参与记录表
CREATE TABLE `participation` (
    `id` varchar(36) NOT NULL COMMENT '参与记录ID',
    `activity_id` varchar(36) NOT NULL DEFAULT '' COMMENT '活动ID',
    `user_id` varchar(36) NOT NULL DEFAULT '' COMMENT '参与者用户ID',
    `status` int(11) NOT NULL DEFAULT 1 COMMENT '参与状态：1-已确认 2-已取消',
    `join_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '参与时间',
    `is_organizer` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否为发起人：0-否 1-是',

    -- 基础字段
    `tenant` int(11) NOT NULL DEFAULT 1 COMMENT '租户，为了应对私有部署',
    `state` int(11) NOT NULL DEFAULT 1 COMMENT '状态 0 未知 1 上架 2 下架',
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at` datetime DEFAULT NULL COMMENT '删除时间',
    `organization_id` int(11) DEFAULT 0 COMMENT '组织id',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_activity_user` (`activity_id`, `user_id`),
    KEY `idx_activity_id` (`activity_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_join_time` (`join_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='参与记录表';

-- 4. 费用记录表
CREATE TABLE `expense_record` (
    `id` varchar(36) NOT NULL COMMENT '费用记录ID',
    `activity_id` varchar(36) NOT NULL DEFAULT '' COMMENT '关联的活动ID',
    `payer_id` varchar(36) NOT NULL DEFAULT '' COMMENT '付款人用户ID',
    `type` varchar(20) NOT NULL DEFAULT 'venue' COMMENT '费用类型：venue-场地费 food-餐饮费 transport-交通费 other-其他',
    `description` varchar(200) NOT NULL DEFAULT '' COMMENT '费用描述',
    `total_amount` decimal(8,2) NOT NULL DEFAULT 0.01 COMMENT '总金额',
    `split_method` varchar(20) NOT NULL DEFAULT 'equal' COMMENT '分摊方式：equal-平均分摊 custom-自定义分摊',

    -- 基础字段
    `tenant` int(11) NOT NULL DEFAULT 1 COMMENT '租户，为了应对私有部署',
    `state` int(11) NOT NULL DEFAULT 1 COMMENT '状态 0 未知 1 上架 2 下架',
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at` datetime DEFAULT NULL COMMENT '删除时间',
    `organization_id` int(11) DEFAULT 0 COMMENT '组织id',

    PRIMARY KEY (`id`),
    KEY `idx_activity_id` (`activity_id`),
    KEY `idx_payer_id` (`payer_id`),
    KEY `idx_type` (`type`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='费用记录表';

-- 5. 费用分摊表
CREATE TABLE `expense_share` (
    `id` varchar(36) NOT NULL COMMENT '分摊记录ID',
    `expense_id` varchar(36) NOT NULL DEFAULT '' COMMENT '关联的费用记录ID',
    `user_id` varchar(36) NOT NULL DEFAULT '' COMMENT '分摊用户ID',
    `amount` decimal(8,2) NOT NULL DEFAULT 0.01 COMMENT '分摊金额',
    `status` int(11) NOT NULL DEFAULT 1 COMMENT '分摊状态：1-待结算 2-已结算',
    `settled_at` datetime DEFAULT NULL COMMENT '结算时间',

    -- 基础字段
    `tenant` int(11) NOT NULL DEFAULT 1 COMMENT '租户，为了应对私有部署',
    `state` int(11) NOT NULL DEFAULT 1 COMMENT '状态 0 未知 1 上架 2 下架',
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at` datetime DEFAULT NULL COMMENT '删除时间',
    `organization_id` int(11) DEFAULT 0 COMMENT '组织id',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_expense_user` (`expense_id`, `user_id`),
    KEY `idx_expense_id` (`expense_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_settled_at` (`settled_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='费用分摊表';

-- 创建外键约束
ALTER TABLE `booking_activity` ADD CONSTRAINT `fk_activity_organizer` FOREIGN KEY (`organizer`) REFERENCES `user` (`id`) ON DELETE RESTRICT;
ALTER TABLE `participation` ADD CONSTRAINT `fk_participation_activity` FOREIGN KEY (`activity_id`) REFERENCES `booking_activity` (`id`) ON DELETE CASCADE;
ALTER TABLE `participation` ADD CONSTRAINT `fk_participation_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;
ALTER TABLE `expense_record` ADD CONSTRAINT `fk_expense_activity` FOREIGN KEY (`activity_id`) REFERENCES `booking_activity` (`id`) ON DELETE CASCADE;
ALTER TABLE `expense_record` ADD CONSTRAINT `fk_expense_payer` FOREIGN KEY (`payer_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT;
ALTER TABLE `expense_share` ADD CONSTRAINT `fk_share_expense` FOREIGN KEY (`expense_id`) REFERENCES `expense_record` (`id`) ON DELETE CASCADE;
ALTER TABLE `expense_share` ADD CONSTRAINT `fk_share_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;