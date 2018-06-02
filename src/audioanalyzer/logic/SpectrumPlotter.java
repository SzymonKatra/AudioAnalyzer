package audioanalyzer.logic;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Spectrum plotter
 */
public class SpectrumPlotter {
    private Rectangle2D m_targetRectangle;
    private Rectangle2D m_graphRectangle;
    private double m_decibelRange = -100;
    private double m_frequencyRange;
    private double[] m_amplitudes;
    private double[] m_averageAmplitudes;
    private GraphicsContext m_gfx;
    private PixelWriter m_pixelGfx;
    private Color m_fillColor = Color.RED;
    private Color m_textColor = Color.BLACK;
    private double[] m_xPoints;
    private double[] m_yPoints;
    private double m_frequencyTextMargin = 20;
    private double m_powerTextMargin = 50;
    private int m_horizontalScales = 10;
    private int m_verticalScales = 10;
    private int m_decibelX = 5;
    private int m_decibelY = 5;
    private int m_hertzX = 30;
    private int m_hertzY = -5;
    private double m_averageFactor = 1;

    public SpectrumPlotter(GraphicsContext context, Rectangle2D targetRectangle) {
        m_gfx = context;
        m_pixelGfx = m_gfx.getPixelWriter();
        m_targetRectangle = targetRectangle;
        m_graphRectangle = new Rectangle2D(targetRectangle.getMinX() + m_powerTextMargin,
                targetRectangle.getMinY(),
                targetRectangle.getWidth() - m_powerTextMargin,
                targetRectangle.getHeight() - m_frequencyTextMargin);
    }

    public void setFrequencyRange(double frequencyRange) {
        m_frequencyRange = frequencyRange;
    }

    /**
     * Plots given amplitudes
     * @param amplitudes
     */
    public void plot(double[] amplitudes) {
        m_amplitudes = amplitudes;
        averageResults();

        m_gfx.clearRect(m_graphRectangle.getMinX(), m_graphRectangle.getMinY(), m_graphRectangle.getWidth(), m_graphRectangle.getHeight());
        m_gfx.setFill(m_fillColor);

        int targetLength = m_averageAmplitudes.length + 2;

        if (m_xPoints == null || m_xPoints.length < targetLength) {
            m_xPoints = new double[targetLength];
        }

        if (m_yPoints == null || m_yPoints.length < targetLength) {
            m_yPoints = new double[targetLength];
        }

        for (int i = 0; i < m_averageAmplitudes.length; i++) {
            double decibel = Math.max(m_decibelRange, 20 * Math.log10(m_averageAmplitudes[i]));
            m_xPoints[i] = ((double) i / m_averageAmplitudes.length) * m_graphRectangle.getWidth() + m_graphRectangle.getMinX();
            m_yPoints[i] = (decibel / m_decibelRange) * m_graphRectangle.getHeight() + m_graphRectangle.getMinY();
        }

        m_xPoints[targetLength - 2] = m_graphRectangle.getMaxX();
        m_yPoints[targetLength - 2] = m_graphRectangle.getMaxY();
        m_xPoints[targetLength - 1] = m_graphRectangle.getMinX();
        m_yPoints[targetLength - 1] = m_graphRectangle.getMaxY();

        m_gfx.fillPolygon(m_xPoints, m_yPoints, targetLength);

        drawGrid();
    }

    /**
     * Draws description for axes
     */
    public void drawAxesDescription() {
        double hStep = m_graphRectangle.getWidth() / m_horizontalScales;
        double vStep = m_graphRectangle.getHeight() / m_verticalScales;

        double decibelPerStep = m_decibelRange / m_verticalScales;

        for (int i = 0; i <= m_verticalScales; i++) {
            int additionalOffset = 0;
            if (i == 0) additionalOffset = 6;
            else if (i == m_verticalScales) additionalOffset = -6;
            m_gfx.strokeText(String.format("%d dB", (int)(i * decibelPerStep)),
                            m_targetRectangle.getMinX() + m_decibelX,
                            m_targetRectangle.getMinY() + m_decibelY + i * vStep + additionalOffset);
        }

        double hertzPerStep = m_frequencyRange / m_horizontalScales;

        for (int i = 0; i <= m_horizontalScales; i++) {
            int additionalOffset = 0;
            if (i == 0) additionalOffset = 20;
            else if (i == m_horizontalScales) additionalOffset = -30;
            m_gfx.strokeText(String.format("%d Hz", (int)(i * hertzPerStep)),
                            m_targetRectangle.getMinX() + m_hertzX + i * hStep + additionalOffset,
                            m_targetRectangle.getMaxY() + m_hertzY);
        }
    }

    public double getAverageFactor() {
        return m_averageFactor;
    }

    public void setAverageFactor(double value) {
        m_averageFactor = value;
    }

    private void averageResults() {
        int count = (int) (m_graphRectangle.getWidth() / m_averageFactor);

        if (m_amplitudes.length <= count) {
            m_averageAmplitudes = m_amplitudes;
        }

        if (m_averageAmplitudes == null || m_averageAmplitudes.length != count) {
            m_averageAmplitudes = new double[count];
        }

        int avgWidth = m_amplitudes.length / m_averageAmplitudes.length;

        for (int i = 0; i < m_averageAmplitudes.length; i++) {
            int start = Math.max(0, (int) Math.round(((double) i / m_averageAmplitudes.length) * m_amplitudes.length));
            int end = Math.min(m_amplitudes.length - 1, start + avgWidth);

            double sum = 0.0;
            for (int j = start; j <= end; j++) sum += m_amplitudes[j];

            m_averageAmplitudes[i] = sum / (end - start + 1);
        }
    }

    private void drawGrid() {
        m_gfx.setStroke(m_textColor);

        drawHorizontalLine(m_graphRectangle.getMinX() - 1, m_graphRectangle.getMaxX(), m_graphRectangle.getMaxY(), m_textColor);
        drawVerticalLine(m_graphRectangle.getMinY(), m_graphRectangle.getMaxY(), m_graphRectangle.getMinX() - 1, m_textColor);

        double hStep = m_graphRectangle.getWidth() / m_horizontalScales;
        double vStep = m_graphRectangle.getHeight() / m_verticalScales;

        for (int i = 1; i <= m_horizontalScales; i++) {
            drawVerticalLine(m_graphRectangle.getMinY(), m_graphRectangle.getMaxY(), m_graphRectangle.getMinX() + i * hStep, m_textColor);
        }

        for (int i = 0; i < m_verticalScales; i++) {
            drawHorizontalLine(m_graphRectangle.getMinX(), m_graphRectangle.getMaxX(), m_graphRectangle.getMinY() + i * vStep, m_textColor);
        }
    }

    private void drawHorizontalLine(double x1, double x2, double y, Color color) {
        for (int x = (int) x1; x <= (int) x2; x++) m_pixelGfx.setColor(x, (int) y, color);
    }

    private void drawVerticalLine(double y1, double y2, double x, Color color) {
        for (int y = (int) y1; y <= (int) y2; y++) m_pixelGfx.setColor((int) x, y, color);
    }
}