package com.anderscore.goldschmiede.springbatch.samples.op;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(OperatorConfig.class)
@TestPropertySource(properties = "jdbc.url: jdbc:hsqldb:hsql://localhost:9001/xdb")
public class OperatorTest {

    // tag::operator[]
    @Autowired
    private JobOperator jobOperator;

    @Test
    void testJobOperator() throws Exception {
        Long executionId = jobOperator.startNextInstance("helloJob");
        System.out.println(jobOperator.getSummary(executionId));
        System.out.println(jobOperator.getStepExecutionSummaries(executionId));
    }
    // end::operator[]
}
