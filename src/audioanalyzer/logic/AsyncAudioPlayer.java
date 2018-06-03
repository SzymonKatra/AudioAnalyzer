package audioanalyzer.logic;

import java.io.IOException;
import java.util.concurrent.Semaphore;

/**
 * Asynchronous audio player.
 */
public class AsyncAudioPlayer {
    private class ReaderThread extends Thread {
        private AsyncAudioPlayer m_parent;
        private long m_currentSampleIndex;
        private long m_totalSamples;
        private int m_sampleRate;
        private double[] m_buffer;

        public ReaderThread(AsyncAudioPlayer parent) {
            m_parent = parent;
            m_currentSampleIndex = 0;
            m_totalSamples = m_parent.m_info.getTotalSamplesCount();
            m_sampleRate = m_parent.m_info.getSampleRate();
            m_buffer = new double[m_parent.m_info.getSampleRate() / 4];
        }

        public void run() {
            while (m_threadRunning) {
                boolean running;
                synchronized (m_parent) {
                    running = m_parent.m_running;
                    m_currentSampleIndex = (long) (m_parent.m_position * m_totalSamples);
                }

                if (!running) {
                    try {
                        m_parent.m_runSemaphore.acquire();
                    } catch (InterruptedException e) {
                        return;
                    }
                }

                m_player.start();

                while (running) {
                    double pos;
                    synchronized (m_parent) {
                        pos = m_parent.m_position;
                    }

                    long requiredPosition = (long) (pos * m_totalSamples) - m_sampleRate / 2;
                    long diff = m_currentSampleIndex - requiredPosition;
                    if (Math.abs(diff) < m_buffer.length * 2) {
                        if (diff < m_buffer.length) {
                            try {
                                m_reader.readSamples((int) m_currentSampleIndex, m_buffer.length, m_buffer, 0);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            m_currentSampleIndex += m_buffer.length;
                            m_player.play(m_buffer, 0, m_buffer.length);
                        }
                        else
                        {
                            int sleepFor = (int)((double)diff / m_sampleRate * 1000);
                            try {
                                Thread.sleep(sleepFor);
                            } catch (InterruptedException e) {
                                return;
                            }
                        }
                    } else {
                        m_player.discard();
                        m_currentSampleIndex = requiredPosition;
                        try {
                            m_reader.readSamples((int) m_currentSampleIndex, m_buffer.length, m_buffer, 0);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        m_currentSampleIndex += m_buffer.length;
                        m_player.play(m_buffer, 0, m_buffer.length);
                    }

                    synchronized (m_parent) {
                        running = m_parent.m_running;
                    }
                }

                m_player.stop();
            }
        }
    }

    private IPCMPlayer m_player;
    private SampleReaderHelper m_reader;
    private AudioStreamInfo m_info;
    private ReaderThread m_thread;
    private double m_position;
    private boolean m_running;
    private Semaphore m_runSemaphore;
    private double m_synchroFix = 0.4;
    private volatile boolean m_threadRunning = true;

    public AsyncAudioPlayer(IPCMPlayer player, SampleReaderHelper reader, AudioStreamInfo info) {
        m_player = player;
        m_reader = reader;
        m_info = info;
        m_running = false;
        m_runSemaphore = new Semaphore(0,true);

        m_thread = new ReaderThread(this);
        m_thread.setDaemon(true);
        m_thread.start();
    }

    /**
     * Set position of audio to play
     * @param position
     */
    public void setPosition(double position) {
        synchronized(this) {
            m_position = Math.min(position + (m_synchroFix / m_info.getDuration()), 1);
        }
    }

    /**
     * Starts playing audio
     */
    public void start() {
        synchronized (this) {
            m_running = true;
        }
        if (m_runSemaphore.availablePermits() < 1) {
            m_runSemaphore.release();
        }
    }

    /**
     * Stops playing audio
     */
    public void stop() {
        synchronized (this) {
            m_running = false;
        }
    }

    /**
     * Stops playing audio and terminates thread used for processing
     */
    public void dispose() {
        stop();
        m_runSemaphore.release();
        m_threadRunning = false;
    }

    /**
     *
     * @param fix Seconds
     */
    public void setSynchroFix(double fix) {
        m_synchroFix = fix;
    }

    public double getSynchroFix() {
        return m_synchroFix;
    }
}
