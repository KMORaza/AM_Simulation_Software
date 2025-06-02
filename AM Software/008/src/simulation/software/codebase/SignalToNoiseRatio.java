package simulation.software.codebase;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Window for displaying Signal-to-Noise Ratio (SNR) analysis of AM signals.
 */
public class SignalToNoiseRatio extends JFrame {
    private static final Logger LOGGER = Logger.getLogger(SignalToNoiseRatio.class.getName());
    private static final int WINDOW_SIZE = 1024; // FFT window size
    private AMSignal signal;
    private String variant;
    private double snrDb;
    private double[] frequencies;
    private double[] magnitudes;
    private double[] signalMagnitudes;
    private double[] noiseMagnitudes;

    /**
     * Constructor for SignalToNoiseRatio.
     *
     * @param signal  The AM signal to analyze
     * @param variant The AM variant (DSB-AM, DSB-SC, SSB, VSB, QAM)
     */
    public SignalToNoiseRatio(AMSignal signal, String variant) {
        this.signal = signal;
        this.variant = variant;
        if (signal == null || signal.getSignal() == null || signal.getSignal().length < WINDOW_SIZE) {
            LOGGER.warning("Invalid or insufficient signal data for SNR analysis.");
            JOptionPane.showMessageDialog(null, "Invalid or insufficient signal data for SNR analysis.", "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        setTitle(variant + " Signal-to-Noise Ratio Analysis");
        setSize(800, 600);
        setMinimumSize(new Dimension(600, 400));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(60, 60, 60));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // SNR Display Panel
        JPanel snrPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        snrPanel.setBackground(new Color(60, 60, 60));
        snrPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "SNR Value", 0, 0, new Font("SansSerif", Font.BOLD, 14), new Color(180, 180, 180)));
        JLabel snrLabel = new JLabel("Computing SNR...");
        snrLabel.setForeground(new Color(180, 180, 180));
        snrLabel.setFont(new Font("Bahnschrift", Font.BOLD, 16));
        snrPanel.add(snrLabel);
        mainPanel.add(snrPanel, BorderLayout.NORTH);

