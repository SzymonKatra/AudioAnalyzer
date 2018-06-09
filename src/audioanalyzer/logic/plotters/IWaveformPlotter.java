package audioanalyzer.logic.plotters;

import java.io.IOException;

public interface IWaveformPlotter {
    /**
     * Reads all samples from the file in order to rebuild waveform
     */
    void rebuildWaveform();
    /**
     * Plots already computed waveform
     * @param position
     * @param analyzeWidth
     * @throws IOException
     */
    void plot(double position, double analyzeWidth) throws IOException;
    /**
     * Draws timeline below waveform
     */
    void drawTimeline();

    double getX();
    double getWidth();
    double getY();
    double getHeight();
}
