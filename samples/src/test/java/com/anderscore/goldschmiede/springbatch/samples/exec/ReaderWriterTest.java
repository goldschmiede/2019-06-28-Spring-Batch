package com.anderscore.goldschmiede.springbatch.samples.exec;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig
public class ReaderWriterTest {

    @Configuration
    @EnableBatchProcessing
    static class Config {
        @Autowired
        private StepBuilderFactory stepBuilderFactory;
        @Autowired
        private JobBuilderFactory jobBuilderFactory;

        @Bean
        ItemReader<Integer> reader() {
            return new ListItemReader<>(IntStream.rangeClosed(1, 10).boxed().collect(Collectors.toList()));
        }

        @Bean
        ItemProcessor<Integer, String> processor() {
            return new ItemProcessor<Integer, String>() {

                @Override
                public String process(Integer item) throws Exception {
                    return "v" + item;
                }
            };
        }

        @Bean
        ItemWriter<String> writer() {
            return new ItemWriter<String>() {

                @Override
                public void write(List<? extends String> items) throws Exception {
                    System.out.println(items);
                }
            };
        }

        Step myStep() {
            TaskletStep step = stepBuilderFactory
                    .get("myStep")
                    .<Integer, String>chunk(4)
                    .reader(reader()).processor(processor()).writer(writer())
                    .build();
            return step;
        }

        @Bean
        Job myJob() {
            Job job = jobBuilderFactory
                    .get("myJob")
                    .start(myStep())
                    .build();
            return job;
        }
    }

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job myJob;

    // tag::code[]
    @Test
    void testReaderWriterJob() throws JobParametersInvalidException, JobExecutionAlreadyRunningException,
            JobRestartException, JobInstanceAlreadyCompleteException {
        jobLauncher.run(myJob, new JobParameters());
    }

}
