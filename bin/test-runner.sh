#!/bin/bash
# 元数据管理系统 - 测试执行脚本

# 检查Maven
if command -v mvn &> /dev/null; then
    MVN_CMD="mvn"
else
    MVN_CMD="./mvnw"
fi

echo "选择测试类型:"
echo "1) 所有测试"
echo "2) 单元测试"
echo "3) 集成测试"
read -p "输入选项: " choice

case $choice in
    1) $MVN_CMD clean test ;;
    2) $MVN_CMD test -Dtest=*ServiceTest ;;
    3) $MVN_CMD test -Dtest=*IntegrationTest ;;
    *) echo "无效选项"; exit 1 ;;
esac
