package com.datawarehouse.metadata;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * 集成测试基类
 * 使用H2内存数据库，每个测试方法结束后回滚事务
 *
 * @author System
 * @since 1.0.0
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = {
        "springfox.documentation.enabled=false",
        "spring.mvc.pathmatch.matching-strategy=ant_path_matcher"
    }
)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
public abstract class BaseIntegrationTest {

    @BeforeEach
    void setUp() {
        // 子类可以覆盖此方法进行额外的初始化
    }
}
