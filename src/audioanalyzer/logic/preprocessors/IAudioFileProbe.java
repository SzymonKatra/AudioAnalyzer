package audioanalyzer.logic.preprocessors;

import audioanalyzer.logic.helpers.AudioStreamInfo;

import java.util.List;

/**
 * Interface for audio probe
 */
public interface IAudioFileProbe {
    /**
     * Gets file name
     * @return
     */
    String getFileName();

    /**
     * Lists all audio streams available in the file
     * @return
     */
    List<AudioStreamInfo> getStreams();
}
