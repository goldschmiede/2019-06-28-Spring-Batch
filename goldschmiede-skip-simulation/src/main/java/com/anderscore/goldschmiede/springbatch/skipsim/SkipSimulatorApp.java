package com.anderscore.goldschmiede.springbatch.skipsim;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.anderscore.goldschmiede.springbatch.skipsim.fx.ResizableCanvas;
import com.anderscore.goldschmiede.springbatch.skipsim.fx.Updater;
import com.anderscore.goldschmiede.springbatch.skipsim.job.JobConfig;
import com.anderscore.goldschmiede.springbatch.skipsim.model.BallContainer;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j2;

/**
 * Run with: --module-path ${PATH_TO_FX} --add-modules
 * javafx.controls,javafx.fxml
 */
@Log4j2
public class SkipSimulatorApp extends Application {
    @Autowired
    private BallContainer container;

    @Autowired
    private JobLauncher launcher;

    @Autowired
    private Job skipSimulationJob;

    @Autowired
    private Updater updater;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() {
        var ctx = new AnnotationConfigApplicationContext(JobConfig.class);
        ctx.getAutowireCapableBeanFactory().autowireBean(this);
    }

    @Override
    public void start(Stage primaryStage) throws JobExecutionAlreadyRunningException, JobRestartException,
            JobInstanceAlreadyCompleteException, JobParametersInvalidException {
        ResizableCanvas canvas = new ResizableCanvas(container);
        updater.setCanvas(canvas);

        Group root = new Group();
        root.getChildren().add(canvas);
        Scene scene = new Scene(root, 800, 400, Color.BLACK);
        canvas.widthProperty().bind(scene.widthProperty());
        canvas.heightProperty().bind(scene.heightProperty().subtract(100));

        Button button = new Button("Start");
        root.getChildren().add(button);
        button.setLayoutX(10);
        button.layoutYProperty().bind(canvas.heightProperty().add(20));
        button.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                runJob();
            }
        });

        primaryStage.setTitle("Skip Simulator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void runJob() {
        JobParameters jobParameters = new JobParametersBuilder().toJobParameters();
        try {
            launcher.run(skipSimulationJob, jobParameters);
        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
                | JobParametersInvalidException ex) {
            log.error("Unable to start Job", ex);
        }
    }
}
