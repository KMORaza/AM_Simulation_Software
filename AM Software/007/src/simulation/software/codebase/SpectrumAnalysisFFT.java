package simulation.software.codebase;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Window for displaying dynamic FFT-based frequency spectrum analysis of AM signals.
 */
public class SpectrumAnalysisFFT extends JFrame {
    private static final Logger LOGGER = Logger.getLogger(SpectrumAnalysisFFT.class.getName());
    private static final int FPS = 60;
    private static final int WINDOW_SIZE = 1024; // FFT window size
    private AMSignal signal;
    private String variant;
    private double zoom = 1.0;
    private double pan = 0;
    private int mouseX;
    private int windowIndex = 0;
    private boolean isPaused = false;
    private Timer animationTimer;
    private double[] currentFreq;
    private double[] currentSpectrum;
    private static List<WeakReference<SpectrumAnalysisFFT>> openWindows = new ArrayList<>();

    /**
     * Constructor for SpectrumAnalysisFFT.
     *
     * @param signal  The AM signal to analyze
     * @param variant The AM variant (DSB-AM, DSB-SC, SSB, VSB, QAM)
     */
    public SpectrumAnalysisFFT(AMSignal signal, String variant) {
        this.signal = signal;
        this.variant = variant;
        if (signal == null || signal.getSignal() == null || signal.getSignal().length < WINDOW_SIZE) {
            LOGGER.warning("Invalid or insufficient signal data for spectrum analysis.");
            JOptionPane.showMessageDialog(null, "Invalid or insufficient signal data for spectrum analysis.", "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }
        openWindows.add(new WeakReference<>(this));

        setTitle(variant + " Dynamic Spectrum Analysis");
        setSize(800, 600);
        setMinimumSize(new Dimension(600, 400));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(60, 60, 60));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Plot Panel
        JPanel plotPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawSpectrumPlot(g);
            }
        };
        plotPanel.setBackground(new Color(60, 60, 60));
        plotPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Dynamic Frequency Spectrum", 0, 0, new Font("SansSerif", Font.BOLD, 14), new Color(180, 180, 180)));
        mainPanel.add(plotPanel, BorderLayout.CENTER);

        // Legend Panel
        JPanel legendPanel = new JPanel(new GridLayout(1, 1, 0, 5));
        legendPanel.setBackground(new Color(60, 60, 60));
        legendPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Legend", 0, 0, new Font("SansSerif", Font.BOLD, 12), new Color(180, 180, 180)));
        legendPanel.setPreferredSize(new Dimension(150, 0));
        JLabel spectrumLabel = new JLabel("Spectrum");
        spectrumLabel.setForeground(Color.MAGENTA);
        spectrumLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        legendPanel.add(spectrumLabel);
        mainPanel.add(legendPanel, BorderLayout.EAST);

        // Control Panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        controlPanel.setBackground(new Color(60, 60, 60));

        JButton zoomInButton = new JButton("Zoom In");
        zoomInButton.setBackground(new Color(80, 80, 80));
        zoomInButton.setForeground(new Color(180, 180, 180));
        zoomInButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        zoomInButton.addActionListener(e -> {
            zoom *= 1.1;
            repaint();
        });
        controlPanel.add(zoomInButton);

        JButton zoomOutButton = new JButton("Zoom Out");
        zoomOutButton.setBackground(new Color(80, 80, 80));
        zoomOutButton.setForeground(new Color(180, 180, 180));
        zoomOutButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        zoomOutButton.addActionListener(e -> {
            zoom *= 0.9;
            repaint();
        });
        controlPanel.add(zoomOutButton);

        JButton pauseResumeButton = new JButton("Pause");
        pauseResumeButton.setBackground(new Color(80, 80, 80));
        pauseResumeButton.setForeground(new Color(180, 180, 180));
        pauseResumeButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        pauseResumeButton.addActionListener(e -> {
            isPaused = !isPaused;
            pauseResumeButton.setText(isPaused ? "Resume" : "Pause");
            LOGGER.info("Spectrum animation " + (isPaused ? "paused" : "resumed"));
        });
        controlPanel.add(pauseResumeButton);

