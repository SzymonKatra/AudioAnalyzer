package audioanalyzer.logic;

public interface ISignalAnalyzer {
    void analyze(double[] samples);
    double[] getAmplitudes();
}
