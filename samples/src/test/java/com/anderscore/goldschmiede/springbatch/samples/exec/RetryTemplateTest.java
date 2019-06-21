package com.anderscore.goldschmiede.springbatch.samples.exec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.FileNotFoundException;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.retry.RetryContext;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class RetryTemplateTest {

    @Test
    public void test() {
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(new SimpleRetryPolicy(10, Collections.singletonMap(FileNotFoundException.class, true)));
        retryTemplate.setBackOffPolicy(new ExponentialBackOffPolicy());
        assertThrows(FileNotFoundException.class, () -> retryTemplate.execute(this::action));
    }

    private Integer action(RetryContext retryContext) throws FileNotFoundException {
        log.info("action");
        throw new FileNotFoundException("test.txt");
    }

    @Test
    public void testWithRecovery() throws FileNotFoundException {
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(new SimpleRetryPolicy(10, Collections.singletonMap(FileNotFoundException.class, true)));
        retryTemplate.setBackOffPolicy(new ExponentialBackOffPolicy());
        assertThat(retryTemplate.execute(this::action, this::recover)).isEqualTo(42);
    }

    private Integer recover(RetryContext retryContext) {
        return 42;
    }

    @Test
    public void testSkipAfterRetry() {
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(new SimpleRetryPolicy(10, Collections.singletonMap(FileNotFoundException.class, true)));
        retryTemplate.setBackOffPolicy(new ExponentialBackOffPolicy());
        Executable executable = () -> retryTemplate.execute(this::action, this::recoverBySkip);
        assertThrows(IllegalArgumentException.class, executable);
    }

    private Integer recoverBySkip(RetryContext retryContext) {
        throw new IllegalArgumentException();
    }
}
