package audioanalyzer.view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import audioanalyzer.logic.AudioStreamInfo;

import java.io.IOException;
import java.util.Locale;

public class StreamInfoDisplay extends VBox {

    //@FXML
    //private Label streamIndex;
    @FXML
    private Label duration;
    @FXML
    private Label channelsCount;
    @FXML
    private Label channelLayout;
    @FXML
    private Label sampleRate;
    @FXML
    private Label bitRate;
    @FXML
    private Label codecName;

    public StreamInfoDisplay() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("StreamInfoDisplay.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public StreamInfoDisplay(AudioStreamInfo info) {
        this();

        //streamIndex.setText(Integer.toString(info.getStreamIndex()));
        double dur = info.getDuration();
        int minutes = (int)Math.floor(dur / 60);
        double secs = dur % 60;
        duration.setText(String.format(Locale.US, "%02d:%02d", minutes, (int)secs));
        channelsCount.setText(Integer.toString(info.getChannelsCount()));
        channelLayout.setText(info.getChannelLayout());
        sampleRate.setText(Integer.toString(info.getSampleRate()));
        bitRate.setText(Integer.toString(info.getBitRate()));
        codecName.setText(info.getCodecName());
    }
}
