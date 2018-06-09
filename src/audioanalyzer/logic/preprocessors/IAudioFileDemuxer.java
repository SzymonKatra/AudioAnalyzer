package audioanalyzer.logic.preprocessors;

/**
 * Interface for audio demuxer
 */
public interface IAudioFileDemuxer {
    /**
     * Demuxes stream to specified file
     * @param streamIndex
     * @param channelIndex
     * @param outputFileName
     */
    void demux(int streamIndex, int channelIndex, String outputFileName) throws Exception;
}
