package com.datawarehouse.metadata.strategy;

import com.datawarehouse.metadata.enums.DataSourceTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * SQL生成器工厂类
 *
 * @author System
 * @since 1.0.0
 */
@Component
public class SqlGeneratorFactory {

    @Autowired(required = false)
    private Map<String, SqlGenerator> strategyMap;

    /**
     * 根据数据源类型获取对应的SQL生成器
     *
     * @param dataSource 数据源类型
     * @return SQL生成器
     * @throws IllegalArgumentException 如果不支持该数据源类型
     */
    public SqlGenerator getGenerator(String dataSource) {
        if (dataSource == null || dataSource.trim().isEmpty()) {
            throw new IllegalArgumentException("数据源类型不能为空");
        }

        DataSourceTypeEnum dataSourceEnum = DataSourceTypeEnum.getByCode(dataSource);
        if (dataSourceEnum == null) {
            throw new IllegalArgumentException("不支持的数据源类型: " + dataSource);
        }

        String beanName = dataSource.toLowerCase() + "SqlGenerator";

        if (strategyMap == null || !strategyMap.containsKey(beanName)) {
            throw new IllegalArgumentException("未找到" + dataSource + "的SQL生成器实现");
        }

        return strategyMap.get(beanName);
    }
}
