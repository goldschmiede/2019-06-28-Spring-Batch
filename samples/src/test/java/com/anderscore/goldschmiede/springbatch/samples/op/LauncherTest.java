package com.anderscore.goldschmiede.springbatch.samples.op;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(LauncherConfig.class)
@TestPropertySource(properties = "jdbc.url: jdbc:hsqldb:hsql://localhost:9001/xdb")
public class LauncherTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private JobExplorer jobExplorer;

    @Autowired
    private Job helloJob;

    @Test
    void testLaunchJob() throws JobParametersInvalidException, JobExecutionAlreadyRunningException,
            JobRestartException, JobInstanceAlreadyCompleteException {
        // tag::launch[]
        JobParameters jobParameters = new JobParametersBuilder(jobExplorer)
                .getNextJobParameters(helloJob)
                .toJobParameters();
        JobExecution jobExecution = jobLauncher.run(helloJob, jobParameters);

        System.out.println(jobExecution);
        jobExecution.getStepExecutions().forEach(System.out::println);
        // end::launch[]
    }
}
