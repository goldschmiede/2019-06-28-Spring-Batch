package com.anderscore.goldschmiede.springbatch.samples.exec;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.item.support.IteratorItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.log4j.Log4j2;

@Log4j2
// tag::config[]
@Configuration
@EnableBatchProcessing
public class RetryJobConfig extends DefaultBatchConfigurer {
    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    // end::config[]

    @Bean
    ItemReader<Integer> reader() {
        return new ItemReader<Integer>() {
            private final IteratorItemReader<Integer> delegate = new IteratorItemReader<>(
                    IntStream.rangeClosed(1, 10).boxed().collect(Collectors.toList()));
            private int retry;

            @Override
            public Integer read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                Integer value = delegate.read();
                log.info("read: {}", value);
                if (Integer.valueOf(9).equals(value) && retry < 3) {
                    retry++;
                    log.info("read - retry: {}", retry);
                    throw new RetryException("read");
                }
                return value;
            }
        };
    }

    @Bean
    ItemProcessor<Integer, String> processor() {
        return new ItemProcessor<Integer, String>() {
            private int retry;

            @Override
            public String process(Integer item) throws Exception {
                log.info("process: {}", item);
                if (Integer.valueOf(5).equals(item) && retry < 3) {
                    retry++;
                    log.info("process - retry: {}", retry);
                    throw new RetryException("process");
                }
                return "v" + item;
            }
        };
    }

    @Bean
    ItemWriter<String> writer() {
        return new ItemWriter<String>() {
            private int retry;

            @Override
            public void write(List<? extends String> items) throws Exception {
                log.info("write: {}", items);
                items.forEach(this::write);
            }

            private void write(String item) {
                if ("v7".equals(item) && retry < 3) {
                    retry++;
                    log.info("write - retry: {}", retry);
                    throw new RetryException("write");
                }
                log.info("written: {}", item);
            }
        };
    }

    @Bean
    TaskletStep retryStep() {
        // tag::step[]
        TaskletStep step = stepBuilderFactory
                .get("retryStep") // StepBuilder
                .<Integer, String>chunk(4) // SimpleStepBuilder
                .reader(reader()).processor(processor()).writer(writer())
                .faultTolerant().retryLimit(7).retry(RetryException.class) // FaultTolerantStepBuilder
                .build();
        // end::step[]
        return step;
    }

    @Bean
    Job retryJob(Step step) {
        Job job = jobBuilderFactory
                .get("retryJob")
                .start(step)
                .build();
        return job;
    }

    // tag::config[]
}
// end::config[]
