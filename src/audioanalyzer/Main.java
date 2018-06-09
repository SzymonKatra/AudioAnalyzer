package audioanalyzer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Files;

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

        removeOldFiles();

        Parent root = FXMLLoader.load(getClass().getResource("view/MainScene.fxml"));

        Scene scene = new Scene(root, WIDTH, HEIGHT);

        stage.setTitle("Audio Analyzer");
        stage.setScene(scene);
        stage.show();
    }

    private static void removeOldFiles() throws IOException {
        String tmp = System.getProperty("java.io.tmpdir");
        Files.list(new File(tmp).toPath()).forEach(path -> {
            if (path.getFileName().toString().startsWith("audioanalyzer")) {
                new File(path.toString()).delete();
            }
        });
    }
}