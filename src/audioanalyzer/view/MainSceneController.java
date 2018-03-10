package audioanalyzer.view;

import audioanalyzer.logic.AudioStreamInfo;
import audioanalyzer.logic.FFmpegProbe;
import audioanalyzer.logic.IAudioFileProbe;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.*;
import java.util.List;

public class MainSceneController {
    @FXML
    private Label selectedFileName;

    @FXML
    private VBox streamBox;

    @FXML
    private void selectFileHandler(MouseEvent event) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select audio file");
        File file = fileChooser.showOpenDialog(((Node)event.getTarget()).getScene().getWindow());

        selectedFileName.setText(file.getName());

        IAudioFileProbe probe = new FFmpegProbe(file.getAbsolutePath());

        List<AudioStreamInfo> streams = probe.getStreams();

        if (streams.isEmpty())
        {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Selected file does not contain any audio stream", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        streamBox.getChildren().add(new StreamInfoDisplay(streams.get(0)));
    }
}
