package audioanalyzer.logic;

public interface IAudioFileDemuxer {
    void Demux(int streamIndex, int channelIndex, String outputFileName);
}
