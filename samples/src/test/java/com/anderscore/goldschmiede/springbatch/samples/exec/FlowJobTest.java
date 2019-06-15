package com.anderscore.goldschmiede.springbatch.samples.exec;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(FlowJobConfig.class)
public class FlowJobTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job flowJob;

    @Test
    void testFlow() throws Exception {
        jobLauncher.run(flowJob, new JobParameters());
    }

}
