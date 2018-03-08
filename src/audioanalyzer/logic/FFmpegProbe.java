package audioanalyzer.logic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class FFmpegProbe implements  IAudioFileProbe {
    private class AudioStreamResult {
        private AudioStream m_audioStream;
        private int m_nextIndex;
        private boolean m_isFinished;

        public AudioStreamResult(AudioStream audioStream, int nextIndex, boolean isFinished) {
            m_audioStream = audioStream;
            m_nextIndex = nextIndex;
            m_isFinished = isFinished;
        }

        public AudioStream getStream() {
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
    private List<AudioStream> m_streams;

    public FFmpegProbe(String fileName) throws IOException {
        m_fileName = fileName;

        m_streams = new ArrayList<AudioStream>();

        String ffProbeResult = runFFmpeg();
        int currentIndex = 0;
        AudioStreamResult result = null;

        do {
            result = findStream(ffProbeResult, currentIndex);
            currentIndex = result.getNextIndex();
            AudioStream s = result.getStream();
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

        AudioStream stream = new AudioStream(Integer.parseInt(properties.getOrDefault("index", "0")),
                                             Integer.parseInt(properties.getOrDefault("channels", "1")),
                                             Integer.parseInt(properties.getOrDefault("sample_rate", "0")),
                                             Integer.parseInt(properties.getOrDefault("bit_rate", "0")),
                                             properties.getOrDefault("codec_long_name", ""),
                                             properties.getOrDefault("channel_layout", ""));

        return new AudioStreamResult(stream, streamEndTagIndex + 9, false);
    }

    @Override
    public List<AudioStream> getStreams() {
        return null;
    }
}