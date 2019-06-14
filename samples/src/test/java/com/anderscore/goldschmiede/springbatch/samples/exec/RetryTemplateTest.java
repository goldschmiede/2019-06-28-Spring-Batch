package com.anderscore.goldschmiede.springbatch.samples.exec;

import java.io.FileNotFoundException;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.retry.RetryContext;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class RetryTemplateTest {

    @Test
    public void test() throws FileNotFoundException {
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(new SimpleRetryPolicy(10, Collections.singletonMap(FileNotFoundException.class, true)));
        retryTemplate.setBackOffPolicy(new ExponentialBackOffPolicy());
        Integer execute = retryTemplate.execute(this::action);
    }

    private Integer action(RetryContext retryContext) throws FileNotFoundException {
        log.info("action");
        throw new FileNotFoundException("test.txt");
    }
}
