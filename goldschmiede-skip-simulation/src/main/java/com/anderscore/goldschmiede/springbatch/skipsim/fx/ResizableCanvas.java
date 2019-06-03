package com.anderscore.goldschmiede.springbatch.skipsim.fx;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ResizableCanvas extends Canvas {
    private final SimulationModel simulationModel;

    public ResizableCanvas(SimulationModel simulationModel) {
        this.simulationModel = simulationModel;

        // Redraw canvas when size changes.
        widthProperty().addListener(evt -> draw());
        heightProperty().addListener(evt -> draw());
    }

    private void draw() {
        log.info("draw");
        double width = getWidth();
        double height = getHeight();

        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);

        var renderer = new Renderer(width, height, gc);
        simulationModel.render(renderer);
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    public void update() {
        log.info("update");
        Platform.runLater(this::draw);
    }
}
