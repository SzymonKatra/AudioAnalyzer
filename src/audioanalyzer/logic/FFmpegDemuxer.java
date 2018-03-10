package audioanalyzer.logic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class FFmpegDemuxer implements IAudioFileDemuxer {
    private String m_fileName;

    public FFmpegDemuxer(String fileName) {
        m_fileName = fileName;
    }

    @Override
    public void Demux(int streamIndex, int channelIndex, String outputFileName) {
        String channel = String.format("0.%d.%d", streamIndex, channelIndex);
        try {
            Process process = new ProcessBuilder("ffmpeg", "-i", m_fileName, "-f", "f32be", "-acodec", "pcm_f32be", "-map_channel", channel, outputFileName).start();

            process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
