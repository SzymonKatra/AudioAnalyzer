package audioanalyzer.logic.playback;

import javax.sound.sampled.*;

/**
 * Implementation of PCM player
 */
public class PCMPlayer implements IPCMPlayer {
    private AudioFormat m_format;
    private DataLine.Info m_info;
    private SourceDataLine m_line;
    private byte[] m_buffer;

    public PCMPlayer(int sampleRate) throws LineUnavailableException {
        m_buffer = new byte[sampleRate * 2];

        m_format = new AudioFormat(sampleRate, 16, 1, true, true);
        m_info = new DataLine.Info(SourceDataLine.class, m_format);
        m_line = (SourceDataLine) AudioSystem.getLine(m_info);
        m_line.open(m_format, sampleRate * 4);
    }

    public void start() {
        m_line.start();
    }

    public void play(double[] buffer, int index, int count) {
        for (int i = 0; i < count; i++) {
            short sample = (short) (buffer[index + i] * Short.MAX_VALUE);
            m_buffer[i * 2] = (byte) (sample >> 8);
            m_buffer[i * 2 + 1] = (byte) sample;
        }

        m_line.write(m_buffer, 0, count * 2);
    }

    public void stop() {
        m_line.stop();
        m_line.flush();
    }

    public void discard() {
        m_line.flush();
    }
}