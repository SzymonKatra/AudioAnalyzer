package audioanalyzer.logic;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;

public class SampleReaderHelper {
    private RandomAccessFile m_file;
    private byte[] m_buffer;

    public SampleReaderHelper(RandomAccessFile file) {
        m_file = file;
    }

    public void readSamples(int sampleIndex, int count, double[] result, int resultIndex) throws IOException {
        if (m_buffer == null || m_buffer.length < count * 8) m_buffer = new byte[count * 8];

        m_file.seek(sampleIndex * 8);
        m_file.read(m_buffer, 0, count * 8);

        for (int i = 0; i < count; i++) {
            long x = (Byte.toUnsignedLong(m_buffer[i * 8]) << 56) | (Byte.toUnsignedLong(m_buffer[i * 8 + 1]) << 48) |
                    (Byte.toUnsignedLong(m_buffer[i * 8 + 2]) << 40) | (Byte.toUnsignedLong(m_buffer[i * 8 + 3]) << 32) |
                    (Byte.toUnsignedLong(m_buffer[i * 8 + 4]) << 24) | (Byte.toUnsignedLong(m_buffer[i * 8 + 5]) << 16) |
                    (Byte.toUnsignedLong(m_buffer[i * 8 + 6]) << 8) | (Byte.toUnsignedLong(m_buffer[i * 8 + 7]));
            result[resultIndex + i] = Double.longBitsToDouble(x);
        }
    }
}
