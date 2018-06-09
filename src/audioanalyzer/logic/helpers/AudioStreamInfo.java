package audioanalyzer.logic.helpers;

import java.util.ArrayList;
import java.util.List;

/**
 * Informations about audio stream
 */
public class AudioStreamInfo {
    private int m_streamIndex;
    private int m_channelsCount;
    private int m_sampleRate;
    private int m_bitRate;
    private String m_codecName;
    private String m_channelLayout;
    private double m_duration;

    private ArrayList<String> m_rawFilePaths;

    public AudioStreamInfo(int streamIndex, int channelsCount, int sampleRate, int bitRate, String codecName, String channelLayout, double duration) {
        m_streamIndex = streamIndex;
        m_channelsCount = channelsCount;
        m_sampleRate = sampleRate;
        m_bitRate = bitRate;
        m_codecName = codecName;
        m_channelLayout = channelLayout;
        m_duration = duration;

        m_rawFilePaths = new ArrayList<String>();
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

    public void setDuration(double value) {
        m_duration = value;
    }

    public long getTotalSamplesCount() {
        return (long)Math.round(m_sampleRate * m_duration);
    }

    public List<String> getRawFilePaths() {
        return m_rawFilePaths;
    }

    public void addRawFilePath(String value) {
        m_rawFilePaths.add(value);
    }
}
