package audioanalyzer.view;

import audioanalyzer.Main;
import audioanalyzer.logic.*;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Initial screen to choose file
 */
public class MainSceneController {
    @FXML
    private Label selectedFileName;

    @FXML
    private VBox streamBox;

    private StreamInfoDisplay streamInfo;

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

        selectedFileName.setText(file.getName());

        IAudioFileProbe probe = new FFmpegProbe(file.getAbsolutePath());

        List<AudioStreamInfo> streams = probe.getStreams();

        if (streams.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Selected file does not contain any audio stream", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        AudioStreamInfo stream = streams.get(0);

        streamInfo = new StreamInfoDisplay(stream);
        streamInfo.setOnAnalyzeClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                IAudioFileDemuxer demuxer = new FFmpegDemuxer(probe.getFileName());

                for (int i = 0; i < stream.getChannelsCount(); i++) {
                    try {
                        File tmpFile = File.createTempFile("audioanalyzer-", String.format("-%d-%d", stream.getStreamIndex(), i));
                        tmpFile.deleteOnExit();

                        demuxer.demux(0, i, tmpFile.getAbsolutePath());

                        stream.addRawFilePath(tmpFile.getAbsolutePath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                AnalyzerSceneController analyzerController = null;
                try {
                    analyzerController = (AnalyzerSceneController) Main.changeView("view/AnalyzerScene.fxml");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    analyzerController.setStream(stream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        streamBox.getChildren().clear();
        streamBox.getChildren().add(streamInfo);
    }
}
