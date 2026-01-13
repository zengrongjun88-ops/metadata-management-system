# 数据仓库元数据管理系统架构设计文档

## 文档信息

| 项目名称 | 数据仓库元数据管理系统 |
|---------|---------------------|
| 文档版本 | 2.0.0 |
| 编写日期 | 2026-01-11 |
| 编写人   | System |
| 状态     | 详细设计阶段 |

---

## 目录

- [1. 系统概述](#1-系统概述)
- [2. 技术架构](#2-技术架构)
- [3. 应用分层架构](#3-应用分层架构)
- [4. 核心模块设计](#4-核心模块设计)
- [5. 数据模型设计](#5-数据模型设计)
- [6. 数据库设计](#6-数据库设计)
- [7. 缓存架构设计](#7-缓存架构设计)
- [8. 搜索引擎设计](#8-搜索引擎设计)
- [9. 数据血缘设计](#9-数据血缘设计)
- [10. 审批流程设计](#10-审批流程设计)
- [11. SQL生成器设计](#11-sql生成器设计)
- [12. 安全架构设计](#12-安全架构设计)
- [13. 性能优化策略](#13-性能优化策略)
- [14. 部署架构](#14-部署架构)
- [15. 扩展性设计](#15-扩展性设计)

---

## 1. 系统概述

### 1.1 系统目标

数据仓库元数据管理系统旨在提供统一的元数据管理平台，支持多数据源（Hive、Paimon、Iceberg、ClickHouse、BigQuery、StarRocks）的元数据管理，包括表的创建、修改、查询、审批、数据标准管理、数据血缘分析等功能。

### 1.2 核心价值

- **统一管理**: 集中管理多个数据源的元数据信息
- **标准化**: 建立统一的数据标准和规范
- **可追溯**: 完整的操作历史和数据血缘追踪
- **协同工作**: 支持审批流程，确保变更安全可控
- **高效搜索**: 基于Elasticsearch的快速检索能力

### 1.3 系统边界

**系统范围内**:
- 元数据表和字段的管理
- 数据标准和规范的维护
- 审批流程的管理
- 数据血缘的分析和展示
- 操作历史的记录和查询

**系统范围外**:
- 实际数据仓库的执行引擎
- 数据的ETL处理
- 数据质量监控
- 实时数据同步

---

## 2. 技术架构

### 2.1 整体技术栈

```
┌─────────────────────────────────────────────────────────────┐
│                        前端层 (Ant Design)                   │
└─────────────────────────────────────────────────────────────┘
                              ↓ HTTP/HTTPS
┌─────────────────────────────────────────────────────────────┐
│                     应用服务层 (Spring Boot)                  │
├─────────────────────────────────────────────────────────────┤
│  Controller层  │  Service层  │  Mapper层  │  Integration层  │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌──────────────┬──────────────┬──────────────┬───────────────┐
│    MySQL     │    Redis     │ Elasticsearch│ Nebula Graph  │
│  (元数据存储)  │   (缓存层)    │   (搜索引擎)  │  (血缘图谱)    │
└──────────────┴──────────────┴──────────────┴───────────────┘
```

### 2.2 技术选型说明

| 技术组件 | 版本 | 用途 | 选型理由 |
|---------|------|------|---------|
| Spring Boot | 2.7.18 | 应用框架 | 成熟稳定，生态完善 |
| MyBatis Plus | 3.5.5 | ORM框架 | 简化CRUD，支持代码生成 |
| MySQL | 8.0+ | 关系数据库 | 高可靠性，事务支持完善 |
| Redis | 6.x+ | 缓存 | 高性能，支持多种数据结构 |
| Elasticsearch | 7.x+ | 搜索引擎 | 全文检索，高性能搜索 |
| Nebula Graph | 3.x+ | 图数据库 | 专业图存储，血缘分析 |
| Knife4j | 4.3.0 | 接口文档 | Swagger增强，UI友好 |
| Druid | 1.2.20 | 连接池 | 监控完善，防SQL注入 |

### 2.3 技术架构分层

```
┌─────────────────────────────────────────────────────────────┐
│                         接入层                                │
│              Nginx / Gateway / Load Balancer                 │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                         应用层                                │
│  ┌──────────────┬──────────────┬──────────────────────────┐ │
│  │   Web层      │   Service层   │      Integration层       │ │
│  │  Controller  │   业务逻辑     │   外部系统集成            │ │
│  └──────────────┴──────────────┴──────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                         数据层                                │
│  ┌──────────────┬──────────────┬──────────────────────────┐ │
│  │  Mapper层    │   Cache层     │      Search层            │ │
│  │  MyBatis Plus│    Redis      │   Elasticsearch          │ │
│  └──────────────┴──────────────┴──────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                        基础设施层                              │
│      MySQL   │   Redis   │   ES   │   Nebula   │   MQ       │
└─────────────────────────────────────────────────────────────┘
```

---

## 3. 应用分层架构

### 3.1 分层设计原则

严格遵循三层架构模式，确保各层职责单一、依赖单向：

```
Controller层（展示层）
    ↓ 只能调用
Service层（业务层）
    ↓ 只能调用
Mapper层（数据访问层）
```

### 3.2 各层职责定义

#### 3.2.1 Controller层

**职责**:
- 接收HTTP请求，参数校验
- 调用Service层业务逻辑
- 封装响应结果，返回给前端
- 异常转换和处理

**规范**:
- 使用`@RestController`注解
- 使用`@Valid`进行参数校验
- 不包含业务逻辑，只做参数转换和结果封装
- 统一使用`Result<T>`包装响应
- 添加Swagger注解，完善接口文档

**示例**:
```java
@RestController
@RequestMapping("/api/metadata/tables")
@Api(tags = "元数据表管理")
public class MetadataTableController {

    private final IMetadataTableService metadataTableService;

    @GetMapping("/{id}")
    @ApiOperation("根据ID查询表信息")
    public Result<MetadataTableVO> getById(@PathVariable Long id) {
        return Result.success(metadataTableService.getTableById(id));
    }
}
```

#### 3.2.2 Service层

**职责**:
- 实现核心业务逻辑
- 事务管理
- 调用Mapper层进行数据访问
- 调用外部服务（ES、Nebula Graph等）
- 缓存管理

**规范**:
- 接口定义在`service`包，实现在`service.impl`包
- 使用`@Service`注解
- 使用`@Transactional`管理事务
- 复杂业务逻辑需要添加详细注释
- 使用构造器注入依赖

**示例**:
```java
public interface IMetadataTableService {
    /**
     * 根据ID查询表信息
     *
     * @param id 表ID
     * @return 表视图对象
     */
    MetadataTableVO getTableById(Long id);
}

@Service
public class MetadataTableServiceImpl implements IMetadataTableService {

    private final MetadataTableMapper metadataTableMapper;
    private final IMetadataFieldService fieldService;
    private final CacheService cacheService;

    @Override
    public MetadataTableVO getTableById(Long id) {
        // 业务逻辑实现
    }
}
```

#### 3.2.3 Mapper层

**职责**:
- 数据库CRUD操作
- SQL映射
- 结果集映射

**规范**:
- 继承`BaseMapper<T>`
- 使用`@Mapper`注解
- 复杂SQL使用XML映射文件
- 简单查询使用MyBatis Plus的Wrapper

**示例**:
```java
@Mapper
public interface MetadataTableMapper extends BaseMapper<MetadataTable> {

    /**
     * 根据数据库名和表名查询
     *
     * @param databaseName 数据库名
     * @param tableName 表名
     * @return 元数据表
     */
    MetadataTable selectByDatabaseAndTableName(
        @Param("databaseName") String databaseName,
        @Param("tableName") String tableName
    );
}
```

### 3.3 包结构设计

```
com.datawarehouse.metadata
├── controller/                    # 控制器层
│   ├── MetadataTableController
│   ├── MetadataFieldController
│   ├── DataStandardController
│   ├── WarehouseManageController
│   └── ApprovalController
│
├── service/                       # 服务接口
│   ├── IMetadataTableService
│   ├── IMetadataFieldService
│   ├── IDataStandardService
│   ├── IWarehouseManageService
│   ├── IApprovalService
│   ├── ISearchService
│   └── ILineageService
│   └── impl/                      # 服务实现
│       ├── MetadataTableServiceImpl
│       └── ...
│
├── mapper/                        # 数据访问层
│   ├── MetadataTableMapper
│   ├── MetadataFieldMapper
│   ├── DataStandardMapper
│   └── ...
│
├── entity/                        # 实体类（对应数据库表）
│   ├── MetadataTable
│   ├── MetadataField
│   ├── DataStandard
│   └── ...
│
├── dto/                          # 数据传输对象
│   ├── request/                  # 请求DTO
│   │   ├── TableCreateRequest
│   │   ├── TableUpdateRequest
│   │   └── TableSearchRequest
│   └── response/                 # 响应DTO（已废弃，使用VO）
│
├── vo/                           # 视图对象（返回给前端）
│   ├── MetadataTableVO
│   ├── MetadataFieldVO
│   └── LineageGraphVO
│
├── enums/                        # 枚举类
│   ├── DataSourceTypeEnum       # 数据源类型
│   ├── SensitivityLevelEnum     # 敏感等级
│   ├── ImportanceLevelEnum      # 重要等级
│   ├── WarehouseLayerEnum       # 数仓分层
│   └── ApprovalStatusEnum       # 审批状态
│
├── config/                       # 配置类
│   ├── SwaggerConfig
│   ├── MybatisPlusConfig
│   ├── RedisConfig
│   ├── ElasticsearchConfig
│   └── NebulaGraphConfig
│
├── common/                       # 通用类
│   ├── Result                   # 统一响应结果
│   ├── PageResult               # 分页结果
│   ├── Constants                # 常量定义
│   └── BaseEntity               # 基础实体类
│
├── exception/                    # 异常类
│   ├── BusinessException        # 业务异常
│   ├── GlobalExceptionHandler   # 全局异常处理器
│   └── ErrorCode                # 错误码定义
│
├── util/                         # 工具类
│   ├── SqlGeneratorUtil         # SQL生成工具
│   ├── ValidationUtil           # 校验工具
│   └── CacheKeyUtil             # 缓存Key工具
│
├── integration/                  # 外部系统集成
│   ├── elasticsearch            # ES集成
│   │   ├── ElasticsearchClient
│   │   └── SearchService
│   ├── nebula                   # 图数据库集成
│   │   ├── NebulaGraphClient
│   │   └── LineageService
│   └── engine                   # 数据仓库引擎集成
│       ├── HiveEngineClient
│       ├── BigQueryEngineClient
│       └── EngineClientFactory
│
└── aspect/                       # 切面
    ├── LogAspect                # 日志切面
    ├── CacheAspect              # 缓存切面
    └── PermissionAspect         # 权限切面
```

---

## 4. 核心模块设计

### 4.1 模块划分

系统按功能领域划分为以下核心模块：

```
┌─────────────────────────────────────────────────────────────┐
│                      元数据管理系统                            │
├─────────────────────────────────────────────────────────────┤
│  ┌──────────────────┐  ┌──────────────────┐                 │
│  │  元数据表管理模块   │  │  元数据字段管理模块 │                 │
│  └──────────────────┘  └──────────────────┘                 │
│  ┌──────────────────┐  ┌──────────────────┐                 │
│  │  数据标准管理模块   │  │  数仓管理模块      │                 │
│  └──────────────────┘  └──────────────────┘                 │
│  ┌──────────────────┐  ┌──────────────────┐                 │
│  │  审批流程模块      │  │  操作历史模块      │                 │
│  └──────────────────┘  └──────────────────┘                 │
│  ┌──────────────────┐  ┌──────────────────┐                 │
│  │  搜索服务模块      │  │  数据血缘模块      │                 │
│  └──────────────────┘  └──────────────────┘                 │
│  ┌──────────────────┐  ┌──────────────────┐                 │
│  │  SQL生成器模块     │  │  引擎集成模块      │                 │
│  └──────────────────┘  └──────────────────┘                 │
└─────────────────────────────────────────────────────────────┘
```

### 4.2 元数据表管理模块

**功能**:
- 表的创建、修改、删除（软删除）
- 表信息的查询和展示
- 表的搜索（基于ES）
- 表的统计信息维护

**核心类**:
- `MetadataTableController`: 表管理控制器
- `IMetadataTableService`: 表管理服务接口
- `MetadataTableServiceImpl`: 表管理服务实现
- `MetadataTableMapper`: 表数据访问
- `MetadataTable`: 表实体
- `MetadataTableVO`: 表视图对象

**关键业务流程**:

```
表创建流程:
用户填写表信息 → 参数校验 → 生成建表SQL →
语法校验（调用引擎） → 保存元数据 → 同步到ES →
返回结果

表修改流程:
用户提交修改 → 创建审批单 → 审批流转 →
审批通过 → 更新元数据 → 同步ES → 记录历史
```

### 4.3 审批流程模块

**功能**:
- 审批单创建
- 审批流转
- 审批历史查询
- 审批通知

**核心类**:
- `ApprovalController`
- `IApprovalService`
- `ApprovalFlowEngine`: 审批流程引擎

**审批流程**:

```
┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐
│ 提交审批  │ → │ 待审批   │ → │  已通过  │ → │  已发布  │
└─────────┘    └─────────┘    └─────────┘    └─────────┘
                     │
                     ↓
               ┌─────────┐
               │  已拒绝  │
               └─────────┘
```

---

## 5. 数据模型设计

### 5.1 核心实体关系图

```
┌─────────────────┐
│  MetadataTable  │
│  元数据表        │
├─────────────────┤
│ id (PK)         │
│ table_name      │
│ database_name   │
│ data_source     │
│ owner           │
│ ...             │
└─────────────────┘
         │ 1
         │
         │ N
         ↓
┌─────────────────┐
│ MetadataField   │
│ 元数据字段       │
├─────────────────┤
│ id (PK)         │
│ table_id (FK)   │
│ field_name      │
│ field_type      │
│ ...             │
└─────────────────┘

┌─────────────────┐
│  ApprovalFlow   │
│  审批流程        │
├─────────────────┤
│ id (PK)         │
│ table_id (FK)   │
│ approval_type   │
│ status          │
│ ...             │
└─────────────────┘

┌─────────────────┐
│ OperationHistory│
│ 操作历史         │
├─────────────────┤
│ id (PK)         │
│ table_id (FK)   │
│ operation_type  │
│ before_content  │
│ after_content   │
│ ...             │
└─────────────────┘
```

---

## 6. 数据库设计

### 6.1 数据库设计原则

- **命名规范**: 表名和字段名使用小写下划线分隔
- **主键策略**: 统一使用`id`作为主键，自增长
- **时间字段**: 使用`DATETIME`类型，统一包含`create_time`、`update_time`
- **软删除**: 使用`deleted`字段（0未删除，1已删除）
- **审计字段**: 包含`create_by`、`update_by`记录操作人

### 6.2 核心表设计

#### 6.2.1 metadata_table (元数据表)

```sql
CREATE TABLE `metadata_table` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `table_name` VARCHAR(200) NOT NULL COMMENT '表名',
  `database_name` VARCHAR(200) NOT NULL COMMENT '数据库名',
  `data_source` VARCHAR(50) NOT NULL COMMENT '数据源类型',
  `table_comment` TEXT COMMENT '表描述',
  `hive_account` VARCHAR(100) COMMENT 'Hive账号/租户',
  `table_size` BIGINT DEFAULT 0 COMMENT '表大小(字节)',
  `warehouse_layer` VARCHAR(50) COMMENT '数仓分层',
  `theme_first` VARCHAR(50) COMMENT '一级主题',
  `theme_second` VARCHAR(50) COMMENT '二级主题',
  `sensitivity_level` VARCHAR(20) COMMENT '敏感等级(L1/L2/L3/L4)',
  `importance_level` VARCHAR(20) COMMENT '重要等级(P0/P1/P2/P3)',
  `partition_type` VARCHAR(50) COMMENT '分区类型',
  `partition_retention_days` INT COMMENT '分区保留天数',
  `update_frequency` VARCHAR(50) COMMENT '更新频率',
  `owner` VARCHAR(100) COMMENT '责任人',
  `custom_tags` VARCHAR(500) COMMENT '自定义标签',
  `create_sql` TEXT COMMENT '建表SQL',
  `create_by` VARCHAR(100) COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(100) COMMENT '更新人',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '是否删除(0-未删除,1-已删除)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_database_table` (`database_name`, `table_name`, `deleted`),
  KEY `idx_data_source` (`data_source`),
  KEY `idx_warehouse_layer` (`warehouse_layer`),
  KEY `idx_theme` (`theme_first`, `theme_second`),
  KEY `idx_owner` (`owner`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='元数据表信息';
```

#### 6.2.2 metadata_field (元数据字段)

```sql
CREATE TABLE `metadata_field` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `table_id` BIGINT NOT NULL COMMENT '表ID',
  `field_name` VARCHAR(200) NOT NULL COMMENT '字段名',
  `field_comment` TEXT COMMENT '字段描述',
  `field_type` VARCHAR(100) NOT NULL COMMENT '字段类型',
  `field_order` INT NOT NULL COMMENT '字段顺序',
  `is_primary_key` TINYINT DEFAULT 0 COMMENT '是否主键(0-否,1-是)',
  `is_nullable` TINYINT DEFAULT 1 COMMENT '是否可为空(0-否,1-是)',
  `is_encrypted` TINYINT DEFAULT 0 COMMENT '是否加密(0-否,1-是)',
  `sensitivity_level` VARCHAR(20) COMMENT '敏感等级',
  `default_value` VARCHAR(500) COMMENT '默认值',
  `create_by` VARCHAR(100) COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(100) COMMENT '更新人',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_table_id` (`table_id`),
  KEY `idx_field_name` (`field_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='元数据字段信息';
```

#### 6.2.3 approval_flow (审批流程)

```sql
CREATE TABLE `approval_flow` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `flow_no` VARCHAR(100) NOT NULL COMMENT '审批单号',
  `table_id` BIGINT NOT NULL COMMENT '表ID',
  `approval_type` VARCHAR(50) NOT NULL COMMENT '审批类型(CREATE/UPDATE/DELETE)',
  `status` VARCHAR(50) NOT NULL COMMENT '审批状态(PENDING/APPROVED/REJECTED/PUBLISHED)',
  `submitter` VARCHAR(100) NOT NULL COMMENT '提交人',
  `submit_time` DATETIME NOT NULL COMMENT '提交时间',
  `approver` VARCHAR(100) COMMENT '审批人',
  `approve_time` DATETIME COMMENT '审批时间',
  `approve_comment` TEXT COMMENT '审批意见',
  `change_content` TEXT COMMENT '变更内容(JSON)',
  `create_by` VARCHAR(100) COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(100) COMMENT '更新人',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_flow_no` (`flow_no`),
  KEY `idx_table_id` (`table_id`),
  KEY `idx_status` (`status`),
  KEY `idx_submitter` (`submitter`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批流程';
```

#### 6.2.4 operation_history (操作历史)

```sql
CREATE TABLE `operation_history` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `table_id` BIGINT NOT NULL COMMENT '表ID',
  `operation_type` VARCHAR(50) NOT NULL COMMENT '操作类型(CREATE/UPDATE/DELETE)',
  `operator` VARCHAR(100) NOT NULL COMMENT '操作人',
  `operation_time` DATETIME NOT NULL COMMENT '操作时间',
  `before_content` TEXT COMMENT '变更前内容(JSON)',
  `after_content` TEXT COMMENT '变更后内容(JSON)',
  `operation_desc` VARCHAR(500) COMMENT '操作描述',
  `create_by` VARCHAR(100) COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_table_id` (`table_id`),
  KEY `idx_operation_time` (`operation_time`),
  KEY `idx_operator` (`operator`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作历史';
```

### 6.3 索引设计策略

- **主键索引**: 所有表都有自增主键
- **唯一索引**: 业务唯一键（database_name + table_name）
- **普通索引**: 高频查询字段（data_source、warehouse_layer、owner等）
- **联合索引**: 遵循最左前缀原则（theme_first + theme_second）
- **时间索引**: 用于历史数据查询

---

## 7. 缓存架构设计

### 7.1 缓存策略

采用多级缓存策略提升系统性能：

```
┌─────────────────────────────────────────────────────────────┐
│                         应用层                                │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                      本地缓存 (Caffeine)                       │
│              热点数据、配置数据、查询结果                         │
└─────────────────────────────────────────────────────────────┘
                              ↓ Cache Miss
┌─────────────────────────────────────────────────────────────┐
│                    分布式缓存 (Redis)                          │
│          元数据表信息、字段信息、数据标准、搜索结果                │
└─────────────────────────────────────────────────────────────┘
                              ↓ Cache Miss
┌─────────────────────────────────────────────────────────────┐
│                       数据库 (MySQL)                           │
└─────────────────────────────────────────────────────────────┘
```

### 7.2 缓存Key设计

采用统一的Key命名规范：

```
格式: {业务模块}:{对象类型}:{唯一标识}:{版本}

示例:
- metadata:table:123:v1          # 表ID为123的元数据
- metadata:field:table:123:v1    # 表ID为123的所有字段
- metadata:search:hash123:v1     # 搜索结果缓存
- config:layer:all:v1            # 所有数仓分层配置
- config:theme:all:v1            # 所有主题配置
```

### 7.3 缓存更新策略

**更新策略**:
- **Cache Aside模式**: 查询时先查缓存，未命中查DB后回写缓存
- **Write Through模式**: 更新时同时更新DB和缓存
- **删除策略**: 更新数据时直接删除缓存，下次查询时重建

**过期时间设置**:
- 元数据表信息: 1小时
- 字段信息: 1小时
- 配置数据: 12小时
- 搜索结果: 10分钟

### 7.4 缓存实现

```java
@Service
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final Cache<String, Object> localCache;

    /**
     * 获取缓存
     */
    public <T> T get(String key, Class<T> clazz) {
        // 1. 先查本地缓存
        T value = (T) localCache.getIfPresent(key);
        if (value != null) {
            return value;
        }

        // 2. 查Redis缓存
        value = (T) redisTemplate.opsForValue().get(key);
        if (value != null) {
            // 写入本地缓存
            localCache.put(key, value);
            return value;
        }

        return null;
    }

    /**
     * 设置缓存
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        // 写入Redis
        redisTemplate.opsForValue().set(key, value, timeout, unit);
        // 写入本地缓存
        localCache.put(key, value);
    }

    /**
     * 删除缓存
     */
    public void delete(String key) {
        redisTemplate.delete(key);
        localCache.invalidate(key);
    }
}
```

### 7.5 缓存穿透、击穿、雪崩防护

**缓存穿透防护**:
- 使用布隆过滤器预判数据是否存在
- 空值缓存，TTL设置较短（5分钟）

**缓存击穿防护**:
- 热点数据永不过期
- 使用互斥锁（分布式锁）防止并发查询DB

**缓存雪崩防护**:
- 缓存过期时间加随机值
- 使用本地缓存作为兜底

```java
public MetadataTableVO getTableById(Long id) {
    String key = CacheKeyUtil.buildTableKey(id);

    // 查缓存
    MetadataTableVO vo = cacheService.get(key, MetadataTableVO.class);
    if (vo != null) {
        return vo;
    }

    // 分布式锁防止缓存击穿
    String lockKey = "lock:" + key;
    try {
        if (redissonClient.getLock(lockKey).tryLock(5, TimeUnit.SECONDS)) {
            // 双重检查
            vo = cacheService.get(key, MetadataTableVO.class);
            if (vo != null) {
                return vo;
            }

            // 查询数据库
            MetadataTable table = metadataTableMapper.selectById(id);
            if (table == null) {
                // 空值缓存，防止缓存穿透
                cacheService.set(key, new MetadataTableVO(), 5, TimeUnit.MINUTES);
                throw new BusinessException("表不存在");
            }

            vo = convertToVO(table);
            // 缓存时间加随机值，防止雪崩
            long ttl = 3600 + RandomUtil.randomInt(0, 600);
            cacheService.set(key, vo, ttl, TimeUnit.SECONDS);

            return vo;
        }
    } finally {
        redissonClient.getLock(lockKey).unlock();
    }

    throw new BusinessException("系统繁忙");
}
```

---

## 8. 搜索引擎设计

### 8.1 Elasticsearch架构

```
┌─────────────────────────────────────────────────────────────┐
│                       应用服务层                               │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                  ElasticsearchClient                          │
│              搜索服务、索引管理、数据同步                         │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│               Elasticsearch Cluster                           │
│  ┌───────────┐  ┌───────────┐  ┌───────────┐                 │
│  │  Master   │  │   Data    │  │   Data    │                 │
│  │   Node    │  │   Node    │  │   Node    │                 │
│  └───────────┘  └───────────┘  └───────────┘                 │
└─────────────────────────────────────────────────────────────┘
```

### 8.2 索引设计

#### 8.2.1 metadata_table索引

```json
{
  "settings": {
    "number_of_shards": 3,
    "number_of_replicas": 1,
    "analysis": {
      "analyzer": {
        "ik_smart_pinyin": {
          "type": "custom",
          "tokenizer": "ik_smart",
          "filter": ["pinyin_filter"]
        }
      },
      "filter": {
        "pinyin_filter": {
          "type": "pinyin",
          "keep_first_letter": true,
          "keep_separate_first_letter": false,
          "keep_full_pinyin": true,
          "keep_original": true,
          "limit_first_letter_length": 16
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "id": {"type": "long"},
      "table_name": {
        "type": "text",
        "analyzer": "ik_smart_pinyin",
        "fields": {
          "keyword": {"type": "keyword"}
        }
      },
      "database_name": {
        "type": "text",
        "analyzer": "ik_smart",
        "fields": {
          "keyword": {"type": "keyword"}
        }
      },
      "table_comment": {
        "type": "text",
        "analyzer": "ik_smart"
      },
      "data_source": {"type": "keyword"},
      "hive_account": {"type": "keyword"},
      "table_size": {"type": "long"},
      "warehouse_layer": {"type": "keyword"},
      "theme_first": {"type": "keyword"},
      "theme_second": {"type": "keyword"},
      "sensitivity_level": {"type": "keyword"},
      "importance_level": {"type": "keyword"},
      "owner": {"type": "keyword"},
      "create_time": {"type": "date"},
      "fields": {
        "type": "nested",
        "properties": {
          "field_name": {
            "type": "text",
            "analyzer": "ik_smart"
          },
          "field_comment": {
            "type": "text",
            "analyzer": "ik_smart"
          },
          "field_type": {"type": "keyword"},
          "sensitivity_level": {"type": "keyword"}
        }
      }
    }
  }
}
```

### 8.3 搜索功能实现

支持表名、字段名、库名、描述的模糊搜索：

```java
public PageResult<MetadataTableVO> search(TableSearchRequest request) {
    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

    // 关键词搜索（表名、库名、字段名）
    if (StringUtils.isNotBlank(request.getKeyword())) {
        boolQuery.should(QueryBuilders.matchQuery("table_name", request.getKeyword()).boost(3.0f));
        boolQuery.should(QueryBuilders.matchQuery("database_name", request.getKeyword()).boost(2.0f));
        boolQuery.should(QueryBuilders.matchQuery("table_comment", request.getKeyword()).boost(1.0f));
        boolQuery.should(QueryBuilders.nestedQuery("fields",
            QueryBuilders.multiMatchQuery(request.getKeyword(),
                "fields.field_name", "fields.field_comment"),
            ScoreMode.Max).boost(2.0f));
        boolQuery.minimumShouldMatch(1);
    }

    // 筛选条件
    if (StringUtils.isNotBlank(request.getDataSource())) {
        boolQuery.filter(QueryBuilders.termQuery("data_source", request.getDataSource()));
    }
    if (StringUtils.isNotBlank(request.getWarehouseLayer())) {
        boolQuery.filter(QueryBuilders.termQuery("warehouse_layer", request.getWarehouseLayer()));
    }
    if (StringUtils.isNotBlank(request.getThemeFirst())) {
        boolQuery.filter(QueryBuilders.termQuery("theme_first", request.getThemeFirst()));
    }

    // 构建查询请求
    SearchRequest searchRequest = new SearchRequest("metadata_table");
    SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
    sourceBuilder.query(boolQuery);
    sourceBuilder.from((request.getPageNum() - 1) * request.getPageSize());
    sourceBuilder.size(request.getPageSize());
    sourceBuilder.sort("_score", SortOrder.DESC);
    sourceBuilder.sort("create_time", SortOrder.DESC);

    // 高亮设置
    HighlightBuilder highlightBuilder = new HighlightBuilder();
    highlightBuilder.field("table_name");
    highlightBuilder.field("table_comment");
    highlightBuilder.preTags("<em>");
    highlightBuilder.postTags("</em>");
    sourceBuilder.highlighter(highlightBuilder);

    searchRequest.source(sourceBuilder);

    // 执行搜索
    SearchResponse response = elasticsearchClient.search(searchRequest);

    // 解析结果
    return parseSearchResponse(response);
}
```

---

## 9. 数据血缘设计

### 9.1 血缘图谱架构

基于Nebula Graph构建表级和字段级的数据血缘关系：

```
┌─────────────────────────────────────────────────────────────┐
│                      应用服务层                                │
│                  LineageService                               │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                  NebulaGraphClient                            │
│              血缘关系查询、血缘图构建                            │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                    Nebula Graph Cluster                       │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐          │
│  │ Graphd  │  │ Storaged│  │ Storaged│  │  Metad  │          │
│  └─────────┘  └─────────┘  └─────────┘  └─────────┘          │
└─────────────────────────────────────────────────────────────┘
```

### 9.2 图模型设计

#### 9.2.1 Schema定义

```sql
-- 创建图空间
CREATE SPACE metadata_lineage (
  partition_num = 10,
  replica_factor = 1,
  vid_type = FIXED_STRING(50)
);

USE metadata_lineage;

-- 定义顶点类型: 表
CREATE TAG table (
  table_name string,
  database_name string,
  data_source string,
  owner string,
  create_time datetime
);

-- 定义顶点类型: 字段
CREATE TAG field (
  field_name string,
  field_type string,
  table_id int64
);

-- 定义边类型: 表依赖关系
CREATE EDGE derives_from (
  dependency_type string,  -- DIRECT/INDIRECT
  sql_logic string,         -- SQL逻辑
  create_time datetime
);

-- 定义边类型: 字段血缘关系
CREATE EDGE field_lineage (
  transform_logic string,   -- 转换逻辑
  create_time datetime
);

-- 定义边类型: 表包含字段
CREATE EDGE has_field (
  field_order int
);
```

### 9.3 血缘查询

#### 9.3.1 上游血缘查询

```java
/**
 * 查询表的上游血缘（N层）
 */
public LineageGraphVO queryUpstreamLineage(Long tableId, int depth) {
    String nGql = String.format(
        "GO %d STEPS FROM '%s' OVER derives_from REVERSELY " +
        "YIELD derives_from._src AS source, derives_from._dst AS target, " +
        "derives_from.dependency_type AS type",
        depth, tableId
    );

    ResultSet resultSet = nebulaGraphClient.execute(nGql);
    return buildLineageGraph(resultSet);
}
```

#### 9.3.2 下游血缘查询

```java
/**
 * 查询表的下游血缘（N层）
 */
public LineageGraphVO queryDownstreamLineage(Long tableId, int depth) {
    String nGql = String.format(
        "GO %d STEPS FROM '%s' OVER derives_from " +
        "YIELD derives_from._src AS source, derives_from._dst AS target, " +
        "derives_from.dependency_type AS type",
        depth, tableId
    );

    ResultSet resultSet = nebulaGraphClient.execute(nGql);
    return buildLineageGraph(resultSet);
}
```

---

## 10. 审批流程设计

### 10.1 审批流程架构

```
┌─────────────────────────────────────────────────────────────┐
│                      用户操作                                  │
│              提交表修改/删除申请                                │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                  ApprovalFlowEngine                           │
│         创建审批单、流程流转、状态管理                            │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                   审批流程状态机                                │
│  PENDING → APPROVED → PUBLISHED                              │
│               ↓                                               │
│           REJECTED                                            │
└─────────────────────────────────────────────────────────────┘
```

### 10.2 审批流程实现

```java
@Service
public class ApprovalFlowEngine {

    private final ApprovalFlowMapper approvalFlowMapper;
    private final MetadataTableService metadataTableService;

    /**
     * 创建审批单
     */
    @Transactional(rollbackFor = Exception.class)
    public ApprovalFlow createApproval(TableUpdateRequest request, String submitter) {
        // 1. 创建审批单
        ApprovalFlow flow = new ApprovalFlow();
        flow.setFlowNo(generateFlowNo());
        flow.setTableId(request.getTableId());
        flow.setApprovalType("UPDATE");
        flow.setStatus(ApprovalStatusEnum.PENDING.getCode());
        flow.setSubmitter(submitter);
        flow.setSubmitTime(LocalDateTime.now());
        flow.setChangeContent(JSON.toJSONString(request));

        approvalFlowMapper.insert(flow);

        // 2. 发送通知给审批人
        notifyApprover(flow);

        return flow;
    }

    /**
     * 审批通过
     */
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long flowId, String approver, String comment) {
        // 1. 更新审批单状态
        ApprovalFlow flow = approvalFlowMapper.selectById(flowId);
        flow.setStatus(ApprovalStatusEnum.APPROVED.getCode());
        flow.setApprover(approver);
        flow.setApproveTime(LocalDateTime.now());
        flow.setApproveComment(comment);
        approvalFlowMapper.updateById(flow);

        // 2. 通知提交人
        notifySubmitter(flow);
    }

    /**
     * 发布变更
     */
    @Transactional(rollbackFor = Exception.class)
    public void publish(Long flowId, String operator) {
        ApprovalFlow flow = approvalFlowMapper.selectById(flowId);

        // 1. 执行变更
        TableUpdateRequest request = JSON.parseObject(flow.getChangeContent(),
            TableUpdateRequest.class);
        metadataTableService.updateTable(request);

        // 2. 更新审批单状态
        flow.setStatus(ApprovalStatusEnum.PUBLISHED.getCode());
        approvalFlowMapper.updateById(flow);

        // 3. 同步到ES
        elasticsearchSyncService.syncTableToEs(flow.getTableId());
    }
}
```

---

## 11. SQL生成器设计

### 11.1 SQL生成器架构

使用策略模式支持多种数据源的SQL生成：

```
┌─────────────────────────────────────────────────────────────┐
│                   TableCreateRequest                          │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                  SqlGeneratorFactory                          │
│              根据数据源类型选择生成器                            │
└─────────────────────────────────────────────────────────────┘
                              ↓
        ┌──────────────┬──────────────┬──────────────┐
        ↓              ↓              ↓              ↓
┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│HiveSqlGen    │ │BigQuerySqlGen│ │ClickHouseSql │ │IcebergSqlGen │
└──────────────┘ └──────────────┘ └──────────────┘ └──────────────┘
```

### 11.2 SQL生成器接口

```java
public interface SqlGenerator {
    String generateCreateTableSql(TableCreateRequest request);
    String generateAlterTableSql(TableUpdateRequest request);
    String generateDropTableSql(String databaseName, String tableName);
}
```

### 11.3 Hive SQL生成器实现

```java
@Component
public class HiveSqlGenerator implements SqlGenerator {

    @Override
    public String generateCreateTableSql(TableCreateRequest request) {
        StringBuilder sql = new StringBuilder();

        // CREATE TABLE
        sql.append("CREATE TABLE IF NOT EXISTS ")
            .append(request.getDatabaseName()).append(".").append(request.getTableName())
            .append(" (\n");

        // 字段定义
        List<FieldDefinition> fields = request.getFields();
        for (int i = 0; i < fields.size(); i++) {
            FieldDefinition field = fields.get(i);
            sql.append("  ").append(field.getFieldName())
                .append(" ").append(field.getFieldType());

            if (StringUtils.isNotBlank(field.getFieldComment())) {
                sql.append(" COMMENT '").append(field.getFieldComment()).append("'");
            }

            if (i < fields.size() - 1) {
                sql.append(",");
            }
            sql.append("\n");
        }
        sql.append(")\n");

        // 表注释
        if (StringUtils.isNotBlank(request.getTableComment())) {
            sql.append("COMMENT '").append(request.getTableComment()).append("'\n");
        }

        // 分区字段
        if (CollUtil.isNotEmpty(request.getPartitionFields())) {
            sql.append("PARTITIONED BY (\n");
            // ... 分区字段逻辑
            sql.append(")\n");
        }

        // 存储格式
        sql.append("STORED AS PARQUET\n");

        return sql.toString();
    }
}
```

---

## 12. 安全架构设计

### 12.1 安全防护体系

```
┌────────────────────────────────────────────────────────────┐
│                        安全防护层                              │
├────────────────────────────────────────────────────────────┤
│  认证   │  授权   │  SQL注入   │   XSS    │  敏感数据   │      │
│  层     │  层     │  防护      │   防护    │  加密       │      │
└────────────────────────────────────────────────────────────┘
```

### 12.2 SQL注入防护

**防护措施**:
1. 使用MyBatis Plus的参数化查询，禁止拼接SQL
2. 使用`#{}`而非`${}`传递参数
3. Druid SQL防火墙拦截危险SQL

```java
// ❌ 错误示例：SQL拼接
String sql = "SELECT * FROM metadata_table WHERE table_name = '" + tableName + "'";

// ✅ 正确示例：参数化查询
QueryWrapper<MetadataTable> wrapper = new QueryWrapper<>();
wrapper.eq("table_name", tableName);
metadataTableMapper.selectList(wrapper);
```

---

## 13. 性能优化策略

### 13.1 数据库优化

**索引优化**:
- 高频查询字段添加索引
- 联合索引遵循最左前缀原则
- 避免索引失效（函数、隐式转换、前缀模糊）

**查询优化**:
- 避免SELECT *，只查询需要的字段
- 大数据量查询必须分页
- 避免N+1查询，使用批量查询

### 13.2 缓存优化

**缓存策略**:
- 热点数据使用本地缓存+Redis二级缓存
- 缓存预热：系统启动时加载常用配置
- 缓存更新：主动更新，避免大量缓存同时失效

### 13.3 异步处理

对于耗时操作，使用异步处理提升响应速度：

```java
@Service
public class AsyncService {

    @Async("taskExecutor")
    public CompletableFuture<Void> syncToElasticsearch(Long tableId) {
        elasticsearchSyncService.syncTableToEs(tableId);
        return CompletableFuture.completedFuture(null);
    }
}
```

---

## 14. 部署架构

### 14.1 部署架构图

```
                    ┌─────────────────┐
                    │   Nginx / LB    │
                    └─────────────────┘
                            │
              ┌─────────────┼─────────────┐
              ↓             ↓             ↓
     ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
     │   App Node1  │ │   App Node2  │ │   App Node3  │
     │ (Spring Boot)│ │ (Spring Boot)│ │ (Spring Boot)│
     └──────────────┘ └──────────────┘ └──────────────┘
              │             │             │
              └─────────────┼─────────────┘
                            ↓
              ┌─────────────────────────────────┐
              │                                 │
        ┌─────┴─────┐  ┌──────────┐  ┌────────┴──────┐
        │   MySQL   │  │  Redis   │  │Elasticsearch  │
        │  Cluster  │  │ Cluster  │  │   Cluster     │
        └───────────┘  └──────────┘  └───────────────┘
                            │
                    ┌───────┴────────┐
                    │ Nebula Graph   │
                    │    Cluster     │
                    └────────────────┘
```

### 14.2 高可用方案

**应用层高可用**:
- 多实例部署（至少3个节点）
- Nginx负载均衡，健康检查
- 无状态设计，支持水平扩展

**数据层高可用**:
- MySQL主从复制，读写分离
- Redis哨兵模式或集群模式
- Elasticsearch集群部署（3节点）

---

## 15. 扩展性设计

### 15.1 新增数据源扩展

系统采用策略模式设计，新增数据源只需：

1. 实现`SqlGenerator`接口
2. 实现`EngineClient`接口
3. 在工厂类中注册

```java
// 1. 实现SQL生成器
@Component
public class NewDataSourceSqlGenerator implements SqlGenerator {
    @Override
    public String generateCreateTableSql(TableCreateRequest request) {
        // 实现逻辑
    }
}
```

### 15.2 插件化扩展

预留插件接口，支持第三方功能扩展：

```java
public interface MetadataPlugin {
    String getName();
    void initialize();
    void onTableCreated(MetadataTable table);
    void onTableUpdated(MetadataTable table);
}
```

---

## 16. 总结

### 16.1 架构特点

1. **分层清晰**: 严格的三层架构，职责分明
2. **可扩展性强**: 策略模式支持多数据源，插件化设计
3. **高性能**: 多级缓存、异步处理、连接池优化
4. **高可用**: 集群部署、主从备份、熔断降级
5. **安全可靠**: 多层安全防护，审批流程管控
6. **易维护**: 统一异常处理、日志规范、监控完善

### 16.2 技术亮点

- 基于Elasticsearch的高性能搜索
- 基于Nebula Graph的数据血缘分析
- 多数据源SQL生成器（策略模式）
- 灵活的审批流程引擎
- 完善的缓存体系（本地+分布式）
- 严格的代码规范和约束

---

**版本历史**

| 版本 | 日期 | 修改内容 | 作者 |
|------|------|----------|------|
| 1.0.0 | 2026-01-10 | 初始版本 | System |
| 2.0.0 | 2026-01-11 | 深化设计，增加详细实现方案 | System |

---

**附录**

- [需求文档](REQUIREMENT.md)
- [开发约束规则](CLAUDE.md)
- [API接口文档](http://localhost:8080/doc.html)
