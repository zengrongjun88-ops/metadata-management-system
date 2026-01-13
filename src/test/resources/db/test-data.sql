-- 测试数据初始化脚本
-- 注意: H2数据库会自动将表名转为大写，需要使用引号

-- 插入测试元数据表
INSERT INTO metadata_table (id, data_source, database_name, table_name, table_comment, warehouse_layer, theme_first, theme_second, sensitivity_level, importance_level, partition_type, update_frequency, owner, create_by, create_time, update_by, update_time, deleted)
VALUES
(1, 'Hive', 'test_db', 'user_info', '用户信息表', 'dwd', 'usr', 'mem', 'L1', 'P1', 'NONE', 'DAILY', 'admin', 'system', NOW(), 'system', NOW(), 0),
(2, 'Hive', 'test_db', 'order_info', '订单信息表', 'dwd', 'ord', 'flight', 'L2', 'P0', 'FULL', 'DAILY', 'admin', 'system', NOW(), 'system', NOW(), 0);

-- 插入测试字段
INSERT INTO metadata_field (id, table_id, field_order, field_name, field_comment, field_type, is_primary_key, is_nullable, sensitivity_level, create_by, create_time, update_by, update_time, deleted)
VALUES
(1, 1, 1, 'user_id', '用户ID', 'BIGINT', 1, 0, 'L1', 'system', NOW(), 'system', NOW(), 0),
(2, 1, 2, 'user_name', '用户姓名', 'STRING', 0, 1, 'L2', 'system', NOW(), 'system', NOW(), 0),
(3, 1, 3, 'mobile', '手机号', 'STRING', 0, 1, 'L3', 'system', NOW(), 'system', NOW(), 0),
(4, 2, 1, 'order_id', '订单ID', 'BIGINT', 1, 0, 'L1', 'system', NOW(), 'system', NOW(), 0),
(5, 2, 2, 'user_id', '用户ID', 'BIGINT', 0, 0, 'L1', 'system', NOW(), 'system', NOW(), 0);

-- 插入测试审批单
INSERT INTO approval_flow (id, flow_no, table_id, approval_type, status, submitter, submit_time, change_content, create_by, create_time, update_by, update_time, deleted)
VALUES
(1, 'APR-20260111-001001', 1, 'CREATE', 'DRAFT', 'admin', NOW(), '{"operation":"create_table"}', 'system', NOW(), 'system', NOW(), 0),
(2, 'APR-20260111-001002', 2, 'UPDATE', 'PENDING', 'admin', NOW(), '{"operation":"update_table"}', 'system', NOW(), 'system', NOW(), 0);

-- 插入测试操作历史
INSERT INTO operation_history (id, table_id, operation_type, operator, operation_time, operation_desc, create_by, create_time, deleted)
VALUES
(1, 1, 'CREATE', 'admin', NOW(), '创建用户信息表', 'system', NOW(), 0),
(2, 2, 'CREATE', 'admin', NOW(), '创建订单信息表', 'system', NOW(), 0);
