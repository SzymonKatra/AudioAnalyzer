package audioanalyzer.logic;

public class AudioStreamInfo {
    private int m_streamIndex;
    private int m_channelsCount;
    private int m_sampleRate;
    private int m_bitRate;
    private String m_codecName;
    private String m_channelLayout;
    private double m_duration;

    public AudioStreamInfo(int streamIndex, int channelsCount, int sampleRate, int bitRate, String codecName, String channelLayout, double duration) {
        m_streamIndex = streamIndex;
        m_channelsCount = channelsCount;
        m_sampleRate = sampleRate;
        m_bitRate = bitRate;
        m_codecName = codecName;
        m_channelLayout = channelLayout;
        m_duration = duration;
    }

    public int getStreamIndex() {
        return m_streamIndex;
    }

    public int getChannelsCount() {
        return m_channelsCount;
    }

    public int getSampleRate() {
        return m_sampleRate;
    }

    public int getBitRate() {
        return m_bitRate;
    }

    public String getCodecName() {
        return m_codecName;
    }

    public String getChannelLayout() {
        return m_channelLayout;
    }

    public double getDuration() {
        return m_duration;
    }
}
