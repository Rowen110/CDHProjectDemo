package com.cloudera.phoenixdemo.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @author Charles
 * @package com.cloudera.phoenixdemo.config
 * @classname DataSourceConfig
 * @description 多数据源配置
 * @date 2019-4-26 16:30
 */
@Configuration
public class DataSourceConfig {
    @Bean(name = "outerData")
    @ConfigurationProperties(prefix = "spring.datasource.master")
    public DataSource outerData() {
        return new DruidDataSource();
    }

    @Bean(name = "sentinelData")
    @ConfigurationProperties(prefix = "spring.datasource.slave")
    public DataSource sentinelData() {
        return new DruidDataSource();
    }

    @Bean(name = "phoenixData")
    @ConfigurationProperties(prefix = "spring.datasource.phoenix")
    public DataSource phoenixData() {
        return new DruidDataSource();
    }
}
