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

    public ModulationAndDemodulation(String variant, double carrierFreq, double[] messageFreqs, double[] amplitudes, double modulationIndex, double phaseShift, String waveformType, String noiseType, double noiseAmplitude, String demodulationType, double pulseDutyCycle) {
        this.variant = variant;
        int samples = 4096;
        double duration = 0.05;
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
                    double hilbert = messageSignal[Math.min(i + samples / 4, samples - 1)];
                    modulatedSignal[i] = modulationIndex * (messageSignal[i] * Math.cos(2 * Math.PI * carrierFreq * time[i]) - hilbert * Math.sin(2 * Math.PI * carrierFreq * time[i]));
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
                demodulatedSignal = coherentDemodulation(modulatedSignal, carrierFreq, dt);
            } else if (demodulationType.equals("Non-Coherent")) {
                demodulatedSignal = nonCoherentDemodulation(modulatedSignal);
            }
        }

        computeSpectrum(modulatedSignal, dt);
    }

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

    private double[] coherentDemodulation(double[] signal, double carrierFreq, double dt) {
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
        double alpha = 0.1;
        filtered[0] = demod[0];
        for (int i = 1; i < signal.length; i++) {
            filtered[i] = alpha * demod[i] + (1 - alpha) * filtered[i - 1];
        }
        return filtered;
    }

    private double[] nonCoherentDemodulation(double[] signal) {
        double[] demod = new double[signal.length];
        for (int i = 0; i < signal.length; i++) {
            demod[i] = Math.abs(signal[i]);
        }
        double[] filtered = new double[signal.length];
        double alpha = 0.1;
        filtered[0] = demod[0];
        for (int i = 1; i < signal.length; i++) {
            filtered[i] = alpha * demod[i] + (1 - alpha) * filtered[i - 1];
        }
        return filtered;
    }

    private void computeSpectrum(double[] signal, double dt) {
        int n = signal.length;
        int m = n / 2;
        double fs = 1 / dt;
        for (int i = 0; i < m; i++) {
            frequency[i] = i * fs / n;
            double re = 0, im = 0;
            for (int j = 0; j < n; j++) {
                double angle = 2 * Math.PI * i * j / n;
                re += signal[j] * Math.cos(angle);
                im -= signal[j] * Math.sin(angle);
            }
            spectrum[i] = 2 * Math.sqrt(re * re + im * im) / n;
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