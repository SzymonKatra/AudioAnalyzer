package audioanalyzer.logic;

import java.io.IOException;
import java.util.concurrent.Semaphore;

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
            while (true) {
                boolean running;
                synchronized (m_parent) {
                    running = m_parent.m_running;
                    m_currentSampleIndex = (long) (m_parent.m_position * m_totalSamples);
                }

                if (!running) {
                    try {
                        m_parent.m_runSemaphore.acquire();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                m_player.start();

                while (running) {
                    double pos;
                    synchronized (m_parent) {
                        pos = m_parent.m_position;
                        running = m_parent.m_running;
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

    public AsyncAudioPlayer(IPCMPlayer player, SampleReaderHelper reader, AudioStreamInfo info) {
        m_player = player;
        m_reader = reader;
        m_info = info;
        m_running = false;
        m_runSemaphore = new Semaphore(0,true);

        m_thread = new ReaderThread(this);
        m_thread.start();
    }

    public void setPosition(double position) {
        synchronized(this) {
            m_position = position;
        }
    }

    public void start() {
        synchronized (this) {
            m_running = true;
        }
        if (m_runSemaphore.availablePermits() < 1) {
            m_runSemaphore.release();
        }
    }

    public void stop() {
        synchronized (this) {
            m_running = false;
        }
    }
}
