package audioanalyzer.view;

import audioanalyzer.logic.AudioStreamInfo;
import audioanalyzer.logic.IPCMPlayer;
import audioanalyzer.logic.S32PCMPlayer;

import java.io.*;

public class AnalyzerSceneController {
    private AudioStreamInfo m_stream;
    private IPCMPlayer m_player;
    private FileInputStream m_currentFile;

    public void setStream(AudioStreamInfo stream) throws IOException {
        m_stream = stream;
        m_player = new S32PCMPlayer(stream.getSampleRate());

        m_currentFile = new FileInputStream(m_stream.getRawFilePaths().get(0));
        DataInputStream data = new DataInputStream(m_currentFile);
        double[] buffer = new double[44100];
        for (int i = 0; i < 44100; i++) buffer[i] = data.readDouble();
        m_player.play(buffer, 0, 44100);
    }
}
