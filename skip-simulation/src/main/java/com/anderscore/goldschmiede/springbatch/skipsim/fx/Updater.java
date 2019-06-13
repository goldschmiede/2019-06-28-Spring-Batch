package com.anderscore.goldschmiede.springbatch.skipsim.fx;

import org.springframework.batch.core.ItemProcessListener;
import org.springframework.batch.core.annotation.AfterChunk;

import com.anderscore.goldschmiede.springbatch.skipsim.model.Ball;

public class Updater implements ItemProcessListener<Ball, Ball> {
    private ResizableCanvas canvas;

    public ResizableCanvas getCanvas() {
        return canvas;
    }

    public void setCanvas(ResizableCanvas canvas) {
        this.canvas = canvas;
    }

    @Override
    public void beforeProcess(Ball item) {
        canvas.update();
    }

    @Override
    public void afterProcess(Ball item, Ball result) {
        canvas.update();
    }

    @Override
    public void onProcessError(Ball item, Exception e) {
        canvas.update();
    }

    @AfterChunk
    public void afterChunk() throws Exception {
        canvas.update();
    }
}
