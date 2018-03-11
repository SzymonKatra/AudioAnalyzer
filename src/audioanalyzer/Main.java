package audioanalyzer;

import audioanalyzer.logic.FFTAnalyzer;
import audioanalyzer.logic.ISignalAnalyzer;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    private static Stage s_stage;
    private static Main s_main;

    public static Object changeView(String fxml) throws IOException {
        FXMLLoader loader = new FXMLLoader(s_main.getClass().getResource(fxml));

        Parent root = loader.load();

        Scene scene = new Scene(root, 800, 600);

        s_stage.setScene(scene);

        return loader.getController();
    }

    @Override
    public void start(Stage stage) throws IOException {
        s_stage = stage;
        s_main = this;

        Parent root = FXMLLoader.load(getClass().getResource("view/MainScene.fxml"));

        Scene scene = new Scene(root, 800, 600);

        stage.setTitle("Audio Analyzer");
        stage.setScene(scene);
        stage.show();

        /*Canvas canvas = new Canvas(800, 600);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(Color.RED);
        double prevX = 0, prevY = 0;
        double[] xPoints = new double[m_fftResult.length + 1];
        double[] yPoints = new double[m_fftResult.length + 1];
        for (int i = 0; i < m_fftResult.length; i++)
        {
            double x = (i * (44100.0 / SAMPLESCOUNT)) / 22050 * 800;
            double y = 600 - (m_fftResult[i] / 0.5 * 600);
            //gc.strokeLine(prevX, prevY, x, y);
            xPoints[i] = x;
            yPoints[i] = y;
            prevX = x;
            prevY = y;
        }
        xPoints[xPoints.length - 1] = 0;
        yPoints[yPoints.length - 1] = 600;
        gc.fillPolygon(xPoints, yPoints, m_fftResult.length + 1);

        Pane root = new Pane();
        root.getChildren().add(canvas);
        Scene scene = new Scene(root);


        stage.setScene(scene);
        stage.setTitle("FFT");
        stage.show();*/

    }
}