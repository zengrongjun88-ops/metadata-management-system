# 数据仓库元数据管理系统

## 项目简介

基于 Spring Boot 2.7.x + MyBatis Plus 的数据仓库元数据管理系统，用于管理和维护数据仓库中的元数据信息。

## 技术栈

- **核心框架**: Spring Boot 2.7.18
- **持久层框架**: MyBatis Plus 3.5.5
- **数据库**: MySQL 8.0
- **连接池**: Druid 1.2.20
- **缓存**: Redis
- **API文档**: Knife4j 3.0.3 (Swagger)
- **开发工具**: Lombok 1.18.30

## 项目结构

```
metadata-management-system
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── datawarehouse
│   │   │           └── metadata
│   │   │               ├── MetadataManagementApplication.java  # 启动类
│   │   │               ├── common                              # 通用类
│   │   │               │   └── Result.java                     # 统一返回结果
│   │   │               ├── config                              # 配置类
│   │   │               │   ├── CorsConfig.java                 # 跨域配置
│   │   │               │   ├── MybatisPlusConfig.java          # MyBatis Plus配置
│   │   │               │   └── SwaggerConfig.java              # Swagger配置
│   │   │               ├── controller                          # 控制层
│   │   │               │   └── MetadataTableController.java
│   │   │               ├── entity                              # 实体类
│   │   │               │   └── MetadataTable.java
│   │   │               ├── exception                           # 异常处理
│   │   │               │   ├── BusinessException.java          # 业务异常
│   │   │               │   └── GlobalExceptionHandler.java     # 全局异常处理
│   │   │               ├── mapper                              # 数据访问层
│   │   │               │   └── MetadataTableMapper.java
│   │   │               └── service                             # 业务层
│   │   │                   ├── IMetadataTableService.java
│   │   │                   └── impl
│   │   │                       └── MetadataTableServiceImpl.java
│   │   └── resources
│   │       ├── application.yml                                 # 应用配置
│   │       ├── db
│   │       │   └── schema.sql                                  # 数据库初始化脚本
│   │       └── mapper
│   │           └── MetadataTableMapper.xml                     # MyBatis XML映射文件
│   └── test
│       └── java
└── pom.xml                                                     # Maven配置
```

## 快速开始

### 环境要求

- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+
- Redis 5.0+

### 安装步骤

1. **克隆项目**（如果是从Git仓库）或直接使用当前项目

2. **创建数据库**

执行 `src/main/resources/db/schema.sql` 文件创建数据库和表：

```bash
mysql -u root -p < src/main/resources/db/schema.sql
```

3. **修改配置**

编辑 `src/main/resources/application.yml`，修改数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/metadata_mgmt?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false
    username: root
    password: your_password  # 修改为你的MySQL密码
```

如果使用Redis，也需要修改Redis配置：

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password:  # 如果有密码，填写密码
```

4. **编译打包**

```bash
cd metadata-management-system
mvn clean package
```

5. **运行项目**

方式一：使用Maven运行
```bash
mvn spring-boot:run
```

方式二：使用jar包运行
```bash
java -jar target/metadata-management-system-1.0.0-SNAPSHOT.jar
```

6. **访问应用**

- 应用地址: http://localhost:8080/api
- Swagger文档: http://localhost:8080/api/doc.html
- Druid监控: http://localhost:8080/api/druid/index.html (账号/密码: admin/admin)

## 主要功能

### 已实现功能

1. **元数据表管理**
   - 分页查询元数据表
   - 新增元数据表
   - 更新元数据表
   - 删除元数据表
   - 根据ID查询元数据表

2. **基础设施**
   - 统一返回结果封装
   - 全局异常处理
   - 参数校验
   - API文档（Swagger）
   - 跨域配置
   - 数据库连接池监控

### 待扩展功能

1. 元数据字段管理
2. 数据血缘关系管理
3. 数据质量监控
4. 元数据版本管理
5. 权限管理
6. 数据字典管理

## API示例

### 1. 分页查询元数据表

```http
GET /api/metadata/table/page?current=1&size=10&tableName=user
```

### 2. 新增元数据表

```http
POST /api/metadata/table
Content-Type: application/json

{
  "tableName": "user_info",
  "tableComment": "用户信息表",
  "databaseName": "user_db",
  "tableType": "BASE TABLE",
  "engine": "InnoDB",
  "charset": "utf8mb4",
  "rowCount": 10000,
  "dataSize": 2.5
}
```

### 3. 更新元数据表

```http
PUT /api/metadata/table
Content-Type: application/json

{
  "id": 1,
  "tableName": "user_info",
  "tableComment": "用户信息表-更新",
  "databaseName": "user_db"
}
```

### 4. 删除元数据表

```http
DELETE /api/metadata/table/1
```

## 开发指南

### 添加新的业务模块

1. 在 `entity` 包下创建实体类
2. 在 `mapper` 包下创建 Mapper 接口
3. 在 `resources/mapper` 下创建对应的 XML 文件
4. 在 `service` 包下创建 Service 接口和实现类
5. 在 `controller` 包下创建 Controller 类

### 代码规范

- 使用 Lombok 简化代码
- 所有接口添加 Swagger 注解
- 统一使用 Result 类返回结果
- 异常使用 BusinessException 抛出
- 遵循阿里巴巴Java开发规范

## 配置说明

### application.yml 主要配置项

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| server.port | 应用端口 | 8080 |
| spring.datasource.url | 数据库连接地址 | - |
| spring.datasource.username | 数据库用户名 | root |
| spring.datasource.password | 数据库密码 | - |
| spring.redis.host | Redis地址 | localhost |
| spring.redis.port | Redis端口 | 6379 |
| mybatis-plus.mapper-locations | Mapper XML文件位置 | classpath*:/mapper/**/*.xml |

## 常见问题

### Q1: 启动报错找不到Mapper

**A**: 确保在启动类上添加了 `@MapperScan("com.datawarehouse.metadata.mapper")` 注解

### Q2: Swagger文档无法访问

**A**: 检查 Knife4j 配置是否正确，访问地址是否包含 context-path: http://localhost:8080/api/doc.html

### Q3: 数据库连接失败

**A**: 检查MySQL是否启动，数据库是否创建，用户名密码是否正确

## 许可证

本项目仅供学习和公司内部使用。

## 联系方式

如有问题，请联系开发团队。