        // Spectrum Plot Panel
        JPanel plotPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawSpectrumPlot(g);
            }
        };
        plotPanel.setBackground(new Color(60, 60, 60));
        plotPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Spectrum (Signal vs. Noise)", 0, 0, new Font("SansSerif", Font.BOLD, 14), new Color(180, 180, 180)));
        mainPanel.add(plotPanel, BorderLayout.CENTER);

        // Control Panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        controlPanel.setBackground(new Color(60, 60, 60));
        JButton closeButton = new JButton("Close");
        closeButton.setBackground(new Color(80, 80, 80));
        closeButton.setForeground(new Color(180, 180, 180));
        closeButton.setFont(new Font("Bahnschrift", Font.BOLD, 13));
        closeButton.addActionListener(e -> dispose());
        controlPanel.add(closeButton);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Compute SNR
        computeSNR();
        snrLabel.setText(String.format("SNR: %.2f dB", snrDb));

        // Window closing event
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                LOGGER.info("SignalToNoiseRatio window closed for " + variant);
            }
        });

        LOGGER.info("SignalToNoiseRatio window initialized for " + variant);
        setVisible(true);
    }

    /**
     * Computes the SNR based on the signal's FFT spectrum.
     */
    private void computeSNR() {
        double[] modulatedSignal = signal.getSignal();
        double duration = signal.getTime()[signal.getTime().length - 1];
        int signalLength = modulatedSignal.length;

        // Extract first window of signal
        double[] windowedSignal = new double[WINDOW_SIZE];
        for (int i = 0; i < WINDOW_SIZE && i < signalLength; i++) {
            // Apply Hamming window
            double window = 0.54 - 0.46 * Math.cos(2 * Math.PI * i / (WINDOW_SIZE - 1));
            windowedSignal[i] = modulatedSignal[i] * window;
        }

        // Compute FFT
        double samplingRate = signalLength / duration;
        double[][] fftResult = computeFFT(windowedSignal, samplingRate);
        frequencies = fftResult[0];
        magnitudes = fftResult[1];

        // Initialize signal and noise magnitude arrays
        signalMagnitudes = new double[magnitudes.length];
        noiseMagnitudes = new double[magnitudes.length];
        System.arraycopy(magnitudes, 0, noiseMagnitudes, 0, magnitudes.length); // Start with all as noise

        // Identify signal components (carrier and sidebands)
        double carrierFreq = findDominantFrequency(frequencies, magnitudes);
        double frequencyResolution = frequencies[1] - frequencies[0];
        double sidebandWidth = 50.0; // Assume max message frequency ~50 Hz (adjustable)
        double signalPower = 0;
        double noisePower = 0;

        // Define signal bands (carrier ± sidebands)
        for (int i = 0; i < frequencies.length; i++) {
            double freq = frequencies[i];
            // Check if frequency is near carrier or sidebands
            if (Math.abs(freq - carrierFreq) < sidebandWidth || // Carrier
                (freq > carrierFreq && Math.abs(freq - (carrierFreq + sidebandWidth)) < frequencyResolution) || // Upper sideband
                (freq < carrierFreq && Math.abs(freq - (carrierFreq - sidebandWidth)) < frequencyResolution)) { // Lower sideband
                signalMagnitudes[i] = magnitudes[i];
                noiseMagnitudes[i] = 0;
                signalPower += Math.pow(magnitudes[i], 2);
            } else {
                signalMagnitudes[i] = 0;
                noisePower += Math.pow(magnitudes[i], 2);
            }
        }

        // Compute SNR
        if (noisePower == 0) {
            snrDb = Double.POSITIVE_INFINITY; // No noise
        } else if (signalPower == 0) {
            snrDb = Double.NEGATIVE_INFINITY; // No signal
        } else {
            snrDb = 10 * Math.log10(signalPower / noisePower);
        }

        LOGGER.info("SNR computed: " + snrDb + " dB for estimated carrier frequency: " + carrierFreq + " Hz");
    }

    /**
     * Finds the dominant frequency in the spectrum.
     */
    private double findDominantFrequency(double[] frequencies, double[] magnitudes) {
        double maxMagnitude = 0;
        int maxIndex = 0;
        double minFreq = 50.0; // Based on ControlPanel carrier constraints
        double maxFreq = 5000.0;
        for (int i = 0; i < frequencies.length; i++) {
            if (frequencies[i] >= minFreq && frequencies[i] <= maxFreq && magnitudes[i] > maxMagnitude) {
                maxMagnitude = magnitudes[i];
                maxIndex = i;
            }
        }
        return maxMagnitude > 0 ? frequencies[maxIndex] : 1000.0; // Default to 1000 Hz if no peak
    }

    /**
     * Draws the spectrum plot with signal and noise components highlighted.
     */
    private void drawSpectrumPlot(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (frequencies == null || magnitudes == null || frequencies.length == 0) {
            g2d.setColor(new Color(180, 180, 180));
            g2d.setFont(new Font("Bahnschrift", Font.BOLD, 14));
            g2d.drawString("No spectrum data", getWidth() / 2 - 50, getHeight() / 2);
            return;
        }

        int width = getWidth();
        int height = getHeight();
        int margin = 50;

        // Compute max magnitude for scaling
        double maxMagnitude = 0;
        for (double m : magnitudes) {
            if (m > maxMagnitude) maxMagnitude = m;
        }
        if (maxMagnitude == 0) maxMagnitude = 1;

        // Downsample for performance
        int displayPoints = Math.min(frequencies.length, 1000);
        int step = Math.max(1, frequencies.length / displayPoints);
        double[] sampledFreq = new double[displayPoints];
        double[] sampledSignalMag = new double[displayPoints];
        double[] sampledNoiseMag = new double[displayPoints];
        for (int i = 0; i < displayPoints; i++) {
            int idx = i * step;
            sampledFreq[i] = frequencies[idx];
            sampledSignalMag[i] = signalMagnitudes[idx];
            sampledNoiseMag[i] = noiseMagnitudes[idx];
        }

        // Draw grid
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(0.5f));
        int numVertical = 10;
        for (int i = 0; i <= numVertical; i++) {
            int x = margin + i * (width - 2 * margin) / numVertical;
            g2d.drawLine(x, margin, x, height - margin);
        }
        int numHorizontal = 8;
        for (int i = 0; i <= numHorizontal; i++) {
            int y = margin + i * (height - 2 * margin) / numHorizontal;
            g2d.drawLine(margin, y, width - margin, y);
        }

        // Draw axes
        g2d.setColor(new Color(180, 180, 180));
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawLine(margin, height - margin, width - margin, height - margin); // X-axis
        g2d.drawLine(margin, margin, margin, height - margin); // Y-axis

        // Draw axis labels
        g2d.setFont(new Font("Bahnschrift", Font.PLAIN, 12));
        g2d.setColor(new Color(180, 180, 180));
        double maxFreq = frequencies[frequencies.length - 1];
        for (int i = 0; i <= numVertical; i++) {
            double f = i * maxFreq / numVertical;
            int x = margin + i * (width - 2 * margin) / numVertical;
            g2d.drawString(String.format("%.0fHz", f), x - 10, height - margin + 15);
        }
        for (int i = 0; i <= numHorizontal; i++) {
            double amp = maxMagnitude * (1 - i * 1.0 / numHorizontal);
            int y = margin + i * (height - 2 * margin) / numHorizontal;
            g2d.drawString(String.format("%.2f", amp), margin - 40, y + 5);
        }

        // Plot noise spectrum
        g2d.setColor(Color.RED);
        for (int i = 0; i < displayPoints - 1; i++) {
            int x1 = margin + (int) (sampledFreq[i] / maxFreq * (width - 2 * margin));
            int x2 = margin + (int) (sampledFreq[i + 1] / maxFreq * (width - 2 * margin));
            int y1 = height - margin - (int) (sampledNoiseMag[i] / maxMagnitude * (height / 2 - margin));
            int y2 = height - margin - (int) (sampledNoiseMag[i + 1] / maxMagnitude * (height / 2 - margin));
            g2d.drawLine(x1, y1, x2, y2);
        }

        // Plot signal spectrum
        g2d.setColor(Color.GREEN);
        for (int i = 0; i < displayPoints - 1; i++) {
            int x1 = margin + (int) (sampledFreq[i] / maxFreq * (width - 2 * margin));
            int x2 = margin + (int) (sampledFreq[i + 1] / maxFreq * (width - 2 * margin));
            int y1 = height - margin - (int) (sampledSignalMag[i] / maxMagnitude * (height / 2 - margin));
            int y2 = height - margin - (int) (sampledSignalMag[i + 1] / maxMagnitude * (height / 2 - margin));
            g2d.drawLine(x1, y1, x2, y2);
        }

        // Label significant signal peaks
        g2d.setFont(new Font("Bahnschrift", Font.BOLD, 12));
        double frequencyResolution = frequencies[1] - frequencies[0];
        for (int i = 0; i < displayPoints; i++) {
            if (sampledSignalMag[i] > maxMagnitude * 0.1) { // Significant signal peak
                g2d.setColor(Color.GREEN);
                int x = margin + (int) (sampledFreq[i] / maxFreq * (width - 2 * margin));
                int y = height - margin - (int) (sampledSignalMag[i] / maxMagnitude * (height / 2 - margin));
                g2d.drawString(String.format("%.0f Hz", sampledFreq[i]), x + 5, y - 5);
            }
        }
    }

    /**
     * Computes the FFT of a signal and returns frequency and magnitude arrays.
     *
     * @param signal       The input signal (time-domain)
     * @param samplingRate The sampling rate in Hz
     * @return A double[][] where result[0] is frequencies, result[1] is magnitudes
     */
    private double[][] computeFFT(double[] signal, double samplingRate) {
        if (signal == null || signal.length == 0) {
            LOGGER.warning("Invalid signal for FFT computation");
            throw new IllegalArgumentException("Signal cannot be null or empty");
        }

        int n = signal.length;
        // Ensure power of 2 for FFT (pad with zeros if needed)
        int fftSize = 1;
        while (fftSize < n) {
            fftSize <<= 1;
        }
        double[] paddedSignal = new double[fftSize];
        System.arraycopy(signal, 0, paddedSignal, 0, n);

        // Compute FFT
        Complex[] fft = new Complex[fftSize];
        for (int i = 0; i < fftSize; i++) {
            fft[i] = new Complex(i < n ? paddedSignal[i] : 0, 0);
        }
        fft = fft(fft); // Recursive FFT

        // Compute frequencies and magnitudes
        double[] frequencies = new double[fftSize / 2];
        double[] magnitudes = new double[fftSize / 2];
        double nyquist = samplingRate / 2.0;
        for (int i = 0; i < fftSize / 2; i++) {
            frequencies[i] = i * nyquist / (fftSize / 2.0);
            magnitudes[i] = fft[i].abs() / fftSize * 2; // Normalize
        }

        LOGGER.info("FFT computed for signal of length " + n);
        return new double[][]{frequencies, magnitudes};
    }

    // Helper FFT implementation (Cooley-Tukey)
    private Complex[] fft(Complex[] x) {
        int n = x.length;
        if (n <= 1) return x;

        // Divide
        Complex[] even = new Complex[n / 2];
        Complex[] odd = new Complex[n / 2];
        for (int i = 0; i < n / 2; i++) {
            even[i] = x[2 * i];
            odd[i] = x[2 * i + 1];
        }

        // Conquer
        even = fft(even);
        odd = fft(odd);

        // Combine
        Complex[] result = new Complex[n];
        for (int k = 0; k < n / 2; k++) {
            double angle = -2 * Math.PI * k / n;
            Complex t = new Complex(Math.cos(angle), Math.sin(angle)).times(odd[k]);
            result[k] = even[k].plus(t);
            result[k + n / 2] = even[k].minus(t);
        }
        return result;
    }

    // Helper Complex class for FFT
    private static class Complex {
        private final double re;
        private final double im;

        public Complex(double real, double imag) {
            this.re = real;
            this.im = imag;
        }

        public Complex plus(Complex b) {
            return new Complex(re + b.re, im + b.im);
        }

        public Complex minus(Complex b) {
            return new Complex(re - b.re, im - b.im);
        }

        public Complex times(Complex b) {
            return new Complex(re * b.re - im * b.im, re * b.im + im * b.re);
        }

        public double abs() {
            return Math.sqrt(re * re + im * im);
        }
    }
}