package com.anderscore.goldschmiede.springbatch.samples.exec;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

// tag::code[]
@SpringJUnitConfig(HelloJobConfig.class)
public class HelloJobTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job helloJob;

    // end::code[]

    @Autowired
    private Job goodByeJob;

    // tag::code[]
    @Test
    void testHello() throws JobParametersInvalidException, JobExecutionAlreadyRunningException,
            JobRestartException, JobInstanceAlreadyCompleteException {
        jobLauncher.run(helloJob, new JobParameters());
    }
    // end::code[]

    @Test
    void testGoodBye() throws JobParametersInvalidException, JobExecutionAlreadyRunningException,
            JobRestartException, JobInstanceAlreadyCompleteException {
        jobLauncher.run(goodByeJob, new JobParameters());
    }

    // tag::code[]
}
// end::code[]
