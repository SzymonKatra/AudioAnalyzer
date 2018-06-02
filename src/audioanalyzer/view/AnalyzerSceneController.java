package audioanalyzer.view;

import audioanalyzer.logic.*;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.PixelWriter;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.io.*;
import java.util.Random;

public class AnalyzerSceneController {
    private class ComboChannelItem {
        private int m_channelNumber;
        public ComboChannelItem(int channelNumber) {
            m_channelNumber = channelNumber;
        }

        public int getChannelNumber() {
            return m_channelNumber;
        }
        public String getChannelName() {
            return "Channel #" + (m_channelNumber + 1);
        }
    }

    private final int WIDTH = 1000;

    @FXML
    private Canvas canvas;
    @FXML
    private ComboBox channelCombo;
    @FXML
    private ComboBox analyzeWidthCombo;
    @FXML
    private Label refreshRateLabel;
    @FXML
    private Button playPauseButton;
    @FXML
    private ScrollBar smoothnessScrollBar;

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
    private int m_refreshRate = 50;
    private int m_diffStartTime = 0;
    private int m_diffPeak = 0;
    private boolean m_playing = false;

    @FXML
    public void initialize() {
        m_gfx = canvas.getGraphicsContext2D();
        m_pixelGfx = m_gfx.getPixelWriter();

        m_spectrumPlotter = new SpectrumPlotter(m_gfx, new Rectangle2D(0, 0, 999, 500));

        refreshRateLabel.setText("Refresh rate: " + m_refreshRate + " ms");
    }

    @FXML
    private void canvasMousePressed(MouseEvent event) throws IOException {
        if (!event.isPrimaryButtonDown()) return;

        if (m_playing) {
            m_timeline.stop();
            m_asyncPlayer.stop();
        }

        tryChangePosition(event);
    }

    @FXML
    private void canvasMouseReleased(MouseEvent event) throws IOException {
        if (event.getButton() != MouseButton.PRIMARY) return;

        if (m_playing) {
            m_timeline.play();
            m_asyncPlayer.start();
        }
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

    @FXML
    public void channelChanged() throws IOException {
        int channel = ((ComboChannelItem)channelCombo.getValue()).getChannelNumber();

        if (m_currentFile != null) m_currentFile.close();
        if (m_asyncPlayer != null) m_asyncPlayer.dispose();
        if (m_timeline != null) m_timeline.stop();

        m_currentFile = new RandomAccessFile(m_stream.getRawFilePaths().get(channel), "r");
        m_reader = new SampleReaderHelper(m_currentFile, false);

        m_waveformPlotter = new WaveformPlotter(m_gfx, new Rectangle2D(0, 520, 1000, 100), m_stream, m_reader);
        m_player = new PCMPlayer(m_stream.getSampleRate());

        m_asyncPlayer = new AsyncAudioPlayer(m_player, m_reader, m_stream);
        m_asyncPlayer.setPosition(m_audioPosition);
        if (m_playing) {
            m_asyncPlayer.start();
        }

        m_waveformPlotter.plot(m_audioPosition, m_analyzeWidth);
        drawSpectrum();

        m_waveformPlotter.drawTimeline();

        m_timeline = new Timeline();
        m_timeline.setCycleCount(Animation.INDEFINITE);
        updateKeyFrame();
    }

    @FXML
    public void playPause() {
        m_playing = !m_playing;

        if (m_playing) {
            playPauseButton.setText("Pause");
            m_timeline.play();
            m_asyncPlayer.start();
        }
        else {
            playPauseButton.setText("Play");
            m_timeline.stop();
            m_asyncPlayer.stop();
        }
    }

    private void updateKeyFrame() {
        if (m_playing) {
            m_timeline.stop();
        }
        m_timeline.getKeyFrames().clear();
        m_timeline.getKeyFrames().add(new KeyFrame(
                Duration.millis(m_refreshRate), new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {
                refreshView();
            }
        }));
        if (m_playing) {
            m_timeline.play();
        }
    }

