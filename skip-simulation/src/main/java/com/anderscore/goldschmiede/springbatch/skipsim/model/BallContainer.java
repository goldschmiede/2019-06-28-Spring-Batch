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
    enum SkipMode {
        NONE, READ, PROCESS, WRITE
    }

    private static final SkipMode skipMode = SkipMode.NONE;
    private static final int delay = 2500;

    private final List<Ball> pool = new ArrayList<>();
    private Optional<Ball> processing = Optional.empty();
    private ItemReader<Ball> reader;
    private final ListItemWriter<Ball> writer = new ListItemWriter<>();
    private List<Ball> tx;

    public static BallContainer createOneInvalid() {
        BallContainer bc = new BallContainer();
        bc.pool.get(6).setInvalid(true);
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
            if (read.isInvalid() && skipMode == SkipMode.READ) {
                throw new InvalidBallException();
            }
            tx.add(read);
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
        if (item != null && item.isInvalid() && skipMode == SkipMode.PROCESS) {
            throw new InvalidBallException();
        }
        TimeUnit.MILLISECONDS.sleep(delay);
        synchronized (this) {
            processing = Optional.empty();
        }
        return item;
    }

    @Override
    public synchronized void write(List<? extends Ball> items) throws Exception {
        log.info("write: {}", items);
        if (skipMode == SkipMode.WRITE && items.stream().filter(Ball::isInvalid).findAny().isPresent()) {
            throw new InvalidBallException();
        }
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
        tx = new ArrayList<>();
    }

    public void commit() {
        log.info("commit transaction");
        tx = null;
    }

    public void rollback() {
        log.info("rollback transaction");
        if (false) {
            tx.forEach(b -> b.setMode(Mode.NEW));
        }
        tx = null;
    }
}
