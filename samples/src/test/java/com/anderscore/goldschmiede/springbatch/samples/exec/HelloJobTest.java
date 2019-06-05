package com.anderscore.goldschmiede.springbatch.samples.exec;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig
public class HelloJobTest {

    @Configuration
    @EnableBatchProcessing
    static class Config {
        @Autowired
        private StepBuilderFactory stepBuilderFactory;
        @Autowired
        private JobBuilderFactory jobBuilderFactory;

        // tag::helloTasklet[]
        @Bean
        Tasklet helloTasklet() {
            return new Tasklet() {

                @Override
                public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                    System.out.println("Hello World!");
                    return RepeatStatus.FINISHED;
                }
            };
        }
        // end::helloTasklet[]

        // tag::helloJob[]
        @Bean
        Job helloJob() {
            TaskletStep step = stepBuilderFactory
                    .get("helloStep")
                    .tasklet(helloTasklet())
                    .build();
            Job job = jobBuilderFactory
                    .get("helloJob")
                    .start(step)
                    .build();
            return job;
        }
        // end::helloJob[]
    }

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job helloJob;

    @Test
    void test() throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        jobLauncher.run(helloJob, new JobParameters());
    }

}
