package org.sinoc.contrdata.config;

import org.sinoc.config.*;
import org.springframework.context.annotation.*;
import org.sinoc.datasource.*;
import org.sinoc.datasource.leveldb.*;

@Configuration
@ComponentScan({ "org.sinoc.contrdata" })
public class ContractDataConfig
{
    @Bean
    public SystemProperties systemProperties() {
        return SystemProperties.getDefault();
    }
    
    @Bean
    public DbSource<byte[]> storageDict() {
        final DbSource<byte[]> dataSource = (DbSource<byte[]>)new LevelDbDataSource("storageDict");
        dataSource.init();
        return dataSource;
    }
}
