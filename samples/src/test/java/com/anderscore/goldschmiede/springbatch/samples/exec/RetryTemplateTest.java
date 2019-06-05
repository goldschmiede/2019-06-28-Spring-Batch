package com.anderscore.goldschmiede.springbatch.samples.exec;

import java.io.FileNotFoundException;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.retry.RetryContext;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

public class RetryTemplateTest {

    @Test
    public void test() throws FileNotFoundException {
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(new SimpleRetryPolicy(3, Collections.singletonMap(FileNotFoundException.class, true)));
        Integer execute = retryTemplate.execute(this::action);
    }

    private Integer action(RetryContext retryContext) throws FileNotFoundException {
        System.out.println("action");
        throw new FileNotFoundException("test.txt");
    }
}
