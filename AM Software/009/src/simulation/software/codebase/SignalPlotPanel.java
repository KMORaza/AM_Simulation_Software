package simulation.software.codebase;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Panel for visualizing time and frequency domain plots of AM signals.
 */
public class SignalPlotPanel extends JPanel {
    private AMSignal signal;
    private String variant;
    private double zoomTime = 1.0, zoomFreq = 1.0;
    private double panTime = 0, panFreq = 0;
    private int mouseX;

    /**
     * Constructor for SignalPlotPanel.
     *
     * @param variant AM variant (DSB-AM, DSB-SC, SSB, VSB, QAM)
     */
    public SignalPlotPanel(String variant) {
        this.variant = variant;
        setBackground(new Color(0, 0, 0));
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Plot Panels
        JPanel plotContainer = new JPanel(new GridLayout(1, 2, 10, 0));
        plotContainer.setBackground(new Color(0, 0, 0));

        JPanel timePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawTimePlot(g);
            }
        };
        timePanel.setBackground(new Color(0, 0, 0));
        timePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Time Domain", 0, 0, new Font("Dialog", Font.BOLD, 12), new Color(192, 192, 192)));
        plotContainer.add(timePanel);

        JPanel freqPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawFreqPlot(g);
            }
        };
        freqPanel.setBackground(new Color(0, 0, 0));
        freqPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Frequency Domain", 0, 0, new Font("Dialog", Font.BOLD, 12), new Color(192, 192, 192)));
        plotContainer.add(freqPanel);

        add(plotContainer, BorderLayout.CENTER);

        // Legend Panel
        JPanel legendPanel = new JPanel(new GridLayout(5, 1, 0, 5));
        legendPanel.setBackground(new Color(0, 0, 0));
        legendPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Legend", 0, 0, new Font("Dialog", Font.BOLD, 12), new Color(192, 192, 192)));
        legendPanel.setPreferredSize(new Dimension(150, 0));
        JLabel messageLabel = new JLabel("Message Signal");
        messageLabel.setForeground(Color.BLUE);
        messageLabel.setFont(new Font("Dialog", Font.BOLD, 12));
        legendPanel.add(messageLabel);
        JLabel carrierLabel = new JLabel("Carrier Signal");
        carrierLabel.setForeground(Color.GREEN);
        carrierLabel.setFont(new Font("Dialog", Font.BOLD, 12));
        legendPanel.add(carrierLabel);
        JLabel modulatedLabel = new JLabel("Modulated Signal");
        modulatedLabel.setForeground(Color.RED);
        modulatedLabel.setFont(new Font("Dialog", Font.BOLD, 12));
        legendPanel.add(modulatedLabel);
        JLabel demodulatedLabel = new JLabel("Demodulated Signal");
        demodulatedLabel.setForeground(Color.YELLOW);
        demodulatedLabel.setFont(new Font("Dialog", Font.BOLD, 12));
        legendPanel.add(demodulatedLabel);
        JLabel spectrumLabel = new JLabel("Spectrum");
        spectrumLabel.setForeground(Color.MAGENTA);
        spectrumLabel.setFont(new Font("Dialog", Font.BOLD, 12));
        legendPanel.add(spectrumLabel);
        add(legendPanel, BorderLayout.EAST);

        // Zoom/Pan Controls
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        controlPanel.setBackground(new Color(0, 0, 0));
        JButton zoomInButton = new JButton("Zoom In");
        zoomInButton.setBackground(new Color(0, 0, 0));
        zoomInButton.setForeground(new Color(192, 192, 192));
        zoomInButton.setFont(new Font("Dialog", Font.BOLD, 12));
        zoomInButton.addActionListener(e -> {
            zoomTime *= 1.1;
            zoomFreq *= 1.1;
            repaint();
        });
        controlPanel.add(zoomInButton);
        JButton zoomOutButton = new JButton("Zoom Out");
        zoomOutButton.setBackground(new Color(0, 0, 0));
        zoomOutButton.setForeground(new Color(192, 192, 192));
        zoomOutButton.setFont(new Font("Dialog", Font.BOLD, 12));
        zoomOutButton.addActionListener(e -> {
            zoomTime *= 0.9;
            zoomFreq *= 0.9;
            repaint();
        });
        controlPanel.add(zoomOutButton);
        add(controlPanel, BorderLayout.SOUTH);

        // Mouse interaction for time panel
        timePanel.addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                zoomTime *= (e.getWheelRotation() < 0 ? 1.1 : 0.9);
                zoomTime = Math.max(0.1, Math.min(zoomTime, 10.0));
                repaint();
            }
        });
        timePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mouseX = e.getX();
            }
        });
        timePanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                panTime += (e.getX() - mouseX) / zoomTime;
                mouseX = e.getX();
                repaint();
            }
        });

        // Mouse interaction for frequency panel
        freqPanel.addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                zoomFreq *= (e.getWheelRotation() < 0 ? 1.1 : 0.9);
                zoomFreq = Math.max(0.1, Math.min(zoomFreq, 10.0));
                repaint();
            }
        });
        freqPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mouseX = e.getX();
            }
        });
        freqPanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                panFreq += (e.getX() - mouseX) / zoomFreq;
                mouseX = e.getX();
                repaint();
            }
        });
    }

    /**
     * Updates the signal to be plotted.
     */
    public void updateSignal(AMSignal signal) {
        this.signal = signal;
        repaint();
    }

    /**
     * Gets the current signal.
     */
    public AMSignal getCurrentSignal() {
        return signal;
    }

    /**
     * Resets zoom and pan to default values.
     */
    public void resetView() {
        zoomTime = 1.0;
        zoomFreq = 1.0;
        panTime = 0;
        panFreq = 0;
        repaint();
    }

    /**
     * Draws the time-domain plot.
     */
    private void drawTimePlot(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (signal == null) {
            g2d.setColor(new Color(192, 192, 192));
            g2d.setFont(new Font("Dialog", Font.BOLD, 12));
            g2d.drawString("No signal data", getWidth() / 2 - 50, getHeight() / 2);
            return;
        }

        double[] time = signal.getTime();
        double[] modulatedSignal = signal.getSignal();
        double[] message = signal.getMessage();
        double[] carrier = signal.getCarrier();
        double[] demodulatedSignal = signal.getDemodulatedSignal();

        int width = getWidth();
        int height = getHeight();
        int margin = 50;

        // Compute max amplitude for dynamic scaling
        double maxAmplitude = 1.0;
        for (double v : message) maxAmplitude = Math.max(maxAmplitude, Math.abs(v));
        for (double v : carrier) maxAmplitude = Math.max(maxAmplitude, Math.abs(v));
        for (double v : modulatedSignal) maxAmplitude = Math.max(maxAmplitude, Math.abs(v));
        for (double v : demodulatedSignal) maxAmplitude = Math.max(maxAmplitude, Math.abs(v));
        if (maxAmplitude == 0) maxAmplitude = 1;

        // Downsample for performance
        int displayPoints = Math.min(time.length, 1000);
        int step = Math.max(1, time.length / displayPoints);
        double[] sampledTime = new double[displayPoints];
        double[] sampledMessage = new double[displayPoints];
        double[] sampledCarrier = new double[displayPoints];
        double[] sampledModulated = new double[displayPoints];
        double[] sampledDemodulated = new double[displayPoints];
        for (int i = 0; i < displayPoints; i++) {
            int idx = i * step;
            sampledTime[i] = time[idx];
            sampledMessage[i] = message[idx];
            sampledCarrier[i] = carrier[idx];
            sampledModulated[i] = modulatedSignal[idx];
            sampledDemodulated[i] = demodulatedSignal[idx];
        }

        // Draw white grid
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(0.5f));
        int numVertical = 10;
        for (int i = 0; i <= numVertical; i++) {
            int x = margin + (int) ((i * (width - 2 * margin) / numVertical + panTime) * zoomTime);
            if (x >= margin && x <= width - margin) {
                g2d.drawLine(x, margin, x, height - margin);
            }
        }
        int numHorizontal = 8;
        for (int i = 0; i <= numHorizontal; i++) {
            int y = margin + i * (height - 2 * margin) / numHorizontal;
            g2d.drawLine(margin, y, width - margin, y);
        }

        // Draw axes
        g2d.setColor(new Color(192, 192, 192));
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawLine(margin, height / 2, width - margin, height / 2); // X-axis
        g2d.drawLine(margin, margin, margin, height - margin); // Y-axis

        // Draw axis labels
        g2d.setFont(new Font("Dialog", Font.PLAIN, 10));
        g2d.setColor(new Color(192, 192, 192));
        double maxTime = time[time.length - 1];
        for (int i = 0; i <= numVertical; i++) {
            double t = i * maxTime / numVertical;
            int x = margin + (int) ((i * (width - 2 * margin) / numVertical + panTime) * zoomTime);
            if (x >= margin && x <= width - margin) {
                g2d.drawString(String.format("%.3fs", t), x - 10, height - margin + 15);
            }
        }
        for (int i = 0; i <= numHorizontal; i++) {
            double amp = maxAmplitude * (1 - 2.0 * i / numHorizontal);
            int y = margin + i * (height - 2 * margin) / numHorizontal;
            g2d.drawString(String.format("%.2f", amp), margin - 40, y + 5);
        }

        // Plot message signal
        g2d.setColor(Color.BLUE);
        for (int i = 0; i < displayPoints - 1; i++) {
            int x1 = margin + (int) (((sampledTime[i] / maxTime) * (width - 2 * margin) + panTime) * zoomTime);
            int x2 = margin + (int) (((sampledTime[i + 1] / maxTime) * (width - 2 * margin) + panTime) * zoomTime);
            int y1 = height / 2 - (int) (sampledMessage[i] / maxAmplitude * (height / 4));
            int y2 = height / 2 - (int) (sampledMessage[i + 1] / maxAmplitude * (height / 4));
            if (x1 >= margin && x1 <= width - margin && x2 >= margin && x2 <= width - margin) {
                g2d.drawLine(x1, y1, x2, y2);
            }
        }

        // Plot carrier signal
        g2d.setColor(Color.GREEN);
        for (int i = 0; i < displayPoints - 1; i++) {
            int x1 = margin + (int) (((sampledTime[i] / maxTime) * (width - 2 * margin) + panTime) * zoomTime);
            int x2 = margin + (int) (((sampledTime[i + 1] / maxTime) * (width - 2 * margin) + panTime) * zoomTime);
            int y1 = height / 2 - (int) (sampledCarrier[i] / maxAmplitude * (height / 4));
            int y2 = height / 2 - (int) (sampledCarrier[i + 1] / maxAmplitude * (height / 4));
            if (x1 >= margin && x1 <= width - margin && x2 >= margin && x2 <= width - margin) {
                g2d.drawLine(x1, y1, x2, y2);
            }
        }

        // Plot modulated signal
        g2d.setColor(Color.RED);
        for (int i = 0; i < displayPoints - 1; i++) {
            int x1 = margin + (int) (((sampledTime[i] / maxTime) * (width - 2 * margin) + panTime) * zoomTime);
            int x2 = margin + (int) (((sampledTime[i + 1] / maxTime) * (width - 2 * margin) + panTime) * zoomTime);
            int y1 = height / 2 - (int) (sampledModulated[i] / maxAmplitude * (height / 4));
            int y2 = height / 2 - (int) (sampledModulated[i + 1] / maxAmplitude * (height / 4));
            if (x1 >= margin && x1 <= width - margin && x2 >= margin && x2 <= width - margin) {
                g2d.drawLine(x1, y1, x2, y2);
            }
        }

        // Plot demodulated signal
        if (demodulatedSignal != null && demodulatedSignal.length > 0 && demodulatedSignal[0] != 0) {
            g2d.setColor(Color.YELLOW);
            for (int i = 0; i < displayPoints - 1; i++) {
                int x1 = margin + (int) (((sampledTime[i] / maxTime) * (width - 2 * margin) + panTime) * zoomTime);
                int x2 = margin + (int) (((sampledTime[i + 1] / maxTime) * (width - 2 * margin) + panTime) * zoomTime);
                int y1 = height / 2 - (int) (sampledDemodulated[i] / maxAmplitude * (height / 4));
                int y2 = height / 2 - (int) (sampledDemodulated[i + 1] / maxAmplitude * (height / 4));
                if (x1 >= margin && x1 <= width - margin && x2 >= margin && x2 <= width - margin) {
                    g2d.drawLine(x1, y1, x2, y2);
                }
            }
        }
    }

    /**
     * Draws the frequency-domain plot.
     */
    private void drawFreqPlot(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (signal == null) {
            g2d.setColor(new Color(192, 192, 192));
            g2d.setFont(new Font("Dialog", Font.BOLD, 12));
            g2d.drawString("No signal data", getWidth() / 2 - 50, getHeight() / 2);
            return;
        }

        double[] frequency = signal.getFrequency();
        double[] spectrum = signal.getSpectrum();

        int width = getWidth();
        int height = getHeight();
        int margin = 50;

        // Compute max spectrum for scaling
        double maxSpectrum = 0;
        for (double s : spectrum) {
            if (s > maxSpectrum) maxSpectrum = s;
        }
        if (maxSpectrum == 0) maxSpectrum = 1;

        // Downsample for performance
        int displayPoints = Math.min(frequency.length, 1000);
        int step = Math.max(1, frequency.length / displayPoints);
        double[] sampledFreq = new double[displayPoints];
        double[] sampledSpectrum = new double[displayPoints];
        for (int i = 0; i < displayPoints; i++) {
            int idx = i * step;
            sampledFreq[i] = frequency[idx];
            sampledSpectrum[i] = spectrum[idx];
        }

        // Draw white grid
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(0.5f));
        int numVertical = 10;
        for (int i = 0; i <= numVertical; i++) {
            int x = margin + (int) ((i * (width - 2 * margin) / numVertical + panFreq) * zoomFreq);
            if (x >= margin && x <= width - margin) {
                g2d.drawLine(x, margin, x, height - margin);
            }
        }
        int numHorizontal = 8;
        for (int i = 0; i <= numHorizontal; i++) {
            int y = margin + i * (height - 2 * margin) / numHorizontal;
            g2d.drawLine(margin, y, width - margin, y);
        }

        // Draw axes
        g2d.setColor(new Color(192, 192, 192));
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawLine(margin, height / 2, width - margin, height / 2); // X-axis
        g2d.drawLine(margin, margin, margin, height - margin); // Y-axis

        // Draw axis labels
        g2d.setFont(new Font("Dialog", Font.PLAIN, 10));
        g2d.setColor(new Color(192, 192, 192));
        double maxFreq = frequency[frequency.length - 1];
        for (int i = 0; i <= numVertical; i++) {
            double f = i * maxFreq / numVertical;
            int x = margin + (int) ((i * (width - 2 * margin) / numVertical + panFreq) * zoomFreq);
            if (x >= margin && x <= width - margin) {
                g2d.drawString(String.format("%.0fHz", f), x - 10, height - margin + 15);
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
            int x1 = margin + (int) (((sampledFreq[i] / maxFreq) * (width - 2 * margin) + panFreq) * zoomFreq);
            int x2 = margin + (int) (((sampledFreq[i + 1] / maxFreq) * (width - 2 * margin) + panFreq) * zoomFreq);
            int y1 = height - margin - (int) (sampledSpectrum[i] / maxSpectrum * (height / 2 - margin));
            int y2 = height - margin - (int) (sampledSpectrum[i + 1] / maxSpectrum * (height / 2 - margin));
            if (x1 >= margin && x1 <= width - margin && x2 >= margin && x2 <= width - margin) {
                g2d.drawLine(x1, y1, x2, y2);
            }
        }

        // Peak detection
        g2d.setColor(Color.WHITE);
        for (int i = 1; i < displayPoints - 1; i++) {
            if (sampledSpectrum[i] > sampledSpectrum[i - 1] && sampledSpectrum[i] > sampledSpectrum[i + 1] && sampledSpectrum[i] / maxSpectrum > 0.1) {
                int x = margin + (int) (((sampledFreq[i] / maxFreq) * (width - 2 * margin) + panFreq) * zoomFreq);
                int y = height - margin - (int) (sampledSpectrum[i] / maxSpectrum * (height / 2 - margin));
                if (x >= margin && x <= width - margin) {
                    g2d.drawString(String.format("%.0f Hz", sampledFreq[i]), x + 5, y - 5);
                }
            }
        }
    }
}