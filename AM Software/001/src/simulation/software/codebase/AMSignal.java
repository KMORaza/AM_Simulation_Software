package simulation.software.codebase;

public class AMSignal {
    private double[] time;
    private double[] signal;
    private double[] message;
    private double[] carrier;
    private double[] demodulatedSignal;
    private double[] frequency;
    private double[] spectrum;
    private String variant;

    public AMSignal(String variant, double carrierFreq, double[] messageFreqs, double modulationIndex, double phaseShift, String waveformType, String noiseType, double noiseAmplitude, String demodulationType) {
        this.variant = variant;
        ModulationAndDemodulation modDemod = new ModulationAndDemodulation(variant, carrierFreq, messageFreqs, modulationIndex, phaseShift, waveformType, noiseType, noiseAmplitude, demodulationType);
        
        this.time = modDemod.getTime();
        this.signal = modDemod.getModulatedSignal();
        this.message = modDemod.getMessageSignal();
        this.carrier = modDemod.getCarrierSignal();
        this.demodulatedSignal = modDemod.getDemodulatedSignal();
        this.frequency = modDemod.getFrequency();
        this.spectrum = modDemod.getSpectrum();
    }

    public double[] getTime() {
        return time;
    }

    public double[] getSignal() {
        return signal;
    }

    public double[] getMessage() {
        return message;
    }

    public double[] getCarrier() {
        return carrier;
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