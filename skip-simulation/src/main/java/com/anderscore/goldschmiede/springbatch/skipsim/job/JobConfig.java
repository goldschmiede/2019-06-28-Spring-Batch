package com.anderscore.goldschmiede.springbatch.skipsim.job;

import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.RegexpMethodPointcutAdvisor;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import com.anderscore.goldschmiede.springbatch.skipsim.fx.Updater;
import com.anderscore.goldschmiede.springbatch.skipsim.model.Ball;
import com.anderscore.goldschmiede.springbatch.skipsim.model.BallContainer;
import com.anderscore.goldschmiede.springbatch.skipsim.model.InvalidBallException;

import lombok.extern.log4j.Log4j2;

@Configuration
@EnableBatchProcessing
@Log4j2
public class JobConfig extends DefaultBatchConfigurer {
    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Bean
    Updater updater() {
        return new Updater();
    }

    @Bean
    BallContainer ballContainer() {
        return BallContainer.createOneInvalid();
    }

    @Bean
    TaskletStep skipSimulationStep() {
        return stepBuilderFactory.get("skip-simulation-step").listener(logListener()).<Ball, Ball>chunk(4)
                .reader(ballContainer()).processor(ballContainer()).writer(ballContainer()).listener(updater())
                .faultTolerant().skip(InvalidBallException.class).skipLimit(3).build();
    }

    @Bean
    StepExecutionListener logListener() {
        return new StepExecutionListener() {

            @Override
            public void beforeStep(StepExecution stepExecution) {
                log.info("Starting Step " + stepExecution.getStepName());
            }

            @Override
            public ExitStatus afterStep(StepExecution stepExecution) {
                log.info("Completed Step " + stepExecution);
                return stepExecution.getExitStatus();
            }
        };
    }

    @Bean
    TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExectutor = new ThreadPoolTaskExecutor();
        return taskExectutor;
    }

    @Bean
    Job skipSimulationJob() {
        return jobBuilderFactory.get("skip-simulation-job").start(skipSimulationStep()).build();
    }

    @Override
    protected JobLauncher createJobLauncher() throws Exception {
        SimpleJobLauncher jobLauncher = (SimpleJobLauncher) super.createJobLauncher();
        jobLauncher.setTaskExecutor(taskExecutor());
        return jobLauncher;
    }

    @Override
    public PlatformTransactionManager getTransactionManager() {
        ResourcelessTransactionManager txManager = new ResourcelessTransactionManager();
        BallContainer ballContainer = ballContainer();
        ProxyFactory proxyFactory = new ProxyFactory(txManager);
        proxyFactory.addAdvisor(new RegexpMethodPointcutAdvisor(".*\\.getTransaction",
                (MethodBeforeAdvice) (method, args, target) -> ballContainer.begin()));
        proxyFactory.addAdvisor(new RegexpMethodPointcutAdvisor(".*\\.commit",
                (MethodBeforeAdvice) (method, args, target) -> ballContainer.commit()));
        proxyFactory.addAdvisor(new RegexpMethodPointcutAdvisor(".*\\.rollback",
                (MethodBeforeAdvice) (method, args, target) -> ballContainer.rollback()));
        Object wrapped = proxyFactory.getProxy();
        return (PlatformTransactionManager) wrapped;
    }
}
