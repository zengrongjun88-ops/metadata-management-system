-- H2-compatible schema for testing

DROP TABLE IF EXISTS operation_history;
DROP TABLE IF EXISTS approval_flow;
DROP TABLE IF EXISTS metadata_field;
DROP TABLE IF EXISTS metadata_table;

CREATE TABLE metadata_table (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    table_name VARCHAR(200) NOT NULL,
    table_comment CLOB,
    database_name VARCHAR(200) NOT NULL,
    data_source VARCHAR(50) NOT NULL,
    hive_account VARCHAR(100),
    table_size BIGINT DEFAULT 0,
    warehouse_layer VARCHAR(50),
    theme_first VARCHAR(50),
    theme_second VARCHAR(50),
    sensitivity_level VARCHAR(20),
    importance_level VARCHAR(20),
    partition_type VARCHAR(50),
    partition_retention_days INT,
    update_frequency VARCHAR(50),
    owner VARCHAR(100),
    custom_tags VARCHAR(500),
    create_sql CLOB,
    create_by VARCHAR(100),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(100),
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uk_database_table ON metadata_table(database_name, table_name, deleted);
CREATE INDEX idx_data_source ON metadata_table(data_source);
CREATE INDEX idx_warehouse_layer ON metadata_table(warehouse_layer);
CREATE INDEX idx_owner ON metadata_table(owner);

CREATE TABLE metadata_field (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    table_id BIGINT NOT NULL,
    field_order INT NOT NULL,
    field_name VARCHAR(200) NOT NULL,
    field_comment CLOB,
    field_type VARCHAR(100) NOT NULL,
    is_primary_key TINYINT DEFAULT 0,
    is_nullable TINYINT DEFAULT 1,
    is_encrypted TINYINT DEFAULT 0,
    is_partition_key TINYINT DEFAULT 0,
    sensitivity_level VARCHAR(20),
    default_value VARCHAR(500),
    create_by VARCHAR(100),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(100),
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    FOREIGN KEY (table_id) REFERENCES metadata_table(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX uk_table_field ON metadata_field(table_id, field_name, deleted);
CREATE INDEX idx_table_id ON metadata_field(table_id);

CREATE TABLE approval_flow (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    flow_no VARCHAR(100) NOT NULL UNIQUE,
    table_id BIGINT NOT NULL,
    approval_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    submitter VARCHAR(100) NOT NULL,
    submit_time DATETIME NOT NULL,
    approver VARCHAR(100),
    approve_time DATETIME,
    approve_comment CLOB,
    change_content CLOB,
    create_by VARCHAR(100),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(100),
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    FOREIGN KEY (table_id) REFERENCES metadata_table(id) ON DELETE CASCADE
);

CREATE INDEX idx_af_table_id ON approval_flow(table_id);
CREATE INDEX idx_status ON approval_flow(status);

CREATE TABLE operation_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    table_id BIGINT NOT NULL,
    operation_type VARCHAR(50) NOT NULL,
    operator VARCHAR(100) NOT NULL,
    operation_time DATETIME NOT NULL,
    before_content CLOB,
    after_content CLOB,
    operation_desc VARCHAR(500),
    create_by VARCHAR(100),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    FOREIGN KEY (table_id) REFERENCES metadata_table(id) ON DELETE CASCADE
);

CREATE INDEX idx_oh_table_id ON operation_history(table_id);
CREATE INDEX idx_operation_type ON operation_history(operation_type);
