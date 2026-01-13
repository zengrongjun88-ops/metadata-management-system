-- ============================================================
-- 数据仓库元数据管理系统 - 数据库初始化脚本
-- 版本: 2.0.0
-- 日期: 2026-01-11
-- 说明: 完整的数据库表结构定义,索引设计
-- ============================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `metadata_mgmt` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `metadata_mgmt`;

-- 删除已存在的表(按依赖关系逆序删除)
DROP TABLE IF EXISTS `operation_history`;
DROP TABLE IF EXISTS `approval_flow`;
DROP TABLE IF EXISTS `metadata_field`;
DROP TABLE IF EXISTS `metadata_table`;

-- ============================================================
-- 表1: metadata_table (元数据表信息)
-- ============================================================
CREATE TABLE `metadata_table` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `table_name` VARCHAR(200) NOT NULL COMMENT '表名',
    `table_comment` TEXT COMMENT '表描述(富文本)',
    `database_name` VARCHAR(200) NOT NULL COMMENT '数据库名',
    `data_source` VARCHAR(50) NOT NULL COMMENT '数据源类型: Hive/Paimon/Iceberg/ClickHouse/BigQuery/StarRocks',
    `hive_account` VARCHAR(100) COMMENT 'Hive账号(租户): flight/hotel/train/bieng等',
    `table_size` BIGINT DEFAULT 0 COMMENT '表大小(字节)',
    `warehouse_layer` VARCHAR(50) COMMENT '数仓分层: ods/edw/cdm/mid/dim/dwd/dws/ads',
    `theme_first` VARCHAR(50) COMMENT '一级主题: usr/mkt/ord/fin/prd/prj/trf/srv',
    `theme_second` VARCHAR(50) COMMENT '二级主题',
    `sensitivity_level` VARCHAR(20) COMMENT '敏感等级: L1/L2/L3/L4',
    `importance_level` VARCHAR(20) COMMENT '重要等级: P0/P1/P2/P3',
    `partition_type` VARCHAR(50) COMMENT '分区类型: FULL/INCR/NONE',
    `partition_retention_days` INT COMMENT '分区保留天数',
    `update_frequency` VARCHAR(50) COMMENT '更新频率: REALTIME/HOURLY/DAILY/WEEKLY/MONTHLY/ON_DEMAND',
    `owner` VARCHAR(100) COMMENT '责任人(域账号)',
    `custom_tags` VARCHAR(500) COMMENT '自定义标签(JSON数组)',
    `create_sql` TEXT COMMENT '建表SQL',
    `create_by` VARCHAR(100) COMMENT '创建人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(100) COMMENT '修改人',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标识: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_database_table` (`database_name`, `table_name`, `deleted`),
    KEY `idx_data_source` (`data_source`),
    KEY `idx_warehouse_layer` (`warehouse_layer`),
    KEY `idx_theme` (`theme_first`, `theme_second`),
    KEY `idx_owner` (`owner`),
    KEY `idx_sensitivity_level` (`sensitivity_level`),
    KEY `idx_importance_level` (`importance_level`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='元数据表信息';

-- ============================================================
-- 表2: metadata_field (元数据字段信息)
-- ============================================================
CREATE TABLE `metadata_field` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `table_id` BIGINT NOT NULL COMMENT '所属表ID',
    `field_order` INT NOT NULL COMMENT '字段序号',
    `field_name` VARCHAR(200) NOT NULL COMMENT '字段名称',
    `field_comment` TEXT COMMENT '字段描述(富文本)',
    `field_type` VARCHAR(100) NOT NULL COMMENT '字段类型: 根据数据源确定',
    `is_primary_key` TINYINT DEFAULT 0 COMMENT '是否主键: 0-否, 1-是',
    `is_nullable` TINYINT DEFAULT 1 COMMENT '是否可为空: 0-否, 1-是',
    `is_encrypted` TINYINT DEFAULT 0 COMMENT '是否加密: 0-否, 1-是',
    `is_partition_key` TINYINT DEFAULT 0 COMMENT '是否分区键: 0-否, 1-是',
    `sensitivity_level` VARCHAR(20) COMMENT '敏感等级: L1/L2/L3/L4',
    `default_value` VARCHAR(500) COMMENT '默认值',
    `create_by` VARCHAR(100) COMMENT '创建人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(100) COMMENT '修改人',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标识: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_table_field` (`table_id`, `field_name`, `deleted`),
    KEY `idx_table_id` (`table_id`),
    KEY `idx_field_name` (`field_name`),
    KEY `idx_sensitivity_level` (`sensitivity_level`),
    CONSTRAINT `fk_field_table` FOREIGN KEY (`table_id`) REFERENCES `metadata_table` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='元数据字段信息';

-- ============================================================
-- 表3: approval_flow (审批流程)
-- ============================================================
CREATE TABLE `approval_flow` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `flow_no` VARCHAR(100) NOT NULL COMMENT '审批单号',
    `table_id` BIGINT NOT NULL COMMENT '表ID',
    `approval_type` VARCHAR(50) NOT NULL COMMENT '审批类型: CREATE/UPDATE/DELETE',
    `status` VARCHAR(50) NOT NULL COMMENT '审批状态: DRAFT/PENDING/APPROVED/REJECTED/CANCELLED/PUBLISHED',
    `submitter` VARCHAR(100) NOT NULL COMMENT '提交人',
    `submit_time` DATETIME NOT NULL COMMENT '提交时间',
    `approver` VARCHAR(100) COMMENT '审批人',
    `approve_time` DATETIME COMMENT '审批时间',
    `approve_comment` TEXT COMMENT '审批意见',
    `change_content` TEXT COMMENT '变更内容(JSON)',
    `create_by` VARCHAR(100) COMMENT '创建人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(100) COMMENT '修改人',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标识: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_flow_no` (`flow_no`),
    KEY `idx_table_id` (`table_id`),
    KEY `idx_status` (`status`),
    KEY `idx_submitter` (`submitter`),
    KEY `idx_approver` (`approver`),
    KEY `idx_submit_time` (`submit_time`),
    CONSTRAINT `fk_approval_table` FOREIGN KEY (`table_id`) REFERENCES `metadata_table` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审批流程';

-- ============================================================
-- 表4: operation_history (操作历史)
-- ============================================================
CREATE TABLE `operation_history` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `table_id` BIGINT NOT NULL COMMENT '表ID',
    `operation_type` VARCHAR(50) NOT NULL COMMENT '操作类型: CREATE/UPDATE/DELETE/PUBLISH/APPROVE/REJECT/QUERY',
    `operator` VARCHAR(100) NOT NULL COMMENT '操作人',
    `operation_time` DATETIME NOT NULL COMMENT '操作时间',
    `before_content` TEXT COMMENT '变更前内容(JSON)',
    `after_content` TEXT COMMENT '变更后内容(JSON)',
    `operation_desc` VARCHAR(500) COMMENT '操作描述',
    `create_by` VARCHAR(100) COMMENT '创建人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标识: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_table_id` (`table_id`),
    KEY `idx_operation_type` (`operation_type`),
    KEY `idx_operator` (`operator`),
    KEY `idx_operation_time` (`operation_time`),
    CONSTRAINT `fk_history_table` FOREIGN KEY (`table_id`) REFERENCES `metadata_table` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作历史';

-- ============================================================
-- 初始化测试数据
-- ============================================================

-- 插入测试表数据
INSERT INTO `metadata_table` (
    `table_name`, `table_comment`, `database_name`, `data_source`,
    `hive_account`, `warehouse_layer`, `theme_first`, `theme_second`,
    `sensitivity_level`, `importance_level`, `partition_type`,
    `partition_retention_days`, `update_frequency`, `owner`,
    `create_sql`, `create_by`
) VALUES (
    'test_user_order',
    '<p>测试用户订单表</p><p>包含用户的订单信息</p>',
    'test_db',
    'Hive',
    'flight',
    'cdm',
    'ord',
    'flight',
    'L2',
    'P1',
    'INCR',
    90,
    'DAILY',
    'admin',
    'CREATE TABLE test_db.test_user_order (\n  id BIGINT COMMENT ''主键ID'',\n  user_id BIGINT COMMENT ''用户ID'',\n  order_no STRING COMMENT ''订单号''\n)\nPARTITIONED BY (dt STRING)\nSTORED AS PARQUET;',
    'system'
);

-- 插入测试字段数据
INSERT INTO `metadata_field` (
    `table_id`, `field_order`, `field_name`, `field_comment`,
    `field_type`, `is_primary_key`, `is_nullable`, `sensitivity_level`,
    `create_by`
)
SELECT
    id, 1, 'id', '<p>主键ID</p>',
    'BIGINT', 1, 0, 'L1',
    'system'
FROM `metadata_table`
WHERE `table_name` = 'test_user_order'
LIMIT 1;

INSERT INTO `metadata_field` (
    `table_id`, `field_order`, `field_name`, `field_comment`,
    `field_type`, `is_primary_key`, `is_nullable`, `sensitivity_level`,
    `create_by`
)
SELECT
    id, 2, 'user_id', '<p>用户ID</p>',
    'BIGINT', 0, 0, 'L2',
    'system'
FROM `metadata_table`
WHERE `table_name` = 'test_user_order'
LIMIT 1;

INSERT INTO `metadata_field` (
    `table_id`, `field_order`, `field_name`, `field_comment`,
    `field_type`, `is_primary_key`, `is_nullable`, `sensitivity_level`,
    `create_by`
)
SELECT
    id, 3, 'order_no', '<p>订单号</p>',
    'STRING', 0, 0, 'L2',
    'system'
FROM `metadata_table`
WHERE `table_name` = 'test_user_order'
LIMIT 1;

-- ============================================================
-- 索引优化说明
-- ============================================================
-- 1. 主键索引: 所有表使用自增主键,InnoDB聚簇索引
-- 2. 唯一索引: 业务唯一键(database_name+table_name, flow_no等)
-- 3. 普通索引: 高频查询字段(data_source, warehouse_layer, theme, owner等)
-- 4. 联合索引: 遵循最左前缀原则(theme_first+theme_second)
-- 5. 外键索引: 自动创建,支持级联删除

-- ============================================================
-- 性能优化建议
-- ============================================================
-- 1. 定期执行ANALYZE TABLE更新统计信息
-- 2. 监控慢查询日志,优化慢SQL
-- 3. 考虑对table_comment等大字段使用TEXT类型,必要时考虑分表
-- 4. 历史数据定期归档(operation_history表)
-- 5. 使用读写分离减轻主库压力

-- ============================================================
-- 初始化完成
-- ============================================================
