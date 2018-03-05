package audioanalyzer;

import org.apache.commons.math3.complex.Complex;

public class FourierTransform {
    private Complex[] m_tmp;

    public void computeHarmonics(double[] samples, double[] result) {
        if (m_tmp == null || m_tmp.length != samples.length / 2) {
            m_tmp = new Complex[samples.length / 2];
        }

        if (result.length < samples.length / 2) {
            throw new IllegalArgumentException("result need to be size of samples.length / 2 or more");
        }

        Complex[] input = new Complex[samples.length];
        for (int i = 0; i < samples.length; i++) {
            input[i] = new Complex(samples[i], 0);
        }

        fft(input, 0, input.length);

        for (int i = 0; i < samples.length / 2; i++)
        {
            result[i] = input[i].abs() * 2 / samples.length;
        }
    }

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

            Complex factor = new Complex(0, (-2.0 * Math.PI * (double) i) / (double) N).exp();

            samples[index + i] = even.add(factor.multiply(odd));
            samples[index + i + halfN] = even.add(factor.negate().multiply(odd));
        }
    }
}
