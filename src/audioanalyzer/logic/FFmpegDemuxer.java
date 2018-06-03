package audioanalyzer.logic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * FFmpeg implementation of audio demuxer
 */
public class FFmpegDemuxer implements IAudioFileDemuxer {
    private String m_fileName;

    public FFmpegDemuxer(String fileName) {
        m_fileName = fileName;
    }

    /**
     * Executes ffmpeg in order to demux given audio
     * @param streamIndex
     * @param channelIndex
     * @param outputFileName
     */
    @Override
    public void demux(int streamIndex, int channelIndex, String outputFileName) {
        String channel = String.format("0.%d.%d", streamIndex, channelIndex);
        try {
            Process process = new ProcessBuilder("ffmpeg", "-i", String.format("\"%s\"", m_fileName), "-f", "f64be", "-acodec", "pcm_f64be", "-map_channel", channel, String.format("\"%s\"", outputFileName), "-y").start();

            BufferedReader input = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            input.close();

            process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
