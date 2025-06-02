package simulation.software.codebase;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Window for displaying Total Harmonic Distortion (THD) analysis of AM signals.
 */
public class TotalHarmonicDistortion extends JFrame {
    private static final Logger LOGGER = Logger.getLogger(TotalHarmonicDistortion.class.getName());
    private static final int WINDOW_SIZE = 1024; // FFT window size
    private AMSignal signal;
    private String variant;
    private double thdPercentage;
    private double fundamentalFreq;
    private double[] harmonicMagnitudes;
    private double[] frequencies;
    private double[] magnitudes;

    /**
     * Constructor for TotalHarmonicDistortion.
     *
     * @param signal  The AM signal to analyze
     * @param variant The AM variant (DSB-AM, DSB-SC, SSB, VSB, QAM)
     */
    public TotalHarmonicDistortion(AMSignal signal, String variant) {
        this.signal = signal;
        this.variant = variant;
        if (signal == null || signal.getSignal() == null || signal.getSignal().length < WINDOW_SIZE) {
            LOGGER.warning("Invalid or insufficient signal data for THD analysis.");
            JOptionPane.showMessageDialog(null, "Invalid or insufficient signal data for THD analysis.", "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        setTitle(variant + " Total Harmonic Distortion Analysis");
        setSize(800, 600);
        setMinimumSize(new Dimension(600, 400));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(60, 60, 60));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // THD Display Panel
        JPanel thdPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        thdPanel.setBackground(new Color(60, 60, 60));
        thdPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "THD Value", 0, 0, new Font("SansSerif", Font.BOLD, 14), new Color(180, 180, 180)));
        JLabel thdLabel = new JLabel("Computing THD...");
        thdLabel.setForeground(new Color(180, 180, 180));
        thdLabel.setFont(new Font("Bahnschrift", Font.BOLD, 16));
        thdPanel.add(thdLabel);
        mainPanel.add(thdPanel, BorderLayout.NORTH);

        // Spectrum Plot Panel
        JPanel plotPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawSpectrumPlot(g);
            }
        };
        plotPanel.setBackground(new Color(60, 60, 60));
        plotPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Spectrum with Harmonics", 0, 0, new Font("SansSerif", Font.BOLD, 14), new Color(180, 180, 180)));
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

        // Compute THD
        computeTHD();
        thdLabel.setText(String.format("THD: %.2f%%", thdPercentage));

        // Window closing event
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                LOGGER.info("TotalHarmonicDistortion window closed for " + variant);
            }
        });

        LOGGER.info("TotalHarmonicDistortion window initialized for " + variant);
        setVisible(true);
    }

    /**
     * Computes the THD based on the signal's FFT spectrum.
     */
    private void computeTHD() {
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

        // Find fundamental frequency (strongest peak in plausible range)
        fundamentalFreq = findFundamentalFrequency(frequencies, magnitudes);
        if (fundamentalFreq == 0) {
            LOGGER.warning("Could not identify fundamental frequency for THD.");
            thdPercentage = 0;
            harmonicMagnitudes = new double[0];
            return;
        }

        // Identify harmonic magnitudes (up to 10th harmonic or Nyquist)
        harmonicMagnitudes = new double[10];
        double fundamentalMagnitude = 0;
        double frequencyResolution = frequencies[1] - frequencies[0];
        for (int i = 0; i < frequencies.length; i++) {
            if (Math.abs(frequencies[i] - fundamentalFreq) < frequencyResolution) {
                fundamentalMagnitude = magnitudes[i];
            }
            for (int h = 2; h <= 10; h++) {
                if (Math.abs(frequencies[i] - h * fundamentalFreq) < frequencyResolution) {
                    harmonicMagnitudes[h - 1] = magnitudes[i];
                }
            }
        }

        // Compute THD
        double harmonicPowerSum = 0;
        for (double mag : harmonicMagnitudes) {
            harmonicPowerSum += mag * mag;
        }
        double fundamentalPower = fundamentalMagnitude * fundamentalMagnitude;
        if (fundamentalPower == 0) {
            thdPercentage = 0;
        } else {
            thdPercentage = Math.sqrt(harmonicPowerSum / fundamentalPower) * 100;
        }

        LOGGER.info("THD computed: " + thdPercentage + "% for fundamental frequency: " + fundamentalFreq + " Hz");
    }

    /**
     * Finds the fundamental frequency by selecting the strongest peak in the spectrum.
     */
    private double findFundamentalFrequency(double[] frequencies, double[] magnitudes) {
        double maxMagnitude = 0;
        int maxIndex = 0;
        double minFreq = 10.0; // Minimum plausible frequency (based on ControlPanel constraints)
        double maxFreq = 5000.0; // Maximum plausible frequency
        for (int i = 0; i < frequencies.length; i++) {
            if (frequencies[i] >= minFreq && frequencies[i] <= maxFreq && magnitudes[i] > maxMagnitude) {
                maxMagnitude = magnitudes[i];
                maxIndex = i;
            }
        }

        if (maxMagnitude > 0) {
            return frequencies[maxIndex];
        }
        return 0; // No valid fundamental found
    }

    /**
     * Draws the spectrum plot with fundamental and harmonic peaks highlighted.
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
        double[] sampledMag = new double[displayPoints];
        for (int i = 0; i < displayPoints; i++) {
            int idx = i * step;
            sampledFreq[i] = frequencies[idx];
            sampledMag[i] = magnitudes[idx];
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

        // Plot spectrum
        g2d.setColor(Color.MAGENTA);
        for (int i = 0; i < displayPoints - 1; i++) {
            int x1 = margin + (int) (sampledFreq[i] / maxFreq * (width - 2 * margin));
            int x2 = margin + (int) (sampledFreq[i + 1] / maxFreq * (width - 2 * margin));
            int y1 = height - margin - (int) (sampledMag[i] / maxMagnitude * (height / 2 - margin));
            int y2 = height - margin - (int) (sampledMag[i + 1] / maxMagnitude * (height / 2 - margin));
            g2d.drawLine(x1, y1, x2, y2);
        }

        // Highlight fundamental and harmonics
        g2d.setFont(new Font("Bahnschrift", Font.BOLD, 12));
        double frequencyResolution = frequencies[1] - frequencies[0];
        for (int i = 0; i < displayPoints; i++) {
            double freq = sampledFreq[i];
            if (Math.abs(freq - fundamentalFreq) < frequencyResolution) {
                g2d.setColor(Color.GREEN);
                int x = margin + (int) (freq / maxFreq * (width - 2 * margin));
                int y = height - margin - (int) (sampledMag[i] / maxMagnitude * (height / 2 - margin));
                g2d.drawString("F1: " + String.format("%.0f Hz", freq), x + 5, y - 5);
            }
            for (int h = 2; h <= 10; h++) {
                if (Math.abs(freq - h * fundamentalFreq) < frequencyResolution && harmonicMagnitudes[h - 1] > 0) {
                    g2d.setColor(Color.RED);
                    int x = margin + (int) (freq / maxFreq * (width - 2 * margin));
                    int y = height - margin - (int) (sampledMag[i] / maxMagnitude * (height / 2 - margin));
                    g2d.drawString("H" + h + ": " + String.format("%.0f Hz", freq), x + 5, y - 5);
                }
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