package simulation.software.codebase;

import java.util.Random;

/**
 * Handles signal modulation, demodulation, noise addition, and spectrum computation for AM variants.
 */
public class ModulationAndDemodulation {
    private double[] time;
    private double[] modulatedSignal;
    private double[] messageSignal;
    private double[] carrierSignal;
    private double[] demodulatedSignal;
    private double[] frequency;
    private double[] spectrum;
    private String variant;
    private Random random = new Random();

    /**
     * Constructor for ModulationAndDemodulation.
     *
     * @param variant         AM variant (DSB-AM, DSB-SC, SSB, VSB, QAM)
     * @param carrierFreq    Carrier frequency in Hz (50–5000)
     * @param messageFreqs   Array of message frequencies in Hz
     * @param amplitudes     Array of message amplitudes
     * @param modulationIndex Modulation index (0–2)
     * @param phaseShift     Phase shift in degrees (0–360, used for QAM)
     * @param waveformType   Waveform type (Sine, Square, Triangle, Sawtooth, Pulse)
     * @param noiseType      Noise type (None, White, Gaussian, Pink)
     * @param noiseAmplitude Noise amplitude (0–1)
     * @param demodulationType Demodulation type (None, Coherent, Non-Coherent)
     * @param pulseDutyCycle Pulse duty cycle (0–100%)
     * @param samples        Number of samples (1024–16384)
     * @param duration       Signal duration in seconds (0.01–1)
     * @param filterAlpha    Low-pass filter alpha (0.01–1)
     * @throws IllegalArgumentException if parameters are invalid
     */
    public ModulationAndDemodulation(String variant, double carrierFreq, double[] messageFreqs, double[] amplitudes,
                                    double modulationIndex, double phaseShift, String waveformType, String noiseType,
                                    double noiseAmplitude, String demodulationType, double pulseDutyCycle,
                                    int samples, double duration, double filterAlpha) {
        // Input validation
        if (carrierFreq < 50 || carrierFreq > 5000) {
            throw new IllegalArgumentException("Carrier frequency must be between 50 and 5000 Hz");
        }
        if (messageFreqs.length == 0 || messageFreqs.length != amplitudes.length) {
            throw new IllegalArgumentException("Message frequencies and amplitudes must be non-empty and match in length");
        }
        if (modulationIndex < 0 || modulationIndex > 2) {
            throw new IllegalArgumentException("Modulation index must be between 0 and 2");
        }
        if (variant.equalsIgnoreCase("QAM") && (phaseShift < 0 || phaseShift > 360)) {
            throw new IllegalArgumentException("Phase shift must be between 0 and 360 degrees for QAM");
        }
        if (pulseDutyCycle < 0 || pulseDutyCycle > 100) {
            throw new IllegalArgumentException("Pulse duty cycle must be between 0 and 100%");
        }
        if (noiseAmplitude < 0 || noiseAmplitude > 1) {
            throw new IllegalArgumentException("Noise amplitude must be between 0 and 1");
        }
        if (samples < 1024 || samples > 16384) {
            throw new IllegalArgumentException("Sample count must be between 1024 and 16384");
        }
        if (duration < 0.01 || duration > 1) {
            throw new IllegalArgumentException("Duration must be between 0.01 and 1 second");
        }
        if (filterAlpha < 0.01 || filterAlpha > 1) {
            throw new IllegalArgumentException("Filter alpha must be between 0.01 and 1");
        }

        this.variant = variant;
        time = new double[samples];
        modulatedSignal = new double[samples];
        messageSignal = new double[samples];
        carrierSignal = new double[samples];
        demodulatedSignal = new double[samples];
        frequency = new double[samples / 2];
        spectrum = new double[samples / 2];

        double dt = duration / samples;
        for (int i = 0; i < samples; i++) {
            time[i] = i * dt;
            carrierSignal[i] = Math.cos(2 * Math.PI * carrierFreq * time[i]);
            messageSignal[i] = generateMessageSignal(time[i], messageFreqs, amplitudes, waveformType, pulseDutyCycle);
            messageSignal[i] += generateNoise(noiseType, noiseAmplitude);
        }

        // Modulation based on variant
        switch (variant.toLowerCase()) {
            case "dsb-am":
                for (int i = 0; i < samples; i++) {
                    modulatedSignal[i] = (1 + modulationIndex * messageSignal[i]) * carrierSignal[i];
                }
                break;
            case "dsb-sc":
                for (int i = 0; i < samples; i++) {
                    modulatedSignal[i] = modulationIndex * messageSignal[i] * carrierSignal[i];
                }
                break;
            case "ssb":
                double[] hilbert = hilbertTransform(messageSignal);
                for (int i = 0; i < samples; i++) {
                    modulatedSignal[i] = modulationIndex * (messageSignal[i] * Math.cos(2 * Math.PI * carrierFreq * time[i])
                            - hilbert[i] * Math.sin(2 * Math.PI * carrierFreq * time[i]));
                }
                break;
            case "vsb":
                for (int i = 0; i < samples; i++) {
                    modulatedSignal[i] = (0.5 + modulationIndex * messageSignal[i]) * carrierSignal[i];
                }
                break;
            case "qam":
                for (int i = 0; i < samples; i++) {
                    double qamI = modulationIndex * messageSignal[i] * Math.cos(2 * Math.PI * carrierFreq * time[i]);
                    double qamQ = modulationIndex * messageSignal[i] * Math.cos(2 * Math.PI * carrierFreq * time[i] + Math.toRadians(phaseShift));
                    modulatedSignal[i] = qamI + qamQ;
                }
                break;
        }

        if (!demodulationType.equals("None")) {
            if (demodulationType.equals("Coherent")) {
                demodulatedSignal = coherentDemodulation(modulatedSignal, carrierFreq, dt, filterAlpha);
            } else if (demodulationType.equals("Non-Coherent")) {
                demodulatedSignal = nonCoherentDemodulation(modulatedSignal, filterAlpha);
            }
        }

        computeSpectrum(modulatedSignal, dt);
    }

