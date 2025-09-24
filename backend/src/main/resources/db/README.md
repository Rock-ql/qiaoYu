# 数据库初始化说明

## 数据库结构

本项目使用MySQL作为主数据存储，Redis作为缓存层。

### 表结构概览

- **user** - 用户表，存储用户基本信息
- **booking_activity** - 约球活动表，存储活动信息
- **participation** - 参与记录表，记录用户参与活动的关系
- **expense_record** - 费用记录表，存储活动相关费用
- **expense_share** - 费用分摊表，记录费用分摊详情

## 初始化步骤

### 1. 创建数据库（首次部署）

在首次部署时，需要先手动创建数据库：

```bash
# 方式一：使用MySQL命令行
mysql -u root -p < src/main/resources/db/init/create_database.sql

# 方式二：MySQL命令行手动执行
mysql -u root -p
CREATE DATABASE IF NOT EXISTS `badminton` DEFAULT CHARACTER SET utf8mb4 DEFAULT COLLATE utf8mb4_unicode_ci;
USE badminton;
```

### 2. 自动表结构初始化

配置好数据库后，Flyway会自动执行表结构初始化：

1. **创建表结构**：执行DDL脚本，创建所有必要的表和索引
2. **设置外键约束**：建立表之间的引用完整性

### 配置要求

确保`application-local.yml`中的数据库配置正确：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/badminton?serverTimezone=GMT%2B8&useUnicode=true&characterEncoding=utf-8&autoReconnect=true&failOverReadOnly=false&useSSL=false
    username: root
    password: 123456
  jpa:
    hibernate:
      ddl-auto: validate
      naming:
        physical-strategy: org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
  flyway:
    enabled: true
    baseline-on-migrate: true
```

### 启动步骤

1. **启动MySQL服务**：确保MySQL 8.0+正在运行
2. **创建数据库**：执行`create_database.sql`脚本
3. **启动应用**：运行`mvn spring-boot:run`
4. **自动表结构创建**：Flyway会自动执行表结构初始化

## 手动初始化

如果需要手动初始化数据库，可以直接执行DDL脚本：

```sql
-- 1. 连接到MySQL
mysql -u root -p

-- 2. 执行初始化脚本
source /path/to/V001__Create_Base_Tables.sql
```

## 数据迁移

如果之前使用纯Redis存储，可以使用数据迁移接口：

```bash
# 启动应用后，执行数据迁移
curl -X POST http://localhost:8080/api/migration/migrate
```

此接口会将Redis中的现有数据迁移到MySQL中。

## 数据库设计原则

1. **字符集**：使用`utf8mb4`确保完整的Unicode支持
2. **整理规则**：使用`utf8mb4_unicode_ci`确保正确的排序和比较
3. **存储引擎**：统一使用`InnoDB`确保事务支持和外键约束
4. **字段设计**：所有表包含标准的基础字段（tenant、state、created_at等）
5. **索引优化**：为常用查询字段建立合适的索引

## 注意事项

- 首次运行前确保MySQL服务已启动
- 确保配置的用户有数据库创建权限
- 生产环境建议使用专用的数据库用户而不是root
- 定期备份数据库数据

作者: xiaolei
更新时间: 2025-01-24