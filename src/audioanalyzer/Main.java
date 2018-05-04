package audioanalyzer;

import audioanalyzer.logic.FFTAnalyzer;
import audioanalyzer.logic.ISignalAnalyzer;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.stage.Stage;

import java.io.*;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    private static Stage s_stage;
    private static Main s_main;

    private static final int WIDTH = 1000;
    private static final int HEIGHT = 700;

    public static Object changeView(String fxml) throws IOException {
        FXMLLoader loader = new FXMLLoader(s_main.getClass().getResource(fxml));

        Parent root = loader.load();

        Scene scene = new Scene(root, WIDTH, HEIGHT);

        s_stage.setScene(scene);

        return loader.getController();
    }

    @Override
    public void start(Stage stage) throws IOException {
        s_stage = stage;
        s_main = this;

        Parent root = FXMLLoader.load(getClass().getResource("view/MainScene.fxml"));

        Scene scene = new Scene(root, WIDTH, HEIGHT);

        stage.setTitle("Audio Analyzer");
        stage.setScene(scene);
        stage.show();

    }
}