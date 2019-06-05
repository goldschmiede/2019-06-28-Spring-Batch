package com.anderscore.goldschmiede.springbatch.samples.exec;


import org.junit.jupiter.api.Test;
import org.springframework.batch.repeat.RepeatContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.repeat.policy.SimpleCompletionPolicy;
import org.springframework.batch.repeat.support.RepeatTemplate;

public class RepeatTemplateTest {

    @Test
    void test() {
        RepeatTemplate repeatTemplate = new RepeatTemplate();
        repeatTemplate.setCompletionPolicy(new SimpleCompletionPolicy(3));
        repeatTemplate.iterate(this::repeatSimple);
    }

    private RepeatStatus repeatSimple(RepeatContext repeatContext) {
        System.out.println("repeat");
        return RepeatStatus.CONTINUABLE;
    }
}
