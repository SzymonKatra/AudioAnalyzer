package audioanalyzer.view;

import audioanalyzer.Main;
import audioanalyzer.logic.helpers.AudioStreamInfo;
import audioanalyzer.logic.preprocessors.FFmpegDemuxer;
import audioanalyzer.logic.preprocessors.FFmpegProbe;
import audioanalyzer.logic.preprocessors.IAudioFileDemuxer;
import audioanalyzer.logic.preprocessors.IAudioFileProbe;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import javax.sound.sampled.LineUnavailableException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Initial screen to choose file
 */
public class MainSceneController {
    private class DemuxInfo {
        private int m_streamIndex;
        private int m_channeIndex;

        public DemuxInfo(int streamIndex, int channelIndex) {
            m_streamIndex = streamIndex;
            m_channeIndex = channelIndex;
        }

        public int getStreamIndex() {
            return m_streamIndex;
        }

        public int getChannelIndex() {
            return m_channeIndex;
        }
    }

    @FXML
    private Label selectedFileName;

    @FXML
    private VBox streamBox;

    @FXML
    private Button selectFileButton;

    @FXML
    private Label demuxInProgress;

    @FXML
    private Label readingMetadataInProgress;

    private StreamInfoDisplay streamInfo;

    private int m_progressDots = 0;
    private Timeline m_progressTimeline;

    private ExecutorService m_executor;

    public MainSceneController() {
        m_executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Select file event handler
     * @param event
     * @throws IOException
     */
    @FXML
    private void selectFileHandler(MouseEvent event) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select audio file");
        File file = fileChooser.showOpenDialog(((Node) event.getTarget()).getScene().getWindow());

        if (file == null) return;

        selectedFileName.setText(file.getName());
        streamBox.getChildren().clear();

        selectFileButton.setDisable(true);
        readingMetadataInProgress.setVisible(true);
        m_executor.submit(() -> readMetadata(file));
    }

    /**
     * Reads metadata from the specified file and shows on the scene
     * @param file
     */
    private void readMetadata(File file) {
        try {
            final IAudioFileProbe probe = new FFmpegProbe(file.getAbsolutePath());
            Platform.runLater(() -> {
                selectFileButton.setDisable(false);
                readingMetadataInProgress.setVisible(false);

                List<AudioStreamInfo> streams = probe.getStreams();

                if (streams.isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Selected file does not contain any audio stream", ButtonType.OK);
                    alert.showAndWait();
                    return;
                }

                AudioStreamInfo stream = streams.get(0);

                streamInfo = new StreamInfoDisplay(stream);
                streamInfo.setOnAnalyzeClicked(analyzeEvent -> startDemux(stream, probe));

                streamBox.getChildren().add(streamInfo);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Begins demuxing of the specified stream
     * @param stream
     * @param probe
     */
    private void startDemux(AudioStreamInfo stream, IAudioFileProbe probe) {
        selectFileButton.setDisable(true);
        demuxInProgress.setVisible(true);

        m_progressTimeline = new Timeline(new KeyFrame(
                Duration.millis(300), ae -> {
            m_progressDots++;
            if (m_progressDots > 5) m_progressDots = 0;
            String val = "Demuxing and decoding in progress";
            for (int i = 0; i < m_progressDots; i++) val += ".";
            demuxInProgress.setText(val);
        }));
        m_progressTimeline.setCycleCount(Animation.INDEFINITE);
        m_progressTimeline.play();

        ArrayList<DemuxInfo> toDemux = new ArrayList<DemuxInfo>();

        for (int i = 0; i < stream.getChannelsCount(); i++) {
            DemuxInfo dInfo = new DemuxInfo(stream.getStreamIndex(), i);
            toDemux.add(dInfo);
        }

        m_executor.submit(() -> {
            try {
                backgroundDemux(probe, stream, toDemux);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Method for demuxing stream. Should be run in the background.
     * @param probe
     * @param stream
     * @param toDemux
     * @throws Exception
     */
    private void backgroundDemux(IAudioFileProbe probe, AudioStreamInfo stream, List<DemuxInfo> toDemux) throws Exception {
        IAudioFileDemuxer demuxer = new FFmpegDemuxer(probe.getFileName());

        for (DemuxInfo info : toDemux) {
            File tmpFile = null;
            try {
                tmpFile = File.createTempFile("audioanalyzer-", String.format("-%d-%d", info.getStreamIndex(), info.getChannelIndex()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            tmpFile.deleteOnExit();

            demuxer.demux(info.getStreamIndex(), info.getChannelIndex(), tmpFile.getAbsolutePath());

            if (stream.getDuration() == 0) {
                stream.setDuration(((double) tmpFile.length() / 8) / stream.getSampleRate());
            }

            stream.addRawFilePath(tmpFile.getAbsolutePath());
        }

        Platform.runLater(() -> {
            AnalyzerSceneController analyzerController = null;
            try {
                analyzerController = (AnalyzerSceneController) Main.changeView("view/AnalyzerScene.fxml");
                m_executor.shutdown();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                analyzerController.setStream(stream);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            }
        });
    }
}
