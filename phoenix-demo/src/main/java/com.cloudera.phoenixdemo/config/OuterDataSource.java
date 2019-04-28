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
@MapperScan(basePackages = "com.cloudera.phoenixdemo.dao.outer", sqlSessionFactoryRef = "outerSqlSessionFactoryRef")
public class OuterDataSource {
    @Autowired
    @Qualifier("outerData")
    private DataSource outerData;

    @Bean
    public SqlSessionFactory outerSqlSessionFactoryRef() throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(outerData); // 使用dDashboard数据源, 连接d_dashboard库
        factoryBean.setMapperLocations(
                new PathMatchingResourcePatternResolver().getResources("classpath:config/mappers/outer/*.xml")
        );
        return factoryBean.getObject();
    }

    @Bean
    public SqlSessionTemplate outerSqlSessionTemplate() throws Exception {
        SqlSessionTemplate template = new SqlSessionTemplate(outerSqlSessionFactoryRef()); // 使用上面配置的Factory
        return template;
    }

    @Bean
    public DataSourceTransactionManager outerTransactionManager() {
        return new DataSourceTransactionManager(outerData);
    }
}