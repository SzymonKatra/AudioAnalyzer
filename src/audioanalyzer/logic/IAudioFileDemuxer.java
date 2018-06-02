package audioanalyzer.logic;

/**
 * Interface for audio demuxer
 */
public interface IAudioFileDemuxer {
    void demux(int streamIndex, int channelIndex, String outputFileName);
}