    /**
     * Generates the message signal based on waveform type.
     */
    private double generateMessageSignal(double t, double[] messageFreqs, double[] amplitudes, String waveformType, double pulseDutyCycle) {
        double sum = 0;
        for (int i = 0; i < messageFreqs.length; i++) {
            double freq = messageFreqs[i];
            double amp = amplitudes[i];
            switch (waveformType.toLowerCase()) {
                case "sine":
                    sum += amp * Math.cos(2 * Math.PI * freq * t);
                    break;
                case "square":
                    sum += amp * Math.signum(Math.cos(2 * Math.PI * freq * t));
                    break;
                case "triangle":
                    sum += amp * (2 / Math.PI * Math.asin(Math.cos(2 * Math.PI * freq * t)));
                    break;
                case "sawtooth":
                    sum += amp * (2 * (freq * t - Math.floor(freq * t + 0.5)));
                    break;
                case "pulse":
                    double phase = 2 * Math.PI * freq * t;
                    sum += amp * (phase % (2 * Math.PI) < 2 * Math.PI * pulseDutyCycle / 100 ? 1 : -1);
                    break;
            }
        }
        return sum;
    }

    /**
     * Generates noise based on the specified type.
     */
    private double generateNoise(String noiseType, double amplitude) {
        switch (noiseType.toLowerCase()) {
            case "white":
                return amplitude * (random.nextDouble() * 2 - 1);
            case "gaussian":
                return amplitude * random.nextGaussian();
            case "pink":
                double pink = 0;
                for (int i = 1; i <= 5; i++) {
                    pink += (random.nextDouble() * 2 - 1) / i;
                }
                return amplitude * pink / 5;
            case "none":
            default:
                return 0;
        }
    }

    /**
     * Performs coherent demodulation with a phase-locked loop.
     */
    private double[] coherentDemodulation(double[] signal, double carrierFreq, double dt, double filterAlpha) {
        double[] demod = new double[signal.length];
        double phaseError = 0;
        double phase = 0;
        double k = 0.01;
        for (int i = 0; i < signal.length; i++) {
            double carrier = Math.cos(2 * Math.PI * carrierFreq * time[i] + phase);
            demod[i] = signal[i] * carrier;
            phaseError = demod[i] * Math.sin(2 * Math.PI * carrierFreq * time[i] + phase);
            phase += k * phaseError;
        }
        double[] filtered = new double[signal.length];
        filtered[0] = demod[0];
        for (int i = 1; i < signal.length; i++) {
            filtered[i] = filterAlpha * demod[i] + (1 - filterAlpha) * filtered[i - 1];
        }
        return filtered;
    }

