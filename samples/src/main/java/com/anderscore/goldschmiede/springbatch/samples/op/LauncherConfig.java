package com.anderscore.goldschmiede.springbatch.samples.op;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableBatchProcessing
public class LauncherConfig {
    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Bean
    static HikariDataSource dataSource(@Value("${jdbc.url}") String jdbcUrl) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:hsqldb:hsql://localhost:9001/xdb");
        config.setUsername("SA");
        config.setPassword("");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        HikariDataSource ds = new HikariDataSource(config);
        return ds;
    }

    @Bean
    Tasklet helloTasklet() {
        return new Tasklet() {

            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
                System.out.println("Hello World!");
                return RepeatStatus.FINISHED;
            }
        };
    }

    @Bean
    TaskletStep helloStep() {
        TaskletStep step = stepBuilderFactory
                .get("helloStep")
                .tasklet(helloTasklet())
                .build();
        return step;
    }

    // tag::incrementer[]
    @Bean
    RunIdIncrementer incrementer() {
        return new RunIdIncrementer();
    }

    @Bean
    Job helloJob() {
        Job job = jobBuilderFactory
                .get("helloJob")
                .start(helloStep())
                .incrementer(incrementer())
                .preventRestart()
                .build();
        return job;
    }
    // end::incrementer[]
}
