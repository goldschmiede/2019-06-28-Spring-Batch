package com.anderscore.goldschmiede.springbatch.samples.parallel;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Disabled
@SpringJUnitConfig
@TestPropertySource(properties = "jdbc.url: jdbc:hsqldb:hsql://localhost:9001/xdb")
public class PartitionDbTest {

    @Configuration
    @Import(PartitionConfig.class)
    static class Config {

        @Bean(name = "dataSource")
        HikariDataSource hikariDataSource(@Value("${jdbc.url}") String jdbcUrl) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setUsername("SA");
            config.setPassword("");
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            HikariDataSource ds = new HikariDataSource(config);
            return ds;
        }
    }

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job job;

    @Test
    void testJob() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("ts", System.currentTimeMillis())
                .toJobParameters();
        JobExecution jobExecution = jobLauncher.run(job, jobParameters);
        System.out.println(jobExecution);
        jobExecution.getStepExecutions().forEach(System.out::println);
    }

}
