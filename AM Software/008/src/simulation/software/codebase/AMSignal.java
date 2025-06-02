package simulation.software.codebase;

/**
 * Represents an amplitude modulation signal with time, message, carrier, modulated, demodulated signals, and spectrum.
 */
public class AMSignal {
    private double[] time;
    private double[] signal;
    private double[] message;
    private double[] carrier;
    private double[] demodulatedSignal;
    private double[] frequency;
    private double[] spectrum;
    private String variant;

    /**
     * Constructor for AMSignal.
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
    public AMSignal(String variant, double carrierFreq, double[] messageFreqs, double[] amplitudes, double modulationIndex,
                    double phaseShift, String waveformType, String noiseType, double noiseAmplitude,
                    String demodulationType, double pulseDutyCycle, int samples, double duration, double filterAlpha) {
        this.variant = variant;
        ModulationAndDemodulation modDemod = new ModulationAndDemodulation(variant, carrierFreq, messageFreqs, amplitudes,
                modulationIndex, phaseShift, waveformType, noiseType, noiseAmplitude, demodulationType, pulseDutyCycle,
                samples, duration, filterAlpha);
        
        this.time = modDemod.getTime();
        this.signal = modDemod.getModulatedSignal();
        this.message = modDemod.getMessageSignal();
        this.carrier = modDemod.getCarrierSignal();
        this.demodulatedSignal = modDemod.getDemodulatedSignal();
        this.frequency = modDemod.getFrequency();
        this.spectrum = modDemod.getSpectrum();
    }

    /**
     * Gets the time array.
     * @return time array
     */
    public double[] getTime() {
        return time;
    }

    /**
     * Gets the modulated signal array.
     * @return modulated signal array
     */
    public double[] getSignal() {
        return signal;
    }

    /**
     * Gets the message signal array.
     * @return message signal array
     */
    public double[] getMessage() {
        return message;
    }

    /**
     * Gets the carrier signal array.
     * @return carrier signal array
     */
    public double[] getCarrier() {
        return carrier;
    }

    /**
     * Gets the demodulated signal array.
     * @return demodulated signal array
     */
    public double[] getDemodulatedSignal() {
        return demodulatedSignal;
    }

    /**
     * Gets the frequency array.
     * @return frequency array
     */
    public double[] getFrequency() {
        return frequency;
    }

    /**
     * Gets the spectrum array.
     * @return spectrum array
     */
    public double[] getSpectrum() {
        return spectrum;
    }
}