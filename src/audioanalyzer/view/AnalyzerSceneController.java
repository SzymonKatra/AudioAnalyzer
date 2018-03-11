package audioanalyzer.view;

import audioanalyzer.logic.AudioStreamInfo;
import audioanalyzer.logic.IPCMPlayer;
import audioanalyzer.logic.PCMPlayer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

import java.io.*;
import java.util.Random;

public class AnalyzerSceneController {
    @FXML
    private Canvas canvas;

    private AudioStreamInfo m_stream;
    private IPCMPlayer m_player;
    private RandomAccessFile m_currentFile;

    private GraphicsContext m_gfx;
    private PixelWriter m_pixelGfx;

    private double[] m_audioPreviewBuffer;

    @FXML
    public void initialize() {
        m_gfx = canvas.getGraphicsContext2D();
        m_pixelGfx = m_gfx.getPixelWriter();
    }

    public void setStream(AudioStreamInfo stream) throws IOException {
        m_stream = stream;
        m_player = new PCMPlayer(stream.getSampleRate());

        m_currentFile = new RandomAccessFile(m_stream.getRawFilePaths().get(0), "r");

        m_audioPreviewBuffer = new double[800];
        buildPreview();

        //double[] buffer = new double[44100];
        //for (int i = 0; i < 44100; i++) buffer[i] = m_currentFile.readDouble();
        //m_player.play(buffer, 0, 44100);
    }

    private void buildPreview() throws IOException {
        int sampleDistance = (int)(m_stream.getTotalSamplesCount() / m_audioPreviewBuffer.length);
        /*for (int i = 0; i < m_audioPreviewBuffer.length; i++) {
            m_currentFile.seek(i * sampleDistance);
            m_audioPreviewBuffer[i] = m_currentFile.readDouble();
        }*/

        byte[] buffer = new byte[sampleDistance * 8];

        for (int i = 0; i < m_audioPreviewBuffer.length; i++) {
            m_currentFile.seek(i * sampleDistance * 8);
            m_currentFile.read(buffer);
            double maxPositive = 0;
            double maxNegative = 0;
            double avgPositive = 0;
            double avgNegative = 0;
            for (int j = 0; j < sampleDistance; ++j) {
                //double sample = m_currentFile.readDouble();
                long x = (Byte.toUnsignedLong(buffer[j * 8]) << 56) | (Byte.toUnsignedLong(buffer[j * 8 + 1]) << 48) | (Byte.toUnsignedLong(buffer[j * 8 + 2]) << 40) | (Byte.toUnsignedLong(buffer[j * 8 + 3]) << 32) |
                         (Byte.toUnsignedLong(buffer[j * 8 + 4]) << 24) | (Byte.toUnsignedLong(buffer[j * 8 + 5]) << 16) | (Byte.toUnsignedLong(buffer[j * 8 + 6]) << 8) | (Byte.toUnsignedLong(buffer[j * 8 + 7]));
                double sample = Double.longBitsToDouble(x);
                if (sample > 0)
                {
                    if (sample > maxPositive) maxPositive = sample;
                    avgPositive += sample;
                }
                else
                {
                    if (sample < maxNegative) maxNegative = sample;
                    avgNegative += sample;
                }
            }

            avgPositive /= (double)sampleDistance;
            avgNegative /= (double)sampleDistance;

            double m1 = 550 - 50 * maxPositive;
            double m2 = 550 - 50 * maxNegative;
            double a1 = 550 - 50 * avgPositive;
            double a2 = 550 - 50 * avgNegative;

            for (int y = (int)m1; y <= (int)m2; y++) m_pixelGfx.setColor(i, y, Color.BLUE);
            for (int y = (int)a1; y <= (int)a2; y++) m_pixelGfx.setColor(i, y, Color.CORNFLOWERBLUE);

            //m_gfx.setStroke(Color.BLUE);
            //m_gfx.strokeLine(i, m1, i, m2);
            //m_gfx.setStroke(Color.CORNFLOWERBLUE);
            //m_gfx.strokeLine(i, a1, i, a2);
        }
    }
}
