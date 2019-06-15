package com.anderscore.goldschmiede.springbatch.samples.exec;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import lombok.extern.log4j.Log4j2;

@Configuration
@EnableBatchProcessing
@Log4j2
public class FlowJobConfig extends DefaultBatchConfigurer {
    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Bean
    TaskExecutor executor() {
        return new SimpleAsyncTaskExecutor(); // Don't use this in production!
    }

    private TaskletStep createTaskletStep(String name) {
        return createTaskletStep(name, null);
    }

    private TaskletStep createTaskletStep(String name, ExitStatus exitStatus) {
        Tasklet tasklet = new Tasklet() {

            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                log.info("tasklet: {}", name);
                if (exitStatus != null) {
                    contribution.setExitStatus(exitStatus);
                }
                return RepeatStatus.FINISHED;
            }
        };
        TaskletStep step = stepBuilderFactory.get(name).tasklet(tasklet).build();
        return step;
    }

    @Bean
    Job flowJob() {
        // tag::flow[]
        Step step3 = createTaskletStep("step 3", ExitStatus.COMPLETED);
        Step step3x = createTaskletStep("step 3x");
        Step step4 = createTaskletStep("step 4");
        Flow flow1 = new FlowBuilder<Flow>("flow1")
                .from(createTaskletStep("step 2a 1"))
                .next(createTaskletStep("step 2a 2"))
                .build();
        Flow flow2 = new FlowBuilder<Flow>("flow2")
                .from(createTaskletStep("step 2b"))
                .build();
        Job job = jobBuilderFactory
                .get("flowJob")
                .start(createTaskletStep("step 1"))
                .split(executor()).add(flow1, flow2)
                .next(step3)
                .on(ExitStatus.COMPLETED.getExitCode()).to(step4)
                .from(step3).on(ExitStatus.FAILED.getExitCode())
                .to(step3x).next(step4)
                .end()
                .build();
        // end::flow[]
        return job;
    }
}
