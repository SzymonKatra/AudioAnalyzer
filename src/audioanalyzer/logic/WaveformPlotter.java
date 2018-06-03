package audioanalyzer.logic;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

import java.io.IOException;

/**
 * Waveform plotter
 */
public class WaveformPlotter {
    private class WaveformItem {
        public double m1;
        public double m2;
        public double a1;
        public double a2;
    }

    private GraphicsContext m_graphics;
    private Rectangle2D m_targetRectangle;
    private Rectangle2D m_waveformRectangle;
    private AudioStreamInfo m_streamInfo;
    private SampleReaderHelper m_reader;
    private PixelWriter m_pixelWriter;
    private int m_timelineHeight = 15;
    private int m_timelineScales = 10;
    private int m_triangleHalfWidth = 7;
    private int m_triangleHeight = 7;
    private int m_timelineX = -14;
    private double[] m_triangleX;
    private double[] m_triangleY;
    private WaveformItem[] m_waveform;

    public WaveformPlotter(GraphicsContext graphics, Rectangle2D targetRectangle, AudioStreamInfo streamInfo, SampleReaderHelper sampleReader) {
        m_graphics = graphics;
        m_pixelWriter = m_graphics.getPixelWriter();
        m_targetRectangle = targetRectangle;
        m_waveformRectangle = new Rectangle2D(m_targetRectangle.getMinX(), m_targetRectangle.getMinY() + m_triangleHeight, m_targetRectangle.getWidth(), m_targetRectangle.getHeight() - m_timelineHeight - m_triangleHeight);
        m_streamInfo = streamInfo;
        m_reader = sampleReader;
        m_triangleX = new double[3];
        m_triangleY = new double[3];
        m_waveform = new WaveformItem[(int)m_waveformRectangle.getWidth()];

        rebuildWaveform();
    }

    /**
     * Reads all samples from the file in order to rebuild waveform
     */
    public void rebuildWaveform() {
        int sampleDistance = (int) (m_streamInfo.getTotalSamplesCount() / m_waveformRectangle.getWidth());
        double[] buffer = new double[sampleDistance];

        for (int i = 0; i < m_waveformRectangle.getWidth(); i++) {
            try {
                m_reader.readSamples(i * sampleDistance, sampleDistance, buffer, 0);
            } catch (IOException e) {
                e.printStackTrace();
            }
            double maxPositive = 0;
            double maxNegative = 0;
            double avgPositive = 0;
            double avgNegative = 0;
            for (int j = 0; j < sampleDistance; ++j) {
                double sample = buffer[j];
                if (sample > 0) {
                    if (sample > maxPositive) maxPositive = sample;
                    avgPositive += sample;
                } else {
                    if (sample < maxNegative) maxNegative = sample;
                    avgNegative += sample;
                }
            }

            avgPositive /= (double) sampleDistance;
            avgNegative /= (double) sampleDistance;

            double halfHeight = m_waveformRectangle.getHeight() / 2;

            WaveformItem item = new WaveformItem();

            item.m1 = halfHeight * (1 - maxPositive);
            item.m2 = halfHeight * (1 - maxNegative);
            item.a1 = halfHeight * (1 - avgPositive);
            item.a2 = halfHeight * (1 - avgNegative);

            m_waveform[i] = item;
        }
    }

    /**
     * Plots already computed waveform
     * @param position
     * @param analyzeWidth
     * @throws IOException
     */
    public void plot(double position, double analyzeWidth) throws IOException {
        m_graphics.clearRect(m_waveformRectangle.getMinX(), m_waveformRectangle.getMinY() - m_triangleHeight, m_waveformRectangle.getWidth(), m_waveformRectangle.getHeight() + m_triangleHeight);

        for (int i = 0; i < m_waveform.length; i++) {
            for (int y = (int) m_waveform[i].m1; y <= (int) m_waveform[i].m2; y++)
                m_pixelWriter.setColor(i + (int) m_waveformRectangle.getMinX(), y + (int) m_waveformRectangle.getMinY(), Color.BLUE);
            for (int y = (int) m_waveform[i].a1; y <= (int) m_waveform[i].a2; y++)
                m_pixelWriter.setColor(i + (int) m_waveformRectangle.getMinX(), y + (int) m_waveformRectangle.getMinY(), Color.CORNFLOWERBLUE);
        }

        m_graphics.setFill(new Color(0, 0, 0, 0.75));
        m_graphics.fillRect(position * m_waveformRectangle.getWidth() - analyzeWidth * m_waveformRectangle.getWidth() / 2 + m_waveformRectangle.getMinX(), m_waveformRectangle.getMinY(),
                analyzeWidth * m_waveformRectangle.getWidth(), m_waveformRectangle.getHeight());

        double realPos = position * m_waveformRectangle.getWidth();

        m_triangleX[0] = realPos;
        m_triangleY[0] = m_targetRectangle.getMinY() + m_triangleHeight;
        m_triangleX[1] = realPos - m_triangleHalfWidth;
        m_triangleY[1] = m_targetRectangle.getMinY();
        m_triangleX[2] = realPos + m_triangleHalfWidth;
        m_triangleY[2] = m_triangleY[1];

        m_graphics.setFill(Color.BLACK);
        m_graphics.fillPolygon(m_triangleX, m_triangleY, 3);
    }

    /**
     * Draws timeline below waveform
     */
    public void drawTimeline() {
        double y = m_targetRectangle.getMinY() + m_targetRectangle.getHeight();
        m_graphics.clearRect(m_timelineX, m_waveformRectangle.getMinY() + m_waveformRectangle.getHeight(), m_targetRectangle.getWidth() - m_timelineX, m_timelineHeight);

        double timeStep = m_streamInfo.getDuration() / m_timelineScales;
        double positionStep = m_targetRectangle.getWidth() / m_timelineScales;

        for (int i = 0; i <= m_timelineScales; i++) {
            int totalSeconds = (int)(i * timeStep);

            int minutes = totalSeconds / 60;
            int seconds = totalSeconds % 60;

            double x = positionStep * i + m_targetRectangle.getMinX();

            int additionalOffset = 0;
            if (i == 0) additionalOffset = 15;
            else if (i == m_timelineScales) additionalOffset = -15;

            m_graphics.strokeText(String.format("%02d:%02d", minutes, seconds), x + m_timelineX + additionalOffset, y);
        }
    }
}
