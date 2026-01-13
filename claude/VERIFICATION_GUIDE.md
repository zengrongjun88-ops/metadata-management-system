# 元数据管理系统 - 功能验证指南

## 1. 验证准备

### 1.1 环境要求

| 组件 | 版本要求 | 说明 |
|------|---------|------|
| JDK | 1.8+ | 推荐使用JDK 11 |
| Maven | 3.6+ | 或使用Maven Wrapper |
| MySQL | 8.0+ | 元数据存储 |
| Redis | 6.x+ | 缓存（可选） |

### 1.2 数据库初始化

```bash
# 1. 创建数据库
mysql -u root -p
CREATE DATABASE metadata_mgmt DEFAULT CHARACTER SET utf8mb4;

# 2. 执行Schema脚本
mysql -u root -p metadata_mgmt < src/main/resources/db/schema.sql

# 3. 修改配置
# 编辑 src/main/resources/application.yml
# 修改数据库密码: spring.datasource.password
```

---

## 2. 快速启动

### 2.1 使用启动脚本

```bash
# 方法1: 使用提供的启动脚本
./bin/start-app.sh

# 方法2: 手动启动
mvn spring-boot:run

# 方法3: 打包后启动
mvn clean package -DskipTests
java -jar target/metadata-management-system.jar
```

### 2.2 验证启动成功

应用启动后，检查以下信息：

```
✅ Tomcat started on port(s): 8080
✅ Started MetadataManagementApplication
✅ Mapped "/api" ...
✅ Swagger UI: http://localhost:8080/api/doc.html
```

---

## 3. 功能验证清单

### 3.1 Swagger文档验证

访问: http://localhost:8080/api/doc.html

**验证项**:
- ✅ Swagger页面正常打开
- ✅ 查看"元数据表管理"接口文档
- ✅ 查看"元数据字段管理"接口文档
- ✅ 查看"审批流程管理"接口文档
- ✅ 查看"操作历史管理"接口文档

---

### 3.2 元数据表管理验证

####  3.2.1 创建表（POST /api/metadata/tables）

**请求示例**:
```json
{
  "dataSource": "Hive",
  "databaseName": "test_db",
  "tableName": "user_info_test",
  "tableComment": "用户信息测试表",
  "warehouseLayer": "dwd",
  "themeFirst": "usr",
  "themeSecond": "mem",
  "sensitivityLevel": "L1",
  "importanceLevel": "P1",
  "partitionType": "NONE",
  "updateFrequency": "DAILY",
  "owner": "admin",
  "fields": [
    {
      "fieldName": "user_id",
      "fieldType": "BIGINT",
      "fieldComment": "用户ID",
      "fieldOrder": 1,
      "isPrimaryKey": 1,
      "isNullable": 0
    },
    {
      "fieldName": "user_name",
      "fieldType": "STRING",
      "fieldComment": "用户姓名",
      "fieldOrder": 2,
      "isPrimaryKey": 0,
      "isNullable": 1
    }
  ]
}
```

