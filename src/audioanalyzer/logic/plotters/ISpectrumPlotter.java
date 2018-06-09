package audioanalyzer.logic.plotters;

public interface ISpectrumPlotter {
    /**
     * Sets range of frequencies that will be plotted.
     * @param frequencyRange
     */
    void setFrequencyRange(double frequencyRange);
    /**
     * Plots given amplitudes
     * @param amplitudes
     */
    void plot(double[] amplitudes);
    /**
     * Draws description for axes
     */
    void drawAxesDescription();
    /**
     * Gets average factor - smoothness
     * @return
     */
    double getAverageFactor();
    /**
     * Sets average factor - smoothness
     * @return
     */
    void setAverageFactor(double value);
}
