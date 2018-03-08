package audioanalyzer;

import audioanalyzer.logic.FFTAnalyzer;
import audioanalyzer.logic.ISignalAnalyzer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;

public class Main extends Application {

    private static double[] m_fftResult;
    private static final int SAMPLESCOUNT = 512;

    public static void main(String[] args) {
        double[] samples = new double[SAMPLESCOUNT];
        try {
            DataInputStream stream = new DataInputStream(new FileInputStream("C:\\Users\\Szymon\\Desktop\\aud2.raw"));
            for (int i = 0; i < samples.length; i++) {
                samples[i] = stream.readDouble();
            }

        }
        catch (FileNotFoundException e)
        {

        } catch (IOException e) {
            e.printStackTrace();
        }
        double[] result = new double[samples.length / 2];
        m_fftResult = result;
        ISignalAnalyzer fft = new FFTAnalyzer();
        fft.computeAmplitudes(samples, result);

        /*for (int i = 0; i < result.length; i++) {
            //System.out.println(Integer.toString(i) + " Hz - " + result[i]);
            System.out.printf("%.20f", result[i]);
            System.out.println();
        }*/
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
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