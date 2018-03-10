package audioanalyzer.logic;

import java.util.List;

public interface IAudioFileProbe {
    String getFileName();
    List<AudioStreamInfo> getStreams();
}
