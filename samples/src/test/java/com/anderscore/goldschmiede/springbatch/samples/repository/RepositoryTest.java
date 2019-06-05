package com.anderscore.goldschmiede.springbatch.samples.repository;

import java.util.stream.IntStream;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.item.support.IteratorItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@SpringJUnitConfig
@TestPropertySource(properties = "jdbc.url: jdbc:hsqldb:hsql://localhost:9001/xdb")
@ActiveProfiles("embedded")
public class RepositoryTest {

    private static final Logger LOG = LogManager.getLogger(RepositoryTest.class);

    @TestConfiguration
    @EnableBatchProcessing
    static class Config {
        @Autowired
        private StepBuilderFactory stepBuilderFactory;
        @Autowired
        private JobBuilderFactory jobBuilderFactory;

        @Bean
        Job job() {
            TaskletStep step = stepBuilderFactory.get("step").chunk(2)
                    .reader(new IteratorItemReader<Integer>(IntStream.range(1, 100).boxed().iterator()))
                    .writer(LOG::info)
                    .build();
            Job job = jobBuilderFactory.get("job").start(step).build();
            return job;
        }

        @Profile("embedded")
        @Bean(name = "dataSource")
        DataSource embeddedDataSource() {
            return new EmbeddedDatabaseBuilder()
                    .setType(EmbeddedDatabaseType.HSQL)
                    .addScript("org/springframework/batch/core/schema-hsqldb.sql")
                    .build();
        }

        @Profile("hsqldb")
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

    @Disabled
    @Test
    @Sql("/org/springframework/batch/core/schema-hsqldb.sql")
    void testInitDb() {
    }

    @Test
    void test() throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        jobLauncher.run(job, new JobParameters());
    }

}