        JButton resetButton = new JButton("Reset");
        resetButton.setBackground(new Color(80, 80, 80));
        resetButton.setForeground(new Color(180, 180, 180));
        resetButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        resetButton.addActionListener(e -> {
            windowIndex = 0;
            zoom = 1.0;
            pan = 0;
            isPaused = false;
            pauseResumeButton.setText("Pause");
            updateSpectrum();
            repaint();
            LOGGER.info("Spectrum animation reset");
        });
        controlPanel.add(resetButton);

        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        // Mouse interaction
        plotPanel.addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                zoom *= (e.getWheelRotation() < 0 ? 1.1 : 0.9);
                zoom = Math.max(0.1, Math.min(zoom, 10.0));
                repaint();
            }
        });
        plotPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mouseX = e.getX();
            }
        });
        plotPanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                pan += (e.getX() - mouseX) / zoom;
                mouseX = e.getX();
                repaint();
            }
        });

        add(mainPanel);

        // Initialize animation
        initAnimation();
        updateSpectrum(); // Compute initial spectrum

        // Window closing event
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (animationTimer != null) {
                    animationTimer.stop();
                }
                openWindows.removeIf(ref -> ref.get() == null || ref.get() == SpectrumAnalysisFFT.this);
                LOGGER.info("SpectrumAnalysisFFT window closed for " + variant);
            }
        });

        LOGGER.info("Dynamic SpectrumAnalysis window initialized for " + variant);
        setVisible(true);
    }

    /**
     * Initializes the animation timer.
     */
    private void initAnimation() {
        animationTimer = new Timer(1000 / FPS, e -> {
            if (!isPaused) {
                windowIndex += WINDOW_SIZE / 4; // Advance window by 1/4 for overlap
                if (windowIndex > signal.getSignal().length - WINDOW_SIZE) {
                    windowIndex = 0; // Loop back
                }
                updateSpectrum();
                repaint();
            }
        });
        animationTimer.start();
    }

    /**
     * Updates the spectrum for the current window.
     */
    private void updateSpectrum() {
        double[] modulatedSignal = signal.getSignal();
        double duration = signal.getTime()[signal.getTime().length - 1];
        int signalLength = modulatedSignal.length;
        if (windowIndex + WINDOW_SIZE > signalLength) {
            windowIndex = 0; // Reset if window exceeds signal
        }

        // Extract windowed signal with Hamming window
        double[] windowedSignal = new double[WINDOW_SIZE];
        for (int i = 0; i < WINDOW_SIZE; i++) {
            // Apply Hamming window
            double window = 0.54 - 0.46 * Math.cos(2 * Math.PI * i / (WINDOW_SIZE - 1));
            windowedSignal[i] = modulatedSignal[windowIndex + i] * window;
        }

        try {
            // Compute FFT
            double samplingRate = signalLength / duration;
            double[][] fftResult = computeFFT(windowedSignal, samplingRate);
            currentFreq = fftResult[0];
            currentSpectrum = fftResult[1];
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.SEVERE, "Failed to compute FFT", e);
            currentFreq = new double[0];
            currentSpectrum = new double[0];
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

    /**
     * Draws the dynamic frequency spectrum plot.
     */
    private void drawSpectrumPlot(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (currentSpectrum == null || currentFreq == null || currentSpectrum.length == 0) {
            g2d.setColor(new Color(180, 180, 180));
            g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2d.drawString("No spectrum data", getWidth() / 2 - 50, getHeight() / 2);
            return;
        }

        int width = getWidth();
        int height = getHeight();
        int margin = 50;

        // Compute max spectrum for scaling
        double maxSpectrum = 0;
        for (double s : currentSpectrum) {
            if (s > maxSpectrum) maxSpectrum = s;
        }
        if (maxSpectrum == 0) maxSpectrum = 1;

        // Downsample for performance
        int displayPoints = Math.min(currentFreq.length, 1000);
        int step = Math.max(1, currentFreq.length / displayPoints);
        double[] sampledFreq = new double[displayPoints];
        double[] sampledSpectrum = new double[displayPoints];
        for (int i = 0; i < displayPoints; i++) {
            int idx = i * step;
            sampledFreq[i] = currentFreq[idx];
            sampledSpectrum[i] = currentSpectrum[idx];
        }

        // Draw white grid
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(0.5f));
        int numVertical = 10;
        for (int i = 0; i <= numVertical; i++) {
            double x = margin + (i * (width - 2 * margin) / (double) numVertical + pan) * zoom;
            int xInt = (int) Math.round(x);
            if (xInt >= margin && xInt <= width - margin) {
                g2d.drawLine(xInt, margin, xInt, height - margin);
            }
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
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2d.setColor(new Color(180, 180, 180));
        double maxFreq = currentFreq[currentFreq.length - 1];
        for (int i = 0; i <= numVertical; i++) {
            double f = i * maxFreq / numVertical;
            double x = margin + (i * (width - 2 * margin) / (double) numVertical + pan) * zoom;
            int xInt = (int) Math.round(x);
            if (xInt >= margin && xInt <= width - margin) {
                g2d.drawString(String.format("%.0fHz", f), xInt - 10, height - margin + 15);
            }
        }
        for (int i = 0; i <= numHorizontal; i++) {
            double amp = maxSpectrum * (1 - i * 1.0 / numHorizontal);
            int y = margin + i * (height - 2 * margin) / numHorizontal;
            g2d.drawString(String.format("%.2f", amp), margin - 40, y + 5);
        }

        // Plot spectrum
        g2d.setColor(Color.MAGENTA);
        for (int i = 0; i < displayPoints - 1; i++) {
            double x1 = margin + ((sampledFreq[i] / maxFreq) * (width - 2 * margin) + pan) * zoom;
            double x2 = margin + ((sampledFreq[i + 1] / maxFreq) * (width - 2 * margin) + pan) * zoom;
            double y1 = height - margin - (sampledSpectrum[i] / maxSpectrum * (height / 2 - margin));
            double y2 = height - margin - (sampledSpectrum[i + 1] / maxSpectrum * (height / 2 - margin));
            int x1Int = (int) Math.round(x1);
            int x2Int = (int) Math.round(x2);
            int y1Int = (int) Math.round(y1);
            int y2Int = (int) Math.round(y2);
            if (x1Int >= margin && x1Int <= width - margin && x2Int >= margin && x2Int <= width - margin) {
                g2d.drawLine(x1Int, y1Int, x2Int, y2Int);
            }
        }

        // Peak detection
        g2d.setColor(Color.WHITE);
        for (int i = 1; i < displayPoints - 1; i++) {
            if (sampledSpectrum[i] > sampledSpectrum[i - 1] && sampledSpectrum[i] > sampledSpectrum[i + 1] && sampledSpectrum[i] / maxSpectrum > 0.1) {
                double x = margin + ((sampledFreq[i] / maxFreq) * (width - 2 * margin) + pan) * zoom;
                double y = height - margin - (sampledSpectrum[i] / maxSpectrum * (height / 2 - margin));
                int xInt = (int) Math.round(x);
                int yInt = (int) Math.round(y);
                if (xInt >= margin && xInt <= width - margin) {
                    g2d.drawString(String.format("%.0f Hz", sampledFreq[i]), xInt + 5, yInt - 5);
                }
            }
        }
    }

    /**
     * Resets zoom, pan, and animation for all open spectrum analysis windows.
     */
    public static void resetAll() {
        for (WeakReference<SpectrumAnalysisFFT> ref : openWindows) {
            SpectrumAnalysisFFT window = ref.get();
            if (window != null) {
                window.zoom = 1.0;
                window.pan = 0;
                window.windowIndex = 0;
                window.isPaused = false;
                window.updateSpectrum();
                window.repaint();
            }
        }
        openWindows.removeIf(ref -> ref.get() == null);
    }

    @Override
    public void dispose() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
        openWindows.removeIf(ref -> ref.get() == null || ref.get() == this);
        super.dispose();
    }
}