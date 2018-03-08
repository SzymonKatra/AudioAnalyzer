package audioanalyzer.view;

import audioanalyzer.logic.FFmpegProbe;
import audioanalyzer.logic.IAudioFileProbe;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;

import java.io.*;

public class MainSceneController {
    @FXML
    private Label selectedFileName;

    @FXML
    private void selectFileHandler(MouseEvent event) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select audio file");
        File file = fileChooser.showOpenDialog(((Node)event.getTarget()).getScene().getWindow());

        selectedFileName.setText(file.getName());

        IAudioFileProbe probe = new FFmpegProbe(file.getAbsolutePath());
    }
}
