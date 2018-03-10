package audioanalyzer.logic;

import javax.sound.sampled.*;

public class S32PCMPlayer {
    private AudioFormat m_format;
    private DataLine.Info m_info;
    private SourceDataLine m_line;

    public S32PCMPlayer(int sampleRate)
    {
        m_format = new AudioFormat(sampleRate, 32, 1, true, true);
        m_info = new DataLine.Info(SourceDataLine.class, m_format);
        try {
            m_line = (SourceDataLine) AudioSystem.getLine(m_info);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }

        try {
            m_line.open(m_format, 44100);
            m_line.start();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }
}
