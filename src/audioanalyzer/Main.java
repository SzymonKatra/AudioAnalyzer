package audioanalyzer;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.*;

public class Main extends Application {

    private static double[] m_fftResult;
    private static final int SAMPLESCOUNT = 512;

    public static void main(String[] args) {
        double[] samples = new double[SAMPLESCOUNT];
        try {
            FileReader reader = new FileReader("C:\\Users\\Szymon\\sample-data-sines.txt");
            BufferedReader buff = new BufferedReader(reader);
            for (int i = 0; i < samples.length; i++) {
                String line = buff.readLine();
                samples[i] = Double.parseDouble(line);
            }

        }
        catch (FileNotFoundException e)
        {

        } catch (IOException e) {
            e.printStackTrace();
        }
        double[] result = new double[samples.length / 2];
        m_fftResult = result;
        FourierTransform fft = new FourierTransform();
        fft.computeHarmonics(samples, result);

        /*for (int i = 0; i < result.length; i++) {
            //System.out.println(Integer.toString(i) + " Hz - " + result[i]);
            System.out.printf("%.20f", result[i]);
            System.out.println();
        }*/
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Hello World!");

        /*NumberAxis xAxis = new NumberAxis(0, 22050,10);
        NumberAxis yAxis = new NumberAxis(0, 0.1, 0.0001);
        AreaChart<Number, Number> chart = new AreaChart<Number, Number>(xAxis, yAxis);

        XYChart.Data data = new XYChart.Data(0,0);
        Rectangle rect = new Rectangle(0, 0);
        rect.setVisible(false);
        data.setNode(rect);

        XYChart.Series series = new XYChart.Series();
        series.setName("fft");

        for (int i = 0; i < m_fftResult.length; i += 10)
        {
            series.getData().add(new XYChart.Data(i * (44100.0 / SAMPLESCOUNT), m_fftResult[i]));
        }

        Scene scene = new Scene(chart, 800, 600);
        chart.getData().addAll(series);
        primaryStage.setScene(scene);*/

        Canvas canvas = new Canvas(800, 600);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(Color.RED);
        double prevX = 0, prevY = 0;
        for (int i = 0; i < m_fftResult.length; i++)
        {
            double x = (i * (44100.0 / SAMPLESCOUNT)) / 22050 * 800;
            double y = 600 - (m_fftResult[i] * 600);
            gc.strokeLine(prevX, prevY, x, y);

            prevX = x;
            prevY = y;
        }

        Pane root = new Pane();
        root.getChildren().add(canvas);
        Scene scene = new Scene(root);


        primaryStage.setScene(scene);
        primaryStage.setTitle("FFT");
        primaryStage.show();

    }
}