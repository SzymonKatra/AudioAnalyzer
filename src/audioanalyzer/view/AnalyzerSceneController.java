package audioanalyzer.view;

import audioanalyzer.logic.*;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.io.*;
import java.util.Random;

public class AnalyzerSceneController {
    private final int WIDTH = 1000;

    @FXML
    private Canvas canvas;

    private AudioStreamInfo m_stream;
    private IPCMPlayer m_player;
    private ISignalAnalyzer m_analyzer;
    private RandomAccessFile m_currentFile;
    private SampleReaderHelper m_reader;
    private SpectrumPlotter m_spectrumPlotter;
    private WaveformPlotter m_waveformPlotter;
    private AsyncAudioPlayer m_asyncPlayer;

    private GraphicsContext m_gfx;
    private PixelWriter m_pixelGfx;
    private Timeline m_timeline;

    private double m_audioPosition;
    private int m_samplesToAnalyze;
    private double m_analyzeWidth;

    @FXML
    public void initialize() {
        m_gfx = canvas.getGraphicsContext2D();
        m_pixelGfx = m_gfx.getPixelWriter();

        m_spectrumPlotter = new SpectrumPlotter(m_gfx, new Rectangle2D(0, 0, 999, 500));
    }

    @FXML
    private void canvasMousePressed(MouseEvent event) throws IOException {
        if (!event.isPrimaryButtonDown()) return;

        m_timeline.stop();
        m_asyncPlayer.stop();

        tryChangePosition(event);
    }

    @FXML
    private void canvasMouseReleased(MouseEvent event) throws IOException {
        if (event.getButton() != MouseButton.PRIMARY) return;

        m_timeline.play();
        m_asyncPlayer.start();
    }

    @FXML
    private void canvasMouseDragged(MouseEvent event) throws IOException {
        if (!event.isPrimaryButtonDown()) return;

        tryChangePosition(event);
    }

    private void tryChangePosition(MouseEvent event) throws IOException {
        double y = event.getY();
        if (y > 500 && y < 600) {
            double x = event.getX();
            m_audioPosition = x / WIDTH;

            m_waveformPlotter.plot(m_audioPosition, m_analyzeWidth);
            drawSpectrum();
        }
    }

    public void setStream(AudioStreamInfo stream) throws IOException {
        m_stream = stream;
        m_player = new PCMPlayer(stream.getSampleRate());
        m_analyzer = new FFTAnalyzer();

        m_samplesToAnalyze = 16384;//m_stream.getSampleRate();
        m_analyzeWidth = ((double) m_samplesToAnalyze / (double) m_stream.getTotalSamplesCount());

        m_currentFile = new RandomAccessFile(m_stream.getRawFilePaths().get(0), "r");
        m_reader = new SampleReaderHelper(m_currentFile, true);

        m_spectrumPlotter.setFrequencyRange(m_stream.getSampleRate() / 2);
        m_waveformPlotter = new WaveformPlotter(m_gfx, new Rectangle2D(0, 520, 1000, 100), m_stream, m_reader);
        m_player = new PCMPlayer(m_stream.getSampleRate());
        m_audioPosition = 0;
        m_asyncPlayer = new AsyncAudioPlayer(m_player, m_reader, m_stream);
        m_asyncPlayer.setPosition(m_audioPosition);
        m_asyncPlayer.start();

        m_waveformPlotter.plot(m_audioPosition, m_analyzeWidth);
        drawSpectrum();
        m_spectrumPlotter.drawAxesDescription();
        m_waveformPlotter.drawTimeline();

        m_timeline = new Timeline(new KeyFrame(
                Duration.millis(50),
                ae -> {
                    m_audioPosition += (0.05 / m_stream.getDuration());
                    if (m_audioPosition > 1) m_audioPosition = 1;
                    try {
                        long startTime, stopTime;
                        startTime = System.currentTimeMillis();
                        drawSpectrum();
                        stopTime = System.currentTimeMillis();
                        System.out.println("Spectrum: " + (stopTime - startTime));
                        startTime = System.currentTimeMillis();
                        m_waveformPlotter.plot(m_audioPosition, m_analyzeWidth);
                        stopTime = System.currentTimeMillis();
                        System.out.println("Waveform: " + (stopTime - startTime));
                        startTime = System.currentTimeMillis();
                        m_asyncPlayer.setPosition(m_audioPosition);
                        stopTime = System.currentTimeMillis();
                        System.out.println("Player: " + (stopTime - startTime));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }));
        m_timeline.setCycleCount(Animation.INDEFINITE);
        m_timeline.play();
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

        long startTime, stopTime;
        startTime = System.currentTimeMillis();
        m_analyzer.analyze(samples);
        stopTime = System.currentTimeMillis();
        System.out.println("Spectrum analyze: " + (stopTime - startTime));
        startTime = System.currentTimeMillis();
        m_spectrumPlotter.plot(m_analyzer.getAmplitudes());
        stopTime = System.currentTimeMillis();
        System.out.println("Spectrum plot: " + (stopTime - startTime));
    }
}
