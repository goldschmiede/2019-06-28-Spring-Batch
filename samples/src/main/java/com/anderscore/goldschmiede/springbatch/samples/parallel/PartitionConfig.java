package com.anderscore.goldschmiede.springbatch.samples.parallel;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.core.partition.support.DefaultStepExecutionAggregator;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.partition.support.StepExecutionAggregator;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.AbstractItemCountingItemStreamItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Configuration
@EnableBatchProcessing
public class PartitionConfig extends DefaultBatchConfigurer {
    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    /**
     * Dieser Job berechnet die Summe der Zahlen zwischen 1 und 100. Vorgehen: 1. Aufteilen in 20er Pakete und von 5 Threads
     * parallel verarbeiten. 2. Teilergebnisse aggregieren und im ExecutionContext ablegen 3. Weiterer Step gibt Ergebnis
     * aus.
     */
    @Bean
    Job partitionJob() {
        TaskletStep resultStep = stepBuilderFactory.get("showResult").tasklet(showResultTasklet()).build();
        Job job = jobBuilderFactory
                .get("partitionJob")
                .start(partitionStep())
                .next(resultStep)
                .build();
        return job;
    }

    /**
     * Ergebnis ausgeben...
     */
    @Bean
    Tasklet showResultTasklet() {
        return new Tasklet() {

            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                int sum = (Integer) chunkContext.getStepContext().getJobExecutionContext().get("sum");
                System.out.println("Result: " + sum);
                return RepeatStatus.FINISHED;
            }
        };
    }

    /**
     * Ergebnis aus Step-ExecutionContext in den Job-ExecutionContext kopieren
     */
    @Bean
    ExecutionContextPromotionListener promotionListener() {
        var listener = new ExecutionContextPromotionListener();
        listener.setKeys(new String[] { "sum" });
        return listener;
    }

    /**
     * Step, der die Partitionierung für einen anderen Step ausführt
     */
    @Bean
    Step partitionStep() {
        Step partitionStep = stepBuilderFactory.get("partitionStep")
                .listener(promotionListener()) // Summe aus Step-ExecutionContext in Job-ExcecutionContext übernehmen
                .partitioner("stepx", partitioner()) // Präfix für partitionierte Steps
                .step(step()) // Der Step, der von meheren Threads ausgeführt wird
                .gridSize(5) // Anzahl Threads
                .taskExecutor(taskExecutor())
                .aggregator(aggregator())
                .build();
        return partitionStep;
    }

    @Bean
    ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(8);
        return taskExecutor;
    }

    @Bean
    TaskletStep step() {
        TaskletStep step = stepBuilderFactory
                .get("simpleStep")
                .<Integer, Integer>chunk(10)
                .reader(reader()).writer(writer()) // Prozessor ist optional
                .stream(reader()) // Ruft bei Reader open() und close() auf
                .build();
        return step;
    }

    /**
     * Zerlegt die Zahlenreihe 1..100 in grid-size große Teile und legt diese als minValue und maxValue im jeweiligen
     * ExecutionContext ab
     */
    @Bean
    Partitioner partitioner() {
        return new Partitioner() {
            private static final int MIN = 1;
            private static final int MAX = 100;

            @Override
            public Map<String, ExecutionContext> partition(int gridSize) {
                int targetSize = (MAX - MIN) / gridSize + 1;

                Map<String, ExecutionContext> result = new HashMap<String, ExecutionContext>();
                int number = 0;
                int start = MIN;
                int end = start + targetSize - 1;

                while (start <= MAX) {
                    ExecutionContext value = new ExecutionContext();
                    result.put("partition" + number, value);

                    if (end >= MAX) {
                        end = MAX;
                    }
                    value.putInt("minValue", start);
                    value.putInt("maxValue", end);
                    start += targetSize;
                    end += targetSize;
                    number++;
                }

                return result;
            }
        };
    }

    @Bean
    @StepScope // Sicherstellen dass jeder Step sein Reader-Objekt hat
    ItemStreamReader<Integer> reader() {
        return new AbstractItemCountingItemStreamItemReader<Integer>() {
            @Value("#{stepExecutionContext[minValue]}") // Late Binding wg. StepScope
            private int minValue;
            @Value("#{stepExecutionContext[maxValue]}")
            private int maxValue;

            private int current;

            {
                setName("reader");
            }

            @Override
            protected Integer doRead() {
                if (current <= maxValue) {
                    return current++;
                }
                return null;
            }

            @Override
            protected void doOpen() {
                current = minValue;
            }

            @Override
            protected void doClose() {
            }
        };
    }

    static class SumItemWriter implements ItemWriter<Integer> {
        private ExecutionContext ec;

        @BeforeStep
        void beforeStep(StepExecution stepExecution) {
            ec = stepExecution.getExecutionContext();
        }

        @Override
        public void write(List<? extends Integer> items) throws Exception {
            log.info("write: {}", items);
            int sum = ec.getInt("sum", 0);
            for (Integer item : items) {
                sum += item;
            }
            ec.putInt("sum", sum);
        }
    }

    @Bean
    @StepScope
    SumItemWriter writer() { // Darf kein Interface zurückgeben, anderfalls würde @BeforeStep nicht erkannt werden
        return new SumItemWriter();
    }

    @Bean
    StepExecutionAggregator aggregator() {
        return new DefaultStepExecutionAggregator() {
            @Override
            public void aggregate(StepExecution result, Collection<StepExecution> executions) {
                super.aggregate(result, executions);
                if (executions == null) {
                    return;
                }
                int total = 0;
                for (StepExecution stepExecution : executions) {
                    int sum = stepExecution.getExecutionContext().getInt("sum");
                    total += sum;
                }
                result.getExecutionContext().putInt("sum", total);
                log.info("total: {}", total);
            }
        };
    }
}
