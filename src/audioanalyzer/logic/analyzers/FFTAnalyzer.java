package audioanalyzer.logic.analyzers;

import org.apache.commons.math3.complex.Complex;

/**
 * Fast Fourier Transform audio analyzer
 */
public class FFTAnalyzer implements ISignalAnalyzer {
    private Complex[] m_tmp;
    private double[] m_samples;
    private double[] m_amplitudes;

    /**
     * Analyze given samples
     * @param samples
     */
    public void analyze(double[] samples) {
        m_samples = samples;

        if (m_tmp == null || m_tmp.length != samples.length / 2) {
            m_tmp = new Complex[samples.length / 2];
        }

        if (m_amplitudes == null || m_amplitudes.length != m_samples.length / 2) {
            m_amplitudes = new double[m_samples.length / 2];
        }

        Complex[] input = new Complex[samples.length];
        for (int i = 0; i < samples.length; i++) {
            input[i] = new Complex(hammingWindow(samples[i], i, samples.length), 0);
        }

        //fft(input, 0, input.length);
        fastFFT(input);

        for (int i = 0; i < samples.length / 2; i++) {
            m_amplitudes[i] = (input[i].abs() / samples.length) * 2;
        }
    }

    /**
     * Returns array with amplitudes of each frequency.
     * Note: Call analyze(double[] samples) first
     * @return
     */
    public double[] getAmplitudes() {
        return m_amplitudes;
    }

    /**
     * Fast, Non-recursive implementation of FFT
     * https://introcs.cs.princeton.edu/java/97data/InplaceFFT.java.html
     * compute the FFT of x[], assuming its length is a power of 2
     * @param x
     */
    private static void fastFFT(Complex[] x) {

        // check that length is a power of 2
        int n = x.length;
        if (Integer.highestOneBit(n) != n) {
            throw new RuntimeException("n is not a power of 2");
        }

        // bit reversal permutation
        int shift = 1 + Integer.numberOfLeadingZeros(n);
        for (int k = 0; k < n; k++) {
            int j = Integer.reverse(k) >>> shift;
            if (j > k) {
                Complex temp = x[j];
                x[j] = x[k];
                x[k] = temp;
            }
        }

        // butterfly updates
        for (int L = 2; L <= n; L = L+L) {
            for (int k = 0; k < L/2; k++) {
                double kth = -2 * k * Math.PI / L;
                Complex w = new Complex(Math.cos(kth), Math.sin(kth));
                for (int j = 0; j < n/L; j++) {
                    Complex tao = w.multiply(x[j*L + k + L/2]);
                    x[j*L + k + L/2] = x[j*L + k].subtract(tao);
                    x[j*L + k]       = x[j*L + k].add(tao);
                }
            }
        }
    }

    /**
     * Recursive implementation of FFT
     * @param samples
     * @param index
     * @param count
     */
    private void fft(Complex[] samples, int index, int count) {
        if (count <= 1) return;

        int N = count;
        int halfN = N / 2;

        // copy odd samples to temporary buffer
        for (int i = 0; i < halfN; i++) m_tmp[i] = samples[index + i * 2 + 1];

        // move even samples to first part of the buffer
        for (int i = 0; i < halfN; i++) samples[index + i] = samples[index + i * 2];

        // move odd samples to second part of the buffer
        for (int i = 0; i < halfN; i++) samples[index + i + halfN] = m_tmp[i];

        fft(samples, index, halfN);
        fft(samples, index + halfN, halfN);

        // combine results
        for (int i = 0; i < halfN; i++) {
            Complex even = samples[index + i];
            Complex odd = samples[index + halfN + i];

            double kFact = -2 * i * Math.PI / N;

            Complex factor = new Complex(Math.cos(kFact), Math.sin(kFact));

            samples[index + i] = even.add(factor.multiply(odd));
            samples[index + i + halfN] = even.subtract(factor.multiply(odd));
        }
    }

    /**
     * Hamming window function
     * @param sample
     * @param sampleIndex
     * @param sampleCount
     * @return
     */
    private double hammingWindow(double sample, int sampleIndex, int sampleCount)
    {
        return sample * (0.53836 - 0.46164 * Math.cos((2 * Math.PI * sampleIndex) / (sampleCount - 1)));
    }
}