**预期响应**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": 1,
  "timestamp": 1234567890
}
```

**验证点**:
- ✅ 响应code=200
- ✅ 返回表ID
- ✅ 数据库中metadata_table表新增记录
- ✅ 数据库中metadata_field表新增2条字段记录
- ✅ operation_history表新增历史记录

---

#### 3.2.2 查询表详情（GET /api/metadata/tables/{id}）

**请求**: GET /api/metadata/tables/1

**预期响应**:
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "tableName": "user_info_test",
    "databaseName": "test_db",
    "tableComment": "用户信息测试表",
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

**验证点**:
- ✅ 返回表基本信息
- ✅ 包含字段列表
- ✅ 字段按order排序

---

#### 3.2.3 分页查询（GET /api/metadata/tables/page）

**请求参数**:
```
pageNum=1
pageSize=10
databaseName=test_db
```

**预期响应**:
```json
{
  "code": 200,
  "data": {
    "total": 1,
    "records": [...]
  }
}
```

**验证点**:
- ✅ 分页信息正确
- ✅ 过滤条件生效
- ✅ 排序正常

---

#### 3.2.4 更新表（PUT /api/metadata/tables/{id}）

**请求**: PUT /api/metadata/tables/1
```json
{
  "id": 1,
  "tableComment": "更新后的表注释",
  "owner": "new_admin"
}
```

**预期响应**:
```json
{
  "code": 200,
  "message": "更新成功"
}
```

**验证点**:
- ✅ 响应成功
- ✅ 数据库记录已更新
- ✅ operation_history记录UPDATE操作

---

#### 3.2.5 删除表（DELETE /api/metadata/tables/{id}）

**请求**: DELETE /api/metadata/tables/1

**预期响应**:
```json
{
  "code": 200,
  "message": "删除成功"
}
```

**验证点**:
- ✅ 响应成功
- ✅ 软删除（deleted=1）
- ✅ 字段级联删除
- ✅ 历史记录DELETE操作

---

#### 3.2.6 生成SQL（POST /api/metadata/tables/generate-sql）

**请求**:
```json
{
  "dataSource": "Hive",
  "databaseName": "test_db",
  "tableName": "test_table",
  "tableComment": "测试表",
  "fields": [
    {
      "fieldName": "id",
      "fieldType": "BIGINT",
      "fieldComment": "主键"
    }
  ]
}
```

**预期响应**:
```json
{
  "code": 200,
  "data": "CREATE TABLE IF NOT EXISTS test_db.test_table (\n  id BIGINT COMMENT '主键'\n)\nCOMMENT '测试表'\nSTORED AS PARQUET;"
}
```

**验证点**:
- ✅ 返回正确的SQL
- ✅ 包含库名、表名
- ✅ 包含字段定义和注释

---

#### 3.2.7 校验SQL（POST /api/metadata/tables/validate-sql）

**测试1: 合法SQL**
```
sql: CREATE TABLE test (id BIGINT)
dataSource: Hive
```
**预期**: code=200, message="校验通过"

**测试2: 危险SQL**
```
sql: DROP DATABASE test_db
dataSource: Hive
```
**预期**: code=500, message包含"禁止执行DROP DATABASE"

**验证点**:
- ✅ 合法SQL通过校验
- ✅ 危险SQL被拦截
- ✅ 返回明确的错误信息

---

### 3.3 审批流程验证

#### 3.3.1 创建审批单（POST /api/approval）

**请求**:
```json
{
  "tableId": 1,
  "approvalType": "UPDATE",
  "changeContent": "{\"operation\":\"update_table\"}"
}
```

**预期响应**:
```json
{
  "code": 200,
  "data": 1
}
```

**验证点**:
- ✅ 返回审批单ID
- ✅ approval_flow表新增记录
- ✅ status=DRAFT
- ✅ flowNo格式: APR-yyyyMMdd-xxxxxx

---

#### 3.3.2 提交审批（POST /api/approval/{id}/submit）

**请求**: POST /api/approval/1/submit

**预期响应**:
```json
{
  "code": 200,
  "message": "提交成功"
}
```

**验证点**:
- ✅ status变更为PENDING
- ✅ submitTime已记录

---

#### 3.3.3 审批通过（POST /api/approval/{id}/approve）

**请求**:
```json
{
  "comment": "审批通过"
}
```

**预期响应**:
```json
{
  "code": 200,
  "message": "审批成功"
}
```

**验证点**:
- ✅ status变更为APPROVED
- ✅ approveComment已保存
- ✅ approveTime已记录

---

#### 3.3.4 发布变更（POST /api/approval/{id}/publish）

**请求**: POST /api/approval/1/publish

**预期响应**:
```json
{
  "code": 200,
  "message": "发布成功"
}
```

**验证点**:
- ✅ status变更为PUBLISHED
- ✅ 变更内容已执行

---

### 3.4 操作历史验证

#### 查询操作历史（GET /api/operation-history）

**请求参数**:
```
tableId=1
pageNum=1
pageSize=10
```

**预期响应**:
```json
{
  "code": 200,
  "data": {
    "total": 3,
    "records": [
      {
        "operationType": "CREATE",
        "operator": "admin",
        "operationDesc": "创建表"
      }
    ]
  }
}
```

**验证点**:
- ✅ 返回操作历史列表
- ✅ 包含CREATE/UPDATE/DELETE记录
- ✅ 按时间倒序排列

---

## 4. 数据库验证

### 4.1 检查表结构

```sql
-- 查看所有表
SHOW TABLES;

-- 应包含:
-- metadata_table
-- metadata_field
-- approval_flow
-- operation_history
```

### 4.2 检查数据完整性

```sql
-- 查询表数量
SELECT COUNT(*) FROM metadata_table WHERE deleted=0;