    private void refreshView() {
        m_audioPosition += (((double) m_refreshRate / 1000.0) / m_stream.getDuration());
        if (m_audioPosition > 1) m_audioPosition = 1;
        try {
            long startTime, stopTime;
            startTime = System.currentTimeMillis();

            drawSpectrum();
            m_waveformPlotter.plot(m_audioPosition, m_analyzeWidth);
            m_asyncPlayer.setPosition(m_audioPosition);

            stopTime = System.currentTimeMillis();
            int diff = (int) (stopTime - startTime);
            if (diff > m_diffPeak) m_diffPeak = diff;

            if (stopTime - m_diffStartTime >= 1000) {
                m_diffStartTime = (int)stopTime;

                if (m_diffPeak > m_refreshRate || (int)((double)m_diffPeak * 4) < m_refreshRate)
                {
                    int newRate = (int)(m_diffPeak * 1.5);
                    if (newRate < 30) newRate = 30;
                    if (m_refreshRate != newRate) {
                        m_refreshRate = newRate;
                        refreshRateLabel.setText("Refresh rate: " + m_refreshRate + " ms");
                        updateKeyFrame();
                    }
                }

                m_diffPeak = 0;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void analyzeWidthChanged() throws IOException {
        m_samplesToAnalyze = (int)analyzeWidthCombo.getValue();
        m_analyzeWidth = ((double) m_samplesToAnalyze / (double) m_stream.getTotalSamplesCount());
        m_waveformPlotter.plot(m_audioPosition, m_analyzeWidth);
        drawSpectrum();
    }

    @FXML
    public void smoothnessChanged(MouseEvent event) throws IOException {
        double value = smoothnessScrollBar.getValue();
        m_spectrumPlotter.setAverageFactor(value);
        drawSpectrum();
    }

    public void setStream(AudioStreamInfo stream) throws IOException {
        ObservableList<ComboChannelItem> list = FXCollections.observableArrayList();
        for (int i = 0; i < stream.getChannelsCount(); i++) {
            list.add(new ComboChannelItem(i));
        }
        channelCombo.setItems(list);
        channelCombo.setConverter(new StringConverter<ComboChannelItem>() {
            @Override
            public String toString(ComboChannelItem object) {
                return object.getChannelName();
            }

            @Override
            public ComboChannelItem fromString(String string) {
                return null;
            }
        });

        analyzeWidthCombo.getItems().addAll(2048, 4096, 8192, 16384, 32768, 65536, 131072, 262144);

        m_stream = stream;
        m_player = new PCMPlayer(stream.getSampleRate());
        m_analyzer = new FFTAnalyzer();

        m_samplesToAnalyze = 32768;//m_stream.getSampleRate();
        m_analyzeWidth = ((double) m_samplesToAnalyze / (double) m_stream.getTotalSamplesCount());

        m_audioPosition = 0;
        m_spectrumPlotter.setFrequencyRange(m_stream.getSampleRate() / 2);
        m_spectrumPlotter.drawAxesDescription();

        channelCombo.setValue(list.get(0));
        analyzeWidthCombo.setValue(16384);
    }

    private void drawSpectrum() throws IOException {
        double begin = Math.max(0, m_audioPosition - m_analyzeWidth / 2);
        double end = Math.min(1, m_audioPosition + m_analyzeWidth / 2);

        int beginSample = (int) Math.round(begin * m_stream.getTotalSamplesCount());
        int endSample = (int) Math.round(end * m_stream.getTotalSamplesCount());

        //int count = endSample - beginSample;
        //if (count % 2 != 0) count++;
        int count = m_samplesToAnalyze;

        double[] samples = new double[count];
        m_reader.readSamples(beginSample, count, samples, 0);

        m_analyzer.analyze(samples);

        m_spectrumPlotter.plot(m_analyzer.getAmplitudes());
    }
}
