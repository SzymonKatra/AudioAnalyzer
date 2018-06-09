package audioanalyzer.logic.playback;

/**
 * Interface for PCM player
 */
public interface IPCMPlayer {
    /**
     * Enable player
     */
    void start();
    /**
     * Disable player
     */
    void stop();

    /**
     * Fill buffer with given samples
     * @param buffer
     * @param index
     * @param count
     */
    void play(double[] buffer, int index, int count);

    /**
     * Remove samples already provided
     */
    void discard();
}
