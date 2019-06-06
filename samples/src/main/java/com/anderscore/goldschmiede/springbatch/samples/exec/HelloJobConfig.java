package com.anderscore.goldschmiede.springbatch.samples.exec;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// tag::config[]
@Configuration
@EnableBatchProcessing
public class HelloJobConfig {
    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    // end::config[]

    // tag::helloJob[]
    @Bean
    Job helloJob() {
        var tasklet = new Tasklet() {

            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
                System.out.println("Hello World!");
                return RepeatStatus.FINISHED;
            }
        };
        TaskletStep step = stepBuilderFactory
                .get("helloStep")
                .tasklet(tasklet)
                .build();
        Job job = jobBuilderFactory
                .get("helloJob")
                .start(step)
                .build();
        return job;
    }
    // end::helloJob[]

    @Bean
    Job goodByeJob() {
        var tasklet = new Tasklet() {

            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
                System.out.println("Good bye World!");
                return RepeatStatus.FINISHED;
            }
        };
        TaskletStep step = stepBuilderFactory
                .get("goodByeStep")
                .tasklet(tasklet)
                .build();
        Job job = jobBuilderFactory
                .get("goodByeJob")
                .start(step)
                .build();
        return job;
    }

    // tag::config[]
}
// end::config[]
