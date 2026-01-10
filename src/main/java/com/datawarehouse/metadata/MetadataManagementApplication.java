package com.datawarehouse.metadata;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 元数据管理系统启动类
 *
 * @author System
 * @since 1.0.0
 */
@SpringBootApplication
@MapperScan("com.datawarehouse.metadata.mapper")
public class MetadataManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(MetadataManagementApplication.class, args);
        System.out.println("==============================================");
        System.out.println("元数据管理系统启动成功！");
        System.out.println("Swagger文档地址: http://localhost:8080/doc.html");
        System.out.println("==============================================");
    }

}
