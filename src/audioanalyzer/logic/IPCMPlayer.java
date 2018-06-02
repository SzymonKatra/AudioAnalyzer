package audioanalyzer.logic;

public interface IPCMPlayer {
    void start();
    void stop();
    void play(double[] buffer, int index, int count);
    void discard();
}
