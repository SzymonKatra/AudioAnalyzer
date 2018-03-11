package audioanalyzer.view;

import audioanalyzer.logic.*;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.io.*;
import java.util.Random;

public class AnalyzerSceneController {
    private final int WIDTH = 800;

    @FXML
    private Canvas canvas;

    private AudioStreamInfo m_stream;
    private IPCMPlayer m_player;
    private ISignalAnalyzer m_analyzer;
    private RandomAccessFile m_currentFile;
    private SampleReaderHelper m_reader;

    private GraphicsContext m_gfx;
    private PixelWriter m_pixelGfx;

    private double m_audioPosition;
    private int m_samplesToAnalyze;
    private double m_analyzeWidth;

    private double[] m_audioPreviewBuffer;

    @FXML
    public void initialize() {
        m_gfx = canvas.getGraphicsContext2D();
        m_pixelGfx = m_gfx.getPixelWriter();

        m_audioPosition = 0.1;
    }

    @FXML
    private void canvasMouse(MouseEvent event) throws IOException {
        if (!event.isPrimaryButtonDown()) return;

        double y = event.getY();
        if (y > 500 && y < 600) {
            double x = event.getX();
            m_audioPosition = x / WIDTH;

            drawWaveform();
            drawSpectrum();
        }
    }

    public void setStream(AudioStreamInfo stream) throws IOException {
        m_stream = stream;
        m_player = new PCMPlayer(stream.getSampleRate());
        m_analyzer = new FFTAnalyzer();

        m_samplesToAnalyze = 512;//m_stream.getSampleRate();
        m_analyzeWidth = ((double)m_samplesToAnalyze / (double)m_stream.getTotalSamplesCount());

        m_currentFile = new RandomAccessFile(m_stream.getRawFilePaths().get(0), "r");
        m_reader = new SampleReaderHelper(m_currentFile);

        drawWaveform();
        drawSpectrum();

        Timeline timeline = new Timeline(new KeyFrame(
                Duration.millis(30),
                ae -> {
                    double beginAll = System.nanoTime();
                    m_audioPosition+= (0.03 / m_stream.getDuration());
                if (m_audioPosition > 1) m_audioPosition = 1;
                    /*try {
                        drawWaveform();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
                    try {
                        drawSpectrum();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    double endAll = System.nanoTime();
                    System.out.println((endAll - beginAll) / 1000000);
                }));
        timeline.setCycleCount(Animation.INDEFINITE);
        //timeline.play();
    }

    private void drawWaveform() throws IOException {
        m_gfx.clearRect(0,500, WIDTH, 100);

        int sampleDistance = (int)(m_stream.getTotalSamplesCount() / WIDTH);
        //byte[] buffer = new byte[sampleDistance * 8];
        double[] buffer = new double[sampleDistance];

        for (int i = 0; i < WIDTH; i++) {
            //m_currentFile.seek(i * sampleDistance * 8);
            //m_currentFile.read(buffer);
            m_reader.readSamples(i * sampleDistance, sampleDistance, buffer, 0);
            double maxPositive = 0;
            double maxNegative = 0;
            double avgPositive = 0;
            double avgNegative = 0;
            for (int j = 0; j < sampleDistance; ++j) {
                //long x = (Byte.toUnsignedLong(buffer[j * 8]) << 56) | (Byte.toUnsignedLong(buffer[j * 8 + 1]) << 48) |
                //         (Byte.toUnsignedLong(buffer[j * 8 + 2]) << 40) | (Byte.toUnsignedLong(buffer[j * 8 + 3]) << 32) |
                //         (Byte.toUnsignedLong(buffer[j * 8 + 4]) << 24) | (Byte.toUnsignedLong(buffer[j * 8 + 5]) << 16) |
                //         (Byte.toUnsignedLong(buffer[j * 8 + 6]) << 8) | (Byte.toUnsignedLong(buffer[j * 8 + 7]));
                //double sample = Double.longBitsToDouble(x);
                double sample = buffer[j];
                if (sample > 0)
                {
                    if (sample > maxPositive) maxPositive = sample;
                    avgPositive += sample;
                }
                else
                {
                    if (sample < maxNegative) maxNegative = sample;
                    avgNegative += sample;
                }
            }

            avgPositive /= (double)sampleDistance;
            avgNegative /= (double)sampleDistance;

            double m1 = 550 - 50 * maxPositive;
            double m2 = 550 - 50 * maxNegative;
            double a1 = 550 - 50 * avgPositive;
            double a2 = 550 - 50 * avgNegative;

            for (int y = (int)m1; y <= (int)m2; y++) m_pixelGfx.setColor(i, y, Color.BLUE);
            for (int y = (int)a1; y <= (int)a2; y++) m_pixelGfx.setColor(i, y, Color.CORNFLOWERBLUE);
        }

        m_gfx.setFill(new Color(0, 0, 0, 0.75));
        m_gfx.fillRect(m_audioPosition * WIDTH - m_analyzeWidth * WIDTH / 2, 500, m_analyzeWidth * WIDTH, 100);
    }
    private void drawSpectrum() throws IOException {
        double begin = Math.max(0, m_audioPosition - m_analyzeWidth / 2);
        double end = Math.min(1, m_audioPosition + m_analyzeWidth / 2);

        int beginSample = (int) Math.round(begin * m_stream.getTotalSamplesCount());
        int endSample = (int) Math.round(end * m_stream.getTotalSamplesCount());

        int count = endSample - beginSample;
        if (count % 2 != 0) count++;

        double[] samples = new double[count];
        m_reader.readSamples(beginSample, count, samples, 0);

        m_analyzer.analyze(samples);

        double[] result = m_analyzer.getAmplitudes();

        m_gfx.clearRect(0,0, WIDTH,500);
        m_gfx.setFill(Color.RED);
        double[] xPoints = new double[result.length + 1];
        double[] yPoints = new double[result.length + 1];
        for (int i = 0; i < result.length; i++)
        {
            double x = ((double)i * (m_stream.getSampleRate() / (double)count)) / (double)(m_stream.getSampleRate() / 2) * (double)WIDTH;
            double y = 500 - (result[i] * 500);
            xPoints[i] = x;
            yPoints[i] = y;
        }
        xPoints[xPoints.length - 1] = 0;
        yPoints[yPoints.length - 1] = 500;
        m_gfx.fillPolygon(xPoints, yPoints, result.length + 1);
    }
}
