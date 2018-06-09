package audioanalyzer.logic.analyzers;

public interface ISignalAnalyzer {
    /**
     * Analyze given samples
     * @param samples
     */
    void analyze(double[] samples);
    /**
     * Returns array with amplitudes of each frequency.
     * Note: Call analyze(double[] samples) first
     * @return
     */
    double[] getAmplitudes();
}
