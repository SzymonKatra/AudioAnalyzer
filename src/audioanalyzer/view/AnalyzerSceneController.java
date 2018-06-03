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

/**
 * Controller used for analyzer scene
 */
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
    private Timeline m_timeline;

    private double[] m_samplesBuffer;
    private double[] m_amplitudesBuffer;
    private Object m_variablesLock = new Object();
    private Object m_drawSpectrumLock = new Object();
    private Object m_readerChangeLock = new Object();

    private double m_audioPosition;
    private int m_samplesToAnalyze;
    private double m_analyzeWidth;
    private boolean m_playing = false;

    private Thread m_analyzerThread;

    /**
     * Initializes controller
     */
    @FXML
    public void initialize() {
        m_gfx = canvas.getGraphicsContext2D();

        m_spectrumPlotter = new SpectrumPlotter(m_gfx, new Rectangle2D(0, 0, 999, 500));
    }

    /**
     * Mouse pressed on canvas event handler
     * @param event
     * @throws IOException
     */
    @FXML
    private void canvasMousePressed(MouseEvent event) throws IOException {
        if (!event.isPrimaryButtonDown()) return;

        if (m_playing) {
            m_timeline.stop();
            m_asyncPlayer.stop();
        }

        tryChangePosition(event);
    }

    /**
     * Mouse released on canvas event handler
     * @param event
     * @throws IOException
     */
    @FXML
    private void canvasMouseReleased(MouseEvent event) throws IOException {
        if (event.getButton() != MouseButton.PRIMARY) return;

        if (m_playing) {
            m_timeline.play();
            m_asyncPlayer.start();
        }
    }

    /**
     * Mouse dragged on canvas event handler
     * @param event
     * @throws IOException
     */
    @FXML
    private void canvasMouseDragged(MouseEvent event) throws IOException {
        if (!event.isPrimaryButtonDown()) return;

        tryChangePosition(event);
    }

    /**
     * Changes position of audio if mouse is inside bounds of waveform
     * @param event
     * @throws IOException
     */
    private void tryChangePosition(MouseEvent event) throws IOException {
        double y = event.getY();
        if (y > 500 && y < 600) {
            double x = event.getX();
            m_audioPosition = x / WIDTH;

            m_waveformPlotter.plot(m_audioPosition, m_analyzeWidth);
            drawSpectrum();
        }
    }

    /**
     * Channel changed event handler
     * @throws IOException
     */
    @FXML
    public void channelChanged() throws IOException {
        int channel = ((ComboChannelItem)channelCombo.getValue()).getChannelNumber();

        if (m_currentFile != null) m_currentFile.close();
        if (m_asyncPlayer != null) m_asyncPlayer.dispose();
        if (m_timeline != null) m_timeline.stop();

        m_currentFile = new RandomAccessFile(m_stream.getRawFilePaths().get(channel), "r");
        synchronized (m_readerChangeLock) {
            m_reader = new SampleReaderHelper(m_currentFile, false);
        }
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

        m_timeline = new Timeline(new KeyFrame(
                Duration.millis(30), ae -> {
            synchronized (m_variablesLock) {
                m_audioPosition += (0.03 / m_stream.getDuration());
                if (m_audioPosition > 1) m_audioPosition = 1;
            }
            try {
                drawSpectrum();
                m_waveformPlotter.plot(m_audioPosition, m_analyzeWidth);
            } catch (IOException e) {
                e.printStackTrace();
            }
            m_asyncPlayer.setPosition(m_audioPosition);
        }));
        m_timeline.setCycleCount(Animation.INDEFINITE);
        if (m_playing) m_timeline.play();
    }

    /**
     * Handler for play/pause button.
     */
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

    /**
     * Analyze width changed event handler
     * @throws IOException
     */
    @FXML
    public void analyzeWidthChanged() throws IOException {
        synchronized (m_variablesLock) {
            m_samplesToAnalyze = (int) analyzeWidthCombo.getValue();
            m_analyzeWidth = ((double) m_samplesToAnalyze / (double) m_stream.getTotalSamplesCount());
        }
        m_waveformPlotter.plot(m_audioPosition, m_analyzeWidth);
        drawSpectrum();
    }

    /**
     * Smoothness changed event handler
     * @param event
     * @throws IOException
     */
    @FXML
    public void smoothnessChanged(MouseEvent event) throws IOException {
        double value = smoothnessScrollBar.getValue();
        m_spectrumPlotter.setAverageFactor(value);
        drawSpectrum();
    }

    /**
     * Applies an audio stream to analyze
     * @param stream Stream to be analyzed
     * @throws IOException
     */
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

        synchronized (m_variablesLock) {
            m_stream = stream;
            m_player = new PCMPlayer(stream.getSampleRate());
            m_analyzer = new FFTAnalyzer();

            m_samplesToAnalyze = 32768;//m_stream.getSampleRate();
            m_analyzeWidth = ((double) m_samplesToAnalyze / (double) m_stream.getTotalSamplesCount());

            m_audioPosition = 0;
            m_spectrumPlotter.setFrequencyRange(m_stream.getSampleRate() / 2);
        }

        m_spectrumPlotter.drawAxesDescription();

        channelCombo.setValue(list.get(0));
        analyzeWidthCombo.setValue(16384);

        m_analyzerThread = new Thread(() -> {
            while (true) {
                double pos, width;
                int toAnalyze;
                long totalCount;
                synchronized (m_variablesLock) {
                    pos = m_audioPosition;
                    width = m_analyzeWidth;
                    toAnalyze = m_samplesToAnalyze;
                    totalCount = m_stream.getTotalSamplesCount();
                }

                double begin = Math.max(0, pos - width / 2);

                int beginSample = (int) Math.round(begin * totalCount);
                if (m_samplesBuffer == null || m_samplesBuffer.length != toAnalyze) {
                    m_samplesBuffer = new double[toAnalyze];
                }

                synchronized (m_readerChangeLock) {
                    try {
                        m_reader.readSamples(beginSample, m_samplesBuffer.length, m_samplesBuffer, 0);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                m_analyzer.analyze(m_samplesBuffer);

                synchronized (m_drawSpectrumLock) {
                    if (m_amplitudesBuffer == null || m_amplitudesBuffer.length != toAnalyze / 2) {
                        m_amplitudesBuffer = new double[toAnalyze / 2];
                    }
                    System.arraycopy(m_analyzer.getAmplitudes(), 0, m_amplitudesBuffer, 0, m_amplitudesBuffer.length);
                }
            }
        });
        m_analyzerThread.setDaemon(true);
        m_analyzerThread.start();
    }

    /**
     * Draws spectrum of current channel onto canvas
     * @throws IOException
     */
    private void drawSpectrum() throws IOException {
        /*double begin = Math.max(0, m_audioPosition - m_analyzeWidth / 2);

        int beginSample = (int) Math.round(begin * m_stream.getTotalSamplesCount());
        if (m_samplesBuffer == null || m_samplesBuffer.length != m_samplesToAnalyze){
            m_samplesBuffer = new double[m_samplesToAnalyze];
        }
        if (m_amplitudesBuffer == null || m_amplitudesBuffer.length != m_samplesToAnalyze / 2) {
            m_amplitudesBuffer = new double[m_samplesToAnalyze / 2];
        }

        synchronized (m_readerChangeLock) {
            m_reader.readSamples(beginSample, m_samplesBuffer.length, m_samplesBuffer, 0);
        }

        m_analyzer.analyze(m_samplesBuffer);

        synchronized (m_amplitudesBuffer) {
            System.arraycopy(m_analyzer.getAmplitudes(), 0, m_amplitudesBuffer, 0, m_amplitudesBuffer.length);
        }*/
        synchronized (m_drawSpectrumLock) {
            if (m_amplitudesBuffer != null) {
                m_spectrumPlotter.plot(m_amplitudesBuffer);
            }
        }
    }
}
