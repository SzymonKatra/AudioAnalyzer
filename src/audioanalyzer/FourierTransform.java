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
        //Complex[] output = fft(input);

        for (int i = 0; i < samples.length / 2; i++)
        {
            result[i] = (input[i].abs() / samples.length) * 2;
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

            double kFact = -2 * i * Math.PI / N;

            Complex factor = new Complex(Math.cos(kFact), Math.sin(kFact));

            samples[index + i] = even.add(factor.multiply(odd));
            samples[index + i + halfN] = even.subtract(factor.multiply(odd));
        }
    }

    /*public static Complex[] fft(Complex[] x) {
        int n = x.length;

        // base case
        if (n == 1) return new Complex[] { x[0] };

        // radix 2 Cooley-Tukey FFT
        if (n % 2 != 0) {
            throw new IllegalArgumentException("n is not a power of 2");
        }

        // fft of even terms
        Complex[] even = new Complex[n/2];
        for (int k = 0; k < n/2; k++) {
            even[k] = x[2*k];
        }
        Complex[] q = fft(even);

        // fft of odd terms
        Complex[] odd  = even;  // reuse the array
        for (int k = 0; k < n/2; k++) {
            odd[k] = x[2*k + 1];
        }
        Complex[] r = fft(odd);

        // combine
        Complex[] y = new Complex[n];
        for (int k = 0; k < n/2; k++) {
            double kth = -2 * k * Math.PI / n;
            Complex wk = new Complex(Math.cos(kth), Math.sin(kth));
            y[k]       = q[k].add(wk.multiply(r[k]));
            y[k + n/2] = q[k].subtract(wk.multiply(r[k]));
        }
        return y;
    }*/
}