    /**
     * Performs non-coherent demodulation (envelope detection).
     */
    private double[] nonCoherentDemodulation(double[] signal, double filterAlpha) {
        double[] demod = new double[signal.length];
        for (int i = 0; i < signal.length; i++) {
            demod[i] = Math.abs(signal[i]);
        }
        double[] filtered = new double[signal.length];
        filtered[0] = demod[0];
        for (int i = 1; i < signal.length; i++) {
            filtered[i] = filterAlpha * demod[i] + (1 - filterAlpha) * filtered[i - 1];
        }
        return filtered;
    }

    /**
     * Computes the Hilbert transform using FFT for SSB modulation.
     */
    private double[] hilbertTransform(double[] signal) {
        int n = signal.length;
        double[] re = new double[n];
        double[] im = new double[n];
        double[] result = new double[n];

        // Compute FFT
        fft(signal, re, im);

        // Apply Hilbert transform in frequency domain
        for (int i = 0; i < n; i++) {
            if (i == 0 || i == n / 2) {
                im[i] = 0; // Zero DC and Nyquist
            } else if (i < n / 2) {
                im[i] *= 2; // Double positive frequencies
            } else {
                im[i] = 0; // Zero negative frequencies
            }
            re[i] = 0;
        }

        // Inverse FFT
        ifft(re, im, result);
        return result;
    }

    /**
     * Computes the FFT of a signal (basic Cooley-Tukey implementation).
     */
    private void fft(double[] input, double[] re, double[] im) {
        int n = input.length;
        if (n == 1) {
            re[0] = input[0];
            im[0] = 0;
            return;
        }

        // Split even and odd
        double[] even = new double[n / 2];
        double[] odd = new double[n / 2];
        for (int i = 0; i < n / 2; i++) {
            even[i] = input[2 * i];
            odd[i] = input[2 * i + 1];
        }

        double[] reEven = new double[n / 2];
        double[] imEven = new double[n / 2];
        double[] reOdd = new double[n / 2];
        double[] imOdd = new double[n / 2];

        fft(even, reEven, imEven);
        fft(odd, reOdd, imOdd);

        for (int k = 0; k < n / 2; k++) {
            double angle = -2 * Math.PI * k / n;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            re[k] = reEven[k] + cos * reOdd[k] - sin * imOdd[k];
            im[k] = imEven[k] + sin * reOdd[k] + cos * imOdd[k];
            re[k + n / 2] = reEven[k] - (cos * reOdd[k] - sin * imOdd[k]);
            im[k + n / 2] = imEven[k] - (sin * reOdd[k] + cos * imOdd[k]);
        }
    }

    /**
     * Computes the inverse FFT.
     */
    private void ifft(double[] re, double[] im, double[] output) {
        int n = re.length;
        double[] reConj = new double[n];
        double[] imConj = new double[n];
        for (int i = 0; i < n; i++) {
            reConj[i] = re[i];
            imConj[i] = -im[i];
        }
        fft(reConj, imConj, output);
        for (int i = 0; i < n; i++) {
            output[i] = imConj[i] / n; // Normalize and use imaginary part for Hilbert
        }
    }

    /**
     * Computes the frequency spectrum using FFT.
     */
    private void computeSpectrum(double[] signal, double dt) {
        int n = signal.length;
        int m = n / 2;
        double fs = 1 / dt;
        double[] re = new double[n];
        double[] im = new double[n];
        fft(signal, re, im);
        for (int i = 0; i < m; i++) {
            frequency[i] = i * fs / n;
            spectrum[i] = 2 * Math.sqrt(re[i] * re[i] + im[i] * im[i]) / n;
        }
    }

    public double[] getTime() { return time; }
    public double[] getModulatedSignal() { return modulatedSignal; }
    public double[] getMessageSignal() { return messageSignal; }
    public double[] getCarrierSignal() { return carrierSignal; }
    public double[] getDemodulatedSignal() { return demodulatedSignal; }
    public double[] getFrequency() { return frequency; }
    public double[] getSpectrum() { return spectrum; }
}