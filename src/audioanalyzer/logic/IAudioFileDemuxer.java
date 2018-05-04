package audioanalyzer.logic;

public interface IAudioFileDemuxer {
    void demux(int streamIndex, int channelIndex, String outputFileName);
}
