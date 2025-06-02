package simulation.software.codebase;

import java.util.Random;

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

    public ModulationAndDemodulation(String variant, double carrierFreq, double[] messageFreqs, double modulationIndex, double phaseShift, String waveformType, String noiseType, double noiseAmplitude, String demodulationType) {
        this.variant = variant;
        int samples = 1000;
        double duration = 0.02; // 20 ms
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
            messageSignal[i] = generateMessageSignal(time[i], messageFreqs, waveformType);
            messageSignal[i] += generateNoise(noiseType, noiseAmplitude);
        }

        // Modulation
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
                for (int i = 0; i < samples; i++) {
                    modulatedSignal[i] = modulationIndex * messageSignal[i] * carrierSignal[i]; // Simplified SSB
                }
                break;
            case "vsb":
                for (int i = 0; i < samples; i++) {
                    modulatedSignal[i] = (0.5 + modulationIndex * messageSignal[i]) * carrierSignal[i]; // Simplified VSB
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

        // Demodulation
        if (!demodulationType.equals("None")) {
            if (demodulationType.equals("Coherent")) {
                demodulatedSignal = coherentDemodulation(modulatedSignal, carrierFreq, dt);
            } else if (demodulationType.equals("Non-Coherent")) {
                demodulatedSignal = nonCoherentDemodulation(modulatedSignal);
            }
        }

        // Frequency Domain (Simple FFT approximation)
        computeSpectrum(modulatedSignal, carrierFreq, dt);
    }

    private double generateMessageSignal(double t, double[] messageFreqs, String waveformType) {
        double sum = 0;
        for (double freq : messageFreqs) {
            switch (waveformType.toLowerCase()) {
                case "sine":
                    sum += Math.cos(2 * Math.PI * freq * t);
                    break;
                case "square":
                    sum += Math.signum(Math.cos(2 * Math.PI * freq * t));
                    break;
                case "triangle":
                    sum += 2 / Math.PI * Math.asin(Math.cos(2 * Math.PI * freq * t));
                    break;
            }
        }
        return sum / messageFreqs.length; // Normalize by number of tones
    }

    private double generateNoise(String noiseType, double amplitude) {
        switch (noiseType.toLowerCase()) {
            case "white":
                return amplitude * (random.nextDouble() * 2 - 1);
            case "gaussian":
                return amplitude * random.nextGaussian();
            case "pink":
                return amplitude * (random.nextDouble() * 2 - 1) / (1 + random.nextDouble());
            case "none":
            default:
                return 0;
        }
    }

    private double[] coherentDemodulation(double[] signal, double carrierFreq, double dt) {
        double[] demod = new double[signal.length];
        // Synchronous detection: Multiply with carrier and low-pass filter (simplified)
        for (int i = 0; i < signal.length; i++) {
            demod[i] = signal[i] * Math.cos(2 * Math.PI * carrierFreq * time[i]);
        }
        // Simple low-pass filter (moving average)
        double[] filtered = new double[signal.length];
        int window = 10;
        for (int i = 0; i < signal.length; i++) {
            double sum = 0;
            int count = 0;
            for (int j = Math.max(0, i - window); j < Math.min(signal.length, i + window); j++) {
                sum += demod[j];
                count++;
            }
            filtered[i] = sum / count;
        }
        return filtered;
    }

    private double[] nonCoherentDemodulation(double[] signal) {
        double[] demod = new double[signal.length];
        // Envelope detection: Rectify and low-pass filter (simplified)
        for (int i = 0; i < signal.length; i++) {
            demod[i] = Math.abs(signal[i]);
        }
        // Simple low-pass filter (moving average)
        double[] filtered = new double[signal.length];
        int window = 10;
        for (int i = 0; i < signal.length; i++) {
            double sum = 0;
            int count = 0;
            for (int j = Math.max(0, i - window); j < Math.min(signal.length, i + window); j++) {
                sum += demod[j];
                count++;
            }
            filtered[i] = sum / count;
        }
        return filtered;
    }

    private void computeSpectrum(double[] signal, double carrierFreq, double dt) {
        int n = signal.length;
        int m = n / 2;
        double fs = 1 / dt; // Sampling frequency
        for (int i = 0; i < m; i++) {
            frequency[i] = i * fs / n;
            double re = 0, im = 0;
            for (int j = 0; j < n; j++) {
                double angle = 2 * Math.PI * i * j / n;
                re += signal[j] * Math.cos(angle);
                im -= signal[j] * Math.sin(angle);
            }
            spectrum[i] = Math.sqrt(re * re + im * im) / n; // Magnitude
        }
    }

    public double[] getTime() {
        return time;
    }

    public double[] getModulatedSignal() {
        return modulatedSignal;
    }

    public double[] getMessageSignal() {
        return messageSignal;
    }

    public double[] getCarrierSignal() {
        return carrierSignal;
    }

    public double[] getDemodulatedSignal() {
        return demodulatedSignal;
    }

    public double[] getFrequency() {
        return frequency;
    }

    public double[] getSpectrum() {
        return spectrum;
    }
}