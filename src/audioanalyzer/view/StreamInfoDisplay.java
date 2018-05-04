package audioanalyzer.view;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import audioanalyzer.logic.AudioStreamInfo;

import java.io.IOException;
import java.util.Locale;

public class StreamInfoDisplay extends VBox {
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
    @FXML
    private Button analyze;

    public StreamInfoDisplay() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("StreamInfoDisplay.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        analyze.onMouseClickedProperty().set(new EventHandler<MouseEvent>(){
            @Override
            public void handle(MouseEvent event){
                onAnalyzeClicked().get().handle(event);
            }
        });
    }

    public StreamInfoDisplay(AudioStreamInfo info) {
        this();

        //streamIndex.setText(Integer.toString(info.getStreamIndex()));
        double dur = info.getDuration();
        int minutes = (int) Math.floor(dur / 60);
        double secs = dur % 60;
        duration.setText(String.format(Locale.US, "%02d:%02d", minutes, (int) secs));
        channelsCount.setText(Integer.toString(info.getChannelsCount()));
        channelLayout.setText(info.getChannelLayout());
        sampleRate.setText(Integer.toString(info.getSampleRate()));
        bitRate.setText(Integer.toString(info.getBitRate()));
        codecName.setText(info.getCodecName());
    }

    private ObjectProperty<EventHandler<MouseEvent>> propertyOnAnalyzeClicked = new SimpleObjectProperty<EventHandler<MouseEvent>>();

    public final ObjectProperty<EventHandler<MouseEvent>> onAnalyzeClicked() {
        return propertyOnAnalyzeClicked;
    }

    public final void setOnAnalyzeClicked(EventHandler<MouseEvent> handler) {
        propertyOnAnalyzeClicked.set(handler);
    }

    public final EventHandler<MouseEvent> getOnAnalyzeClicked() {
        return propertyOnAnalyzeClicked.get();
    }
}
