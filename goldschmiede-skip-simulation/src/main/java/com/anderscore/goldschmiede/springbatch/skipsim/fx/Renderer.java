package com.anderscore.goldschmiede.springbatch.skipsim.fx;

import com.anderscore.goldschmiede.springbatch.skipsim.model.Ball;
import com.anderscore.goldschmiede.springbatch.skipsim.model.Ball.Mode;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Renderer {
    private final double width;
    private final double height;
    private final GraphicsContext gc;

    public Renderer(double width, double height, GraphicsContext gc) {
        super();
        this.width = width;
        this.height = height;
        this.gc = gc;
    }

    private void drawBall(int index, int row, Color color) {
        gc.setFill(color);
        double sz = Math.min((width - 20) / 10, (height - 20) / 3);
        double x = index * sz;
        double y = row * sz;

        gc.fillOval(10 + x, 10 + y, sz - 5, sz - 5);
    }

    public void renderToRead(Ball ball) {
        if (ball.getMode() == Mode.NEW) {
            drawBall(ball.getNo(), 0, color(ball));
        }
    }

    public void renderProcessing(Ball ball) {
        drawBall(ball.getNo(), 1, color(ball));
    }

    public void renderWritten(Ball ball) {
        drawBall(ball.getNo(), 2, color(ball));
    }

    private Color color(Ball ball) {
        return ball.isInvalid() ? Color.ROYALBLUE : Color.GOLD;
    }
}
