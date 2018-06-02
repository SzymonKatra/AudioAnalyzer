package audioanalyzer.logic;

import java.util.List;

/**
 * Interface for audio probe
 */
public interface IAudioFileProbe {
    String getFileName();
    List<AudioStreamInfo> getStreams();
}
