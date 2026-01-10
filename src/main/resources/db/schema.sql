-- 创建数据库
CREATE DATABASE IF NOT EXISTS `metadata_mgmt` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE `metadata_mgmt`;

-- 元数据表
CREATE TABLE `metadata_table` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `table_name` varchar(128) NOT NULL COMMENT '表名',
  `table_comment` varchar(500) DEFAULT NULL COMMENT '表注释',
  `database_name` varchar(128) NOT NULL COMMENT '数据库名',
  `table_type` varchar(32) DEFAULT NULL COMMENT '表类型',
  `engine` varchar(32) DEFAULT NULL COMMENT '存储引擎',
  `charset` varchar(32) DEFAULT NULL COMMENT '字符集',
  `row_count` bigint(20) DEFAULT 0 COMMENT '行数',
  `data_size` decimal(10,2) DEFAULT 0.00 COMMENT '数据大小(MB)',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT 0 COMMENT '是否删除(0-未删除,1-已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_table_name` (`table_name`),
  KEY `idx_database_name` (`database_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='元数据表';

-- 插入示例数据
INSERT INTO `metadata_table` (`table_name`, `table_comment`, `database_name`, `table_type`, `engine`, `charset`, `row_count`, `data_size`, `create_by`)
VALUES
('user_info', '用户信息表', 'user_db', 'BASE TABLE', 'InnoDB', 'utf8mb4', 10000, 2.50, 'admin'),
('order_info', '订单信息表', 'order_db', 'BASE TABLE', 'InnoDB', 'utf8mb4', 50000, 15.30, 'admin'),
('product_info', '产品信息表', 'product_db', 'BASE TABLE', 'InnoDB', 'utf8mb4', 5000, 1.20, 'admin');
