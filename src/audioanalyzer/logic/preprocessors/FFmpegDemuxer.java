package audioanalyzer.logic.preprocessors;

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
    public void demux(int streamIndex, int channelIndex, String outputFileName) throws IOException, InterruptedException {
        String channel = String.format("0.%d.%d", streamIndex, channelIndex);
        Process process = new ProcessBuilder("ffmpeg", "-i", String.format("\"%s\"", m_fileName), "-f", "f64be", "-acodec", "pcm_f64be", "-map_channel", channel, String.format("\"%s\"", outputFileName), "-y").start();

        BufferedReader input = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        input.close();

        process.waitFor();
    }
}
