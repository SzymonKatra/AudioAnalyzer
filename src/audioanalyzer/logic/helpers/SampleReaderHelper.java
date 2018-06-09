package audioanalyzer.logic.helpers;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;

/**
 * Helper class for reading samples from file
 */
public class SampleReaderHelper {
    private RandomAccessFile m_file;
    private byte[] m_fileContent;
    private byte[] m_buffer;
    private int m_fileLength;

    /**
     * Constructrs SampleReaderHelper
     * @param file
     * @param bufferEntireFile true to load entire file into memory, false to enable buffering
     * @throws IOException
     */
    public SampleReaderHelper(RandomAccessFile file, boolean bufferEntireFile) throws IOException {
        m_file = file;
        m_fileLength = (int)m_file.length();

        if (bufferEntireFile) {
            try {
                m_fileContent = new byte[m_fileLength];
                m_file.read(m_fileContent, 0, m_fileContent.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Read specified range of samples from the file
     * This method is thread-safe
     * @param sampleIndex
     * @param count
     * @param result
     * @param resultIndex
     * @throws IOException
     */
    public synchronized void readSamples(int sampleIndex, int count, double[] result, int resultIndex) throws IOException {
        if (m_buffer == null || m_buffer.length < count * 8) m_buffer = new byte[count * 8];

        int samplesCount = m_fileLength / 8;
        if (sampleIndex < 0) sampleIndex = 0;
        if (sampleIndex + count >= samplesCount) {
            count = samplesCount - sampleIndex;
        }

        if (m_fileContent == null) {
            m_file.seek(sampleIndex * 8);
            m_file.read(m_buffer, 0, count * 8);
        }
        else {
            System.arraycopy(m_fileContent, sampleIndex * 8, m_buffer, 0, count * 8);
        }

        for (int i = 0; i < count; i++) {
            long x = (Byte.toUnsignedLong(m_buffer[i * 8]) << 56) | (Byte.toUnsignedLong(m_buffer[i * 8 + 1]) << 48) |
                    (Byte.toUnsignedLong(m_buffer[i * 8 + 2]) << 40) | (Byte.toUnsignedLong(m_buffer[i * 8 + 3]) << 32) |
                    (Byte.toUnsignedLong(m_buffer[i * 8 + 4]) << 24) | (Byte.toUnsignedLong(m_buffer[i * 8 + 5]) << 16) |
                    (Byte.toUnsignedLong(m_buffer[i * 8 + 6]) << 8) | (Byte.toUnsignedLong(m_buffer[i * 8 + 7]));
            result[resultIndex + i] = Double.longBitsToDouble(x);
        }
    }
}
