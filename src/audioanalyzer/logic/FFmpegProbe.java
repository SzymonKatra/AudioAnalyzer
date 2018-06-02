package audioanalyzer.logic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class FFmpegProbe implements IAudioFileProbe {
    private class AudioStreamResult {
        private AudioStreamInfo m_audioStream;
        private int m_nextIndex;
        private boolean m_isFinished;

        public AudioStreamResult(AudioStreamInfo audioStream, int nextIndex, boolean isFinished) {
            m_audioStream = audioStream;
            m_nextIndex = nextIndex;
            m_isFinished = isFinished;
        }

        public AudioStreamInfo getStream() {
            return m_audioStream;
        }

        public int getNextIndex() {
            return m_nextIndex;
        }

        public boolean getIsFinished() {
            return m_isFinished;
        }
    }

    private String m_fileName;
    private List<AudioStreamInfo> m_streams;

    public FFmpegProbe(String fileName) throws IOException {
        m_fileName = fileName;

        m_streams = new ArrayList<AudioStreamInfo>();

        String ffProbeResult = runFFmpeg();
        int currentIndex = 0;
        AudioStreamResult result = null;

        do {
            result = findStream(ffProbeResult, currentIndex);
            currentIndex = result.getNextIndex();
            AudioStreamInfo s = result.getStream();
            if (s != null) m_streams.add(s);
        } while(!result.getIsFinished());
    }

    private String runFFmpeg() throws IOException {
        Process process = new ProcessBuilder("ffprobe", "-show_streams", m_fileName).start();
        BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = input.readLine()) != null) {
            sb.append(line);
            sb.append('\n');
        }

        input.close();

        return sb.toString();
    }

    private AudioStreamResult findStream(String ffprobeResult, int start) {
        int streamTagIndex = ffprobeResult.indexOf("[STREAM]", start);
        int streamEndTagIndex = ffprobeResult.indexOf("[/STREAM]", start);

        if (streamTagIndex == -1 || streamEndTagIndex == -1) return new AudioStreamResult(null, start, true);

        String keyValuePairs = ffprobeResult.substring(streamTagIndex + 8, streamEndTagIndex);

        String[] lines = keyValuePairs.split("\n");
        Map<String, String> properties = new HashMap<String, String>();
        for (String line : lines) {
            String[] split = line.split("=");
            if (split.length != 2) continue;

            properties.put(split[0], split[1]);
        }

        if (!properties.getOrDefault("codec_type", "").equals("audio")) {
            return new AudioStreamResult(null, streamEndTagIndex + 9, false);
        }

        AudioStreamInfo stream = new AudioStreamInfo(Integer.parseInt(getPropertyOrDefault(properties, "index", "0")),
                                             Integer.parseInt(getPropertyOrDefault(properties, "channels", "1")),
                                             Integer.parseInt(getPropertyOrDefault(properties, "sample_rate", "0")),
                                             Integer.parseInt(getPropertyOrDefault(properties, "bit_rate", "0")),
                                             getPropertyOrDefault(properties, "codec_long_name", ""),
                                             getPropertyOrDefault(properties, "channel_layout", ""),
                                             Double.parseDouble(getPropertyOrDefault(properties, "duration", "0")));

        return new AudioStreamResult(stream, streamEndTagIndex + 9, false);
    }

    private String getPropertyOrDefault(Map<String, String> properties, String key, String defaultValue) {
        String result = properties.getOrDefault(key, defaultValue);

        if (result.equals("N/A")) return defaultValue;

        return result;
    }

    @Override
    public String getFileName() {
        return m_fileName;
    }

    @Override
    public List<AudioStreamInfo> getStreams() {
        return m_streams;
    }
}
