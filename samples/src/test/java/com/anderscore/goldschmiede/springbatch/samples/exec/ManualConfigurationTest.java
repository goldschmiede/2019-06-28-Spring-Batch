package com.anderscore.goldschmiede.springbatch.samples.exec;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.item.support.ListItemWriter;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;

/**
 * This is a most basic sample showing the usage of the spring-batch core framework. It doesn't use the magic of Spring
 * for plumbing to make it easier to comprehend.
 */
public class ManualConfigurationTest {
    private int SIZE = 5;

    @Test
    public void test() throws Exception {
        // prepare infrastructure
        MapJobRepositoryFactoryBean jobRepositoryFactoryBean = new MapJobRepositoryFactoryBean();
        JobRepository jobRepository = jobRepositoryFactoryBean.getObject();
        ResourcelessTransactionManager transactionManager = new ResourcelessTransactionManager();
        JobBuilderFactory jobBuilderFactory = new JobBuilderFactory(jobRepository);
        StepBuilderFactory stepBuilderFactory = new StepBuilderFactory(jobRepository, transactionManager);

        // create reader writer and processor
        ListItemReader<Integer> reader = new ListItemReader<>(IntStream.rangeClosed(1, SIZE).boxed().collect(Collectors.toList()));
        ListItemWriter<String> writer = new ListItemWriter<>();
        ItemProcessor<Integer, String> processor = i -> "item " + i;

        // build a step
        Step step = stepBuilderFactory.get("step")
                .<Integer, String>chunk(2)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();

        // create the job
        Job job = jobBuilderFactory.get("job").flow(step).end().build();

        // create a launcher
        SimpleJobLauncher launcher = new SimpleJobLauncher();
        launcher.setJobRepository(jobRepository);
        launcher.afterPropertiesSet();

        // launch the job
        JobParameters params = new JobParameters();
        JobExecution execution = launcher.run(job, params);

        // make sure job finished successfully
        assertThat(execution.getAllFailureExceptions()).isEmpty();
        assertThat(execution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);

        // check the result
        List<? extends String> items = writer.getWrittenItems();
        assertThat(items).hasSize(SIZE);
    }
}
