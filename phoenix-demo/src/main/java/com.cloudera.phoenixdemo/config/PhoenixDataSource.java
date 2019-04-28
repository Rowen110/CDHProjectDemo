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
@MapperScan(basePackages = "com.cloudera.phoenixdemo.dao.phoenix",  sqlSessionFactoryRef = "phoenixSqlSessionFactoryRef")
public class PhoenixDataSource {

    @Autowired
    @Qualifier("phoenixData")
    private DataSource phoenixData;

    @Bean
    public SqlSessionFactory phoenixSqlSessionFactoryRef() throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(phoenixData);
        factoryBean.setMapperLocations(
                new PathMatchingResourcePatternResolver().getResources("classpath:config/mappers/phoenix/*.xml")
        );
        return factoryBean.getObject();
    }

    @Bean
    public SqlSessionTemplate phoenixSqlSessionTemplate() throws Exception {
        SqlSessionTemplate template = new SqlSessionTemplate(phoenixSqlSessionFactoryRef()); // 使用上面配置的Factory
        return template;
    }

    @Bean
    public DataSourceTransactionManager phoenixTransactionManager() {
        return new DataSourceTransactionManager(phoenixData);
    }

}