-- 查询字段数量
SELECT COUNT(*) FROM metadata_field WHERE deleted=0;

-- 查询审批单
SELECT * FROM approval_flow ORDER BY create_time DESC LIMIT 10;

-- 查询操作历史
SELECT * FROM operation_history ORDER BY operation_time DESC LIMIT 10;
```

### 4.3 检查索引

```sql
-- 查看索引
SHOW INDEX FROM metadata_table;
SHOW INDEX FROM metadata_field;
```

**验证点**:
- ✅ uk_database_table唯一索引
- ✅ idx_data_source索引
- ✅ 外键约束正常

---

## 5. 异常场景验证

### 5.1 参数校验

**测试**: 创建表时不提供必填字段
```json
{
  "tableName": "test"
  // 缺少其他必填字段
}
```

**预期**: code=400, message包含"参数校验失败"

---

### 5.2 业务规则验证

**测试**: 创建重复表名
```json
{
  "databaseName": "test_db",
  "tableName": "user_info_test"
  // 表名已存在
}
```

**预期**: code=500, message包含"表已存在"

---

### 5.3 状态机验证

**测试**: 对APPROVED状态的审批单再次审批

**预期**: code=500, message包含"当前状态不允许审批"

---

## 6. 性能验证

### 6.1 批量操作

```bash
# 批量创建10张表
for i in {1..10}; do
  curl -X POST http://localhost:8080/api/metadata/tables \
    -H "Content-Type: application/json" \
    -d "{\"tableName\":\"test_table_$i\", ...}"
done
```

**验证点**:
- ✅ 所有请求在合理时间内完成
- ✅ 无死锁或超时

### 6.2 分页查询性能

```bash
# 查询大数据量
curl "http://localhost:8080/api/metadata/tables/page?pageNum=1&pageSize=100"
```

**验证点**:
- ✅ 响应时间 < 1秒
- ✅ 分页信息正确

---

## 7. 日志验证

### 7.1 检查应用日志

```bash
# 查看日志
tail -f logs/metadata-management-system.log
```

**验证点**:
- ✅ SQL日志输出（MyBatis Plus）
- ✅ 异常堆栈完整
- ✅ 关键操作有日志记录

---

## 8. 测试执行验证

### 8.1 执行测试

```bash
# 方法1: 使用测试脚本
./bin/test-runner.sh

# 方法2: Maven命令
mvn clean test
```

### 8.2 查看测试报告

```bash
# 打开测试报告
open target/surefire-reports/index.html

# 查看覆盖率报告
mvn jacoco:report
open target/site/jacoco/index.html
```

**验证点**:
- ✅ 所有测试通过
- ✅ 代码覆盖率 > 80%

---

## 9. 验证总结

### 9.1 功能验证清单

| 功能模块 | 验证状态 |
|---------|---------|
| 元数据表管理 | □ |
| 元数据字段管理 | □ |
| 审批流程管理 | □ |
| 操作历史记录 | □ |
| SQL生成器 | □ |
| SQL校验 | □ |
| 异常处理 | □ |
| 日志输出 | □ |
| 测试通过 | □ |

### 9.2 问题记录

| 问题描述 | 严重级别 | 状态 |
|---------|---------|------|
| - | - | - |

---

## 10. 附录

### 10.1 常用curl命令

```bash
# 创建表
curl -X POST http://localhost:8080/api/metadata/tables \
  -H "Content-Type: application/json" \
  -d @examples/create-table.json

# 查询表
curl http://localhost:8080/api/metadata/tables/1

# 更新表
curl -X PUT http://localhost:8080/api/metadata/tables/1 \
  -H "Content-Type: application/json" \
  -d '{"id":1,"tableComment":"Updated"}'

# 删除表
curl -X DELETE http://localhost:8080/api/metadata/tables/1
```

### 10.2 数据库快速命令

```sql
-- 清空测试数据
DELETE FROM operation_history;
DELETE FROM approval_flow;
DELETE FROM metadata_field;
DELETE FROM metadata_table;

-- 重置自增ID
ALTER TABLE metadata_table AUTO_INCREMENT = 1;
ALTER TABLE metadata_field AUTO_INCREMENT = 1;
```

---

**验证指南版本**: 1.0.0  
**编写日期**: 2026-01-13  
**适用版本**: metadata-management-system v1.0.0
