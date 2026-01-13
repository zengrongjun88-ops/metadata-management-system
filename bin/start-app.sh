#!/bin/bash
# 元数据管理系统 - 应用启动脚本

set -e

echo "=================================="
echo "元数据管理系统 - 启动脚本"
echo "=================================="

# 检查Java
java -version 2>&1 || { echo "❌ Java未安装"; exit 1; }

# 检查Maven
if command -v mvn &> /dev/null; then
    MVN_CMD="mvn"
else
    MVN_CMD="./mvnw"
fi

# 编译打包
echo "[编译] 正在编译打包..."
$MVN_CMD clean package -DskipTests

# 启动应用
echo "[启动] 正在启动应用..."
java -jar target/metadata-management-system.jar
