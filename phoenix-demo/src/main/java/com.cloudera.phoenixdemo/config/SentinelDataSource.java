package com.cloudera.phoenixdemo.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Configuration
@MapperScan(basePackages = "com.cloudera.phoenixdemo.dao.sentinel",  sqlSessionFactoryRef = "sentinelSqlSessionFactoryRef")
public class SentinelDataSource {

    @Autowired
    @Qualifier("sentinelData")
    private DataSource sentinelData;

    @Bean
    public SqlSessionFactory sentinelSqlSessionFactoryRef() throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(sentinelData); // 使用dDashboard数据源, 连接d_dashboard库
        factoryBean.setMapperLocations(
                new PathMatchingResourcePatternResolver().getResources("classpath:config/mappers/sentinel/*.xml")
        );
        return factoryBean.getObject();
    }

    @Bean
    public SqlSessionTemplate sentinelSqlSessionTemplate() throws Exception {
        SqlSessionTemplate template = new SqlSessionTemplate(sentinelSqlSessionFactoryRef()); // 使用上面配置的Factory
        return template;
    }

    @Bean
    public DataSourceTransactionManager sentinelTransactionManager() {
        return new DataSourceTransactionManager(sentinelData);
    }


}
