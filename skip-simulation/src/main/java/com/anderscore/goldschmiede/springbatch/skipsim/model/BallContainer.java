package com.anderscore.goldschmiede.springbatch.skipsim.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.IteratorItemReader;
import org.springframework.batch.item.support.ListItemWriter;

import com.anderscore.goldschmiede.springbatch.skipsim.fx.Renderer;
import com.anderscore.goldschmiede.springbatch.skipsim.fx.SimulationModel;
import com.anderscore.goldschmiede.springbatch.skipsim.model.Ball.Mode;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class BallContainer implements ItemReader<Ball>, ItemWriter<Ball>, ItemProcessor<Ball, Ball>, SimulationModel {
    private final List<Ball> pool = new ArrayList<>();
    private Optional<Ball> processing = Optional.empty();
    private ItemReader<Ball> reader;
    private final ListItemWriter<Ball> writer = new ListItemWriter<>();

    public static BallContainer createOneInvalid() {
        BallContainer bc = new BallContainer();
        bc.pool.get(2).setInvalid(true);
        return bc;
    }

    private BallContainer() {
        for (int no = 0; no < 10; no++) {
            Ball ball = new Ball();
            ball.setNo(no);
            pool.add(ball);
        }
    }

    @Override
    public synchronized Ball read() throws Exception {
        if (reader == null) {
            reader = new IteratorItemReader<>(pool);
        }
        Ball read = reader.read();
        log.info("read: {}", read);
        if (read != null) {
            read.setMode(Mode.READ);
        }
        return read;
    }

    @Override
    public Ball process(Ball item) throws Exception {
        log.info("process: {}", item);
        synchronized (this) {
            processing = Optional.ofNullable(item);
        }
        TimeUnit.SECONDS.sleep(1);
        synchronized (this) {
            processing = Optional.empty();
        }
        return item;
    }

    @Override
    public synchronized void write(List<? extends Ball> items) throws Exception {
        log.info("write: {}", items);
        writer.write(items);
    }

    @Override
    public synchronized void render(Renderer renderer) {
        pool.forEach(renderer::renderToRead);
        processing.ifPresent(renderer::renderProcessing);
        writer.getWrittenItems().forEach(renderer::renderWritten);
    }

    public void begin() {
        log.info("begin transaction");
    }

    public void commit() {
        log.info("commit transaction");
    }

    public void rollback() {
        log.info("rollback transaction");
    }
}
