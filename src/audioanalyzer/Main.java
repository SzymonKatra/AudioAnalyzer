package audioanalyzer;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.*;

public class Main extends Application {

    private static double[] m_fftResult;
    private static final int SAMPLESCOUNT = 8192;

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

        NumberAxis xAxis = new NumberAxis(0, 22050,10);
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
        primaryStage.setScene(scene);
        primaryStage.show();

    }
}