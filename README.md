# 数据仓库元数据管理系统

[![Java](https://img.shields.io/badge/Java-1.8+-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MyBatis Plus](https://img.shields.io/badge/MyBatis%20Plus-3.5.5-blue.svg)](https://baomidou.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

> 一个功能完整、测试全面的数据仓库元数据管理系统，支持多数据源、审批流程、操作历史追踪。

## 📋 目录

- [项目简介](#项目简介)
- [核心功能](#核心功能)
- [技术栈](#技术栈)
- [快速开始](#快速开始)
- [功能演示](#功能演示)
- [测试](#测试)
- [项目结构](#项目结构)
- [文档](#文档)
- [贡献指南](#贡献指南)
- [许可证](#许可证)

---

## 项目简介

数据仓库元数据管理系统是一个用于统一管理多数据源元数据的平台，支持表的创建、修改、查询、审批等完整生命周期管理。

### ✨ 特性

- 🎯 **多数据源支持**: Hive、BigQuery、ClickHouse、Paimon、Iceberg、StarRocks
- 🔄 **审批流程**: 完整的状态机设计，支持创建、提交、审批、拒绝、取消、发布
- 📝 **操作历史**: 自动记录所有操作，完整可追溯
- 🛡️ **SQL防护**: 危险SQL拦截，防止误操作
- 🧪 **测试完善**: 67+测试用例，85%+代码覆盖率
- 📖 **文档齐全**: 7份完整技术文档

---

## 核心功能

### 1. 元数据表管理

- ✅ 创建表（含字段定义）
- ✅ 查询表详情（含字段列表）
- ✅ 更新表信息
- ✅ 删除表（软删除）
- ✅ 分页查询
- ✅ 按条件搜索

### 2. 元数据字段管理

- ✅ 批量创建字段
- ✅ 更新字段信息
- ✅ 删除字段
- ✅ 查询字段列表

### 3. 审批流程管理

```
DRAFT → PENDING → APPROVED → PUBLISHED
           ↓
        REJECTED / CANCELLED
```

- ✅ 创建审批单
- ✅ 提交审批
- ✅ 审批通过/拒绝
- ✅ 取消审批
- ✅ 发布变更
- ✅ 查询审批历史

### 4. SQL生成与校验

- ✅ 自动生成建表SQL（支持6种数据源）
- ✅ SQL语法校验
- ✅ 危险SQL拦截（DROP DATABASE、TRUNCATE等）

### 5. 操作历史

- ✅ 自动记录所有操作（CREATE、UPDATE、DELETE）
- ✅ 查询操作历史
- ✅ 变更前后对比

---

## 技术栈

### 后端

| 技术 | 版本 | 说明 |
|-----|------|------|
| Spring Boot | 2.7.18 | 应用框架 |
| MyBatis Plus | 3.5.5 | ORM框架 |
| MySQL | 8.0+ | 关系数据库 |
| Druid | 1.2.20 | 数据库连接池 |
| Knife4j | 4.3.0 | API文档 |
| Lombok | 1.18.30 | 简化代码 |

### 测试

| 技术 | 版本 | 说明 |
|-----|------|------|
| JUnit 5 | 5.x | 测试框架 |
| Mockito | 4.x | Mock框架 |
| H2 Database | 2.x | 内存数据库 |
| Spring Boot Test | 2.7.18 | 集成测试 |

---

## 快速开始

### 环境要求

- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+

### 1. 克隆代码

```bash
git clone https://github.com/zengrongjun88-ops/metadata-management-system.git
cd metadata-management-system
```

### 2. 初始化数据库

```bash
# 创建数据库
mysql -u root -p
> CREATE DATABASE metadata_mgmt DEFAULT CHARACTER SET utf8mb4;
> exit

# 执行Schema脚本
mysql -u root -p metadata_mgmt < src/main/resources/db/schema.sql
```

### 3. 修改配置

编辑 `src/main/resources/application.yml`，修改数据库密码：

```yaml
spring:
  datasource:
    username: root
    password: your_password  # 修改为你的密码
```

### 4. 启动应用

**方法1: 使用启动脚本**
```bash
./bin/start-app.sh
```

**方法2: Maven命令**
```bash
mvn spring-boot:run
```

**方法3: 打包运行**
```bash
mvn clean package -DskipTests
java -jar target/metadata-management-system.jar
```

### 5. 访问应用

- **Swagger文档**: http://localhost:8080/api/doc.html
- **健康检查**: http://localhost:8080/api/actuator/health

---

## 功能演示

### 创建表

**请求**:
```bash
curl -X POST http://localhost:8080/api/metadata/tables \
  -H "Content-Type: application/json" \
  -d '{
    "dataSource": "Hive",
    "databaseName": "test_db",
    "tableName": "user_info",
    "tableComment": "用户信息表",
    "warehouseLayer": "dwd",
    "themeFirst": "usr",
    "themeSecond": "mem",
    "sensitivityLevel": "L1",
    "importanceLevel": "P1",
    "owner": "admin",
    "fields": [
      {
        "fieldName": "user_id",
        "fieldType": "BIGINT",
        "fieldComment": "用户ID",
        "fieldOrder": 1,
        "isPrimaryKey": 1
      }
    ]
  }'
```

**响应**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": 1
}
```

### 查询表详情

**请求**:
```bash
curl http://localhost:8080/api/metadata/tables/1
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "tableName": "user_info",
    "databaseName": "test_db",
    "fields": [
      {
        "fieldName": "user_id",
        "fieldType": "BIGINT",
        "fieldComment": "用户ID"
      }
    ]
  }
}
```

### 生成SQL

**请求**:
```bash
curl -X POST http://localhost:8080/api/metadata/tables/generate-sql \
  -H "Content-Type: application/json" \
  -d '{
    "dataSource": "Hive",
    "databaseName": "test_db",
    "tableName": "test_table",
    "fields": [
      {
        "fieldName": "id",
        "fieldType": "BIGINT",
        "fieldComment": "主键"
      }
    ]
  }'
```

**响应**:
```sql
CREATE TABLE IF NOT EXISTS test_db.test_table (
  id BIGINT COMMENT '主键'
)
STORED AS PARQUET;
```

---

## 测试

### 执行测试

**方法1: 使用测试脚本**
```bash
./bin/test-runner.sh
```

**方法2: Maven命令**
```bash
# 执行所有测试
mvn clean test

# 执行单元测试
mvn test -Dtest=*ServiceTest

# 执行集成测试
mvn test -Dtest=*IntegrationTest

# 生成覆盖率报告
mvn clean test jacoco:report
open target/site/jacoco/index.html
```

### 测试统计

| 测试类型 | 用例数 | 覆盖率 |
|---------|--------|--------|
| 单元测试 | 52+ | Service层 |
| 集成测试 | 7+ | 完整流程 |
| Controller测试 | 8+ | REST API |
| **总计** | **67+** | **85%+** |

---

## 项目结构

```
metadata-management-system/
├── src/main/java/                      # 主代码
│   └── com/datawarehouse/metadata/
│       ├── controller/                 # Controller层（4个）
│       ├── service/                    # Service层（4个接口+实现）
│       ├── mapper/                     # Mapper层（4个）
│       ├── entity/                     # 实体类（4个）
│       ├── dto/                        # 数据传输对象（7个）
│       ├── vo/                         # 视图对象（6个）
│       ├── enums/                      # 枚举类（12个）
│       ├── strategy/                   # SQL生成器（6个）
│       ├── converter/                  # 转换器（2个）
│       ├── config/                     # 配置类（4个）
│       ├── common/                     # 通用类（3个）
│       └── exception/                  # 异常类（2个）
├── src/main/resources/
│   ├── application.yml                 # 主配置文件
│   └── db/schema.sql                   # 数据库Schema
├── src/test/java/                      # 测试代码
│   └── com/datawarehouse/metadata/
│       ├── service/                    # Service测试（3个）
│       ├── controller/                 # Controller测试（1个）
│       ├── integration/                # 集成测试（1个）
│       └── strategy/                   # 策略测试（1个）
├── src/test/resources/
│   ├── application-test.yml            # 测试配置
│   └── db/                             # 测试数据库脚本
├── claude/                             # 文档目录
│   ├── Architecture.md                 # 架构设计文档
│   ├── TEST_REPORT.md                  # 测试报告
│   ├── VERIFICATION_GUIDE.md           # 验证指南
│   ├── IMPLEMENTATION_REPORT.md        # 实施报告
│   ├── PROJECT_SUMMARY.md              # 项目总结
│   ├── CLAUDE.md                       # 开发约束
│   └── REQUIREMENT.md                  # 需求文档
├── bin/                                # 脚本目录
│   ├── start-app.sh                    # 启动脚本
│   └── test-runner.sh                  # 测试脚本
├── pom.xml                             # Maven配置
└── README.md                           # 本文件
```

---

## 文档

### 技术文档

| 文档 | 路径 | 说明 |
|-----|------|------|
| 需求文档 | [claude/REQUIREMENT.md](claude/REQUIREMENT.md) | 业务需求说明 |
| 架构设计 | [claude/Architecture.md](claude/Architecture.md) | 详细技术设计 |
| 测试报告 | [claude/TEST_REPORT.md](claude/TEST_REPORT.md) | 测试覆盖说明 |
| 验证指南 | [claude/VERIFICATION_GUIDE.md](claude/VERIFICATION_GUIDE.md) | 功能验证步骤 |
| 项目总结 | [claude/PROJECT_SUMMARY.md](claude/PROJECT_SUMMARY.md) | 项目完成总结 |
| 开发约束 | [claude/CLAUDE.md](claude/CLAUDE.md) | 开发规范约束 |

### API文档

启动应用后访问：http://localhost:8080/api/doc.html

---

## 数据库设计

### 核心表

| 表名 | 说明 | 主要字段 |
|-----|------|---------|
| metadata_table | 元数据表信息 | table_name, database_name, data_source |
| metadata_field | 元数据字段信息 | field_name, field_type, table_id |
| approval_flow | 审批流程 | flow_no, status, submitter |
| operation_history | 操作历史 | operation_type, operator, operation_time |

### ER图

```
metadata_table (1) ----< (N) metadata_field
metadata_table (1) ----< (N) approval_flow
metadata_table (1) ----< (N) operation_history
```

---

## 设计模式

### 已应用的设计模式

- **策略模式**: SQL生成器（支持多种数据源）
- **工厂模式**: SQL生成器工厂
- **状态机模式**: 审批流程状态管理
- **转换器模式**: Entity与VO转换
- **模板方法模式**: MyBatis Plus ServiceImpl

---

## 后续规划

### Phase 2: 搜索增强
- [ ] 集成Elasticsearch
- [ ] 全文搜索
- [ ] 拼音搜索
- [ ] 搜索高亮

### Phase 3: 缓存优化
- [ ] 集成Redis
- [ ] 缓存策略
- [ ] 缓存预热

### Phase 4: 数据血缘
- [ ] 集成Nebula Graph
- [ ] 表级血缘
- [ ] 字段级血缘

### Phase 5: 前端开发
- [ ] Ant Design UI
- [ ] 可视化界面

---

## 常见问题

### Q1: 如何修改数据库密码？

编辑 `src/main/resources/application.yml`：
```yaml
spring:
  datasource:
    password: your_password
```

### Q2: 如何查看API文档？

启动应用后访问：http://localhost:8080/api/doc.html

### Q3: 如何执行测试？

```bash
# 使用测试脚本
./bin/test-runner.sh

# 或使用Maven
mvn clean test
```

### Q4: 数据库初始化失败？

检查：
1. MySQL服务是否启动
2. 数据库是否已创建
3. 用户权限是否正确

### Q5: 如何查看日志？

日志输出到控制台，也可以配置文件输出：
```yaml
logging:
  file:
    name: logs/app.log
```

---

## 贡献指南

欢迎贡献代码！请遵循以下步骤：

1. Fork本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建Pull Request

### 开发规范

请参考 [claude/CLAUDE.md](claude/CLAUDE.md) 了解详细的开发约束和规范。

---

## 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

---

## 联系方式

- **GitHub**: https://github.com/zengrongjun88-ops/metadata-management-system
- **Issues**: https://github.com/zengrongjun88-ops/metadata-management-system/issues

---

## 致谢

感谢以下开源项目：

- [Spring Boot](https://spring.io/projects/spring-boot)
- [MyBatis Plus](https://baomidou.com/)
- [Knife4j](https://doc.xiaominfo.com/)
- [Druid](https://github.com/alibaba/druid)

---

**⭐ 如果这个项目对你有帮助，请给一个Star！**

---

**最后更新**: 2026-01-13  
**版本**: v1.0.0  
**状态**: ✅ 核心功能已完成
