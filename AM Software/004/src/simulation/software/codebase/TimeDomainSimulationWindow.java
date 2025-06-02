package simulation.software.codebase;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Window for real-time time-domain simulation of AM signals.
 */
public class TimeDomainSimulationWindow extends JFrame {
    private AMSignal signal;
    private String variant;
    private double zoom = 1.0;
    private double pan = 0;
    private int mouseX;
    private int windowSize;
    private int currentOffset = 0;
    private Timer animationTimer;
    private static List<TimeDomainSimulationWindow> openWindows = new ArrayList<>();

    /**
     * Constructor for TimeDomainSimulationWindow.
     *
     * @param signal  The AM signal to simulate
     * @param variant The AM variant (DSB-AM, DSB-SC, SSB, VSB, QAM)
     */
    public TimeDomainSimulationWindow(AMSignal signal, String variant) {
        this.signal = signal;
        this.variant = variant;
        this.windowSize = signal.getTime().length / 4; // Show 1/4 of the signal
        openWindows.add(this);

        setTitle(variant + " Time-Domain Simulation");
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
                drawTimePlot(g);
            }
        };
        plotPanel.setBackground(new Color(60, 60, 60));
        plotPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Time-Domain Simulation", 0, 0, new Font("SansSerif", Font.BOLD, 14), new Color(180, 180, 180)));
        mainPanel.add(plotPanel, BorderLayout.CENTER);

        // Zoom Controls
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

        // Animation timer (60 FPS)
        animationTimer = new Timer(1000 / 60, e -> {
            currentOffset = (currentOffset + 10) % (signal.getTime().length - windowSize);
            repaint();
        });
        animationTimer.start();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                animationTimer.stop();
                openWindows.remove(TimeDomainSimulationWindow.this);
            }
        });

        setVisible(true);
    }

    /**
     * Draws the animated time-domain plot.
     */
    private void drawTimePlot(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (signal == null) {
            g2d.setColor(new Color(180, 180, 180));
            g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2d.drawString("No signal data", getWidth() / 2 - 50, getHeight() / 2);
            return;
        }

        double[] time = signal.getTime();
        double[] message = signal.getMessage();
        double[] carrier = signal.getCarrier();
        double[] modulated = signal.getSignal();
        double[] demodulated = signal.getDemodulatedSignal();

        int width = getWidth();
        int height = getHeight();
        int margin = 50;

        // Compute max amplitude for dynamic scaling
        double maxAmplitude = 1.0;
        for (int i = currentOffset; i < currentOffset + windowSize; i++) {
            if (i < time.length) {
                maxAmplitude = Math.max(maxAmplitude, Math.abs(message[i]));
                maxAmplitude = Math.max(maxAmplitude, Math.abs(carrier[i]));
                maxAmplitude = Math.max(maxAmplitude, Math.abs(modulated[i]));
                if (demodulated[i] != 0) {
                    maxAmplitude = Math.max(maxAmplitude, Math.abs(demodulated[i]));
                }
            }
        }

        // Downsample for performance
        int displayPoints = Math.min(windowSize, 1000);
        int step = Math.max(1, windowSize / displayPoints);
        double[] sampledTime = new double[displayPoints];
        double[] sampledMessage = new double[displayPoints];
        double[] sampledCarrier = new double[displayPoints];
        double[] sampledModulated = new double[displayPoints];
        double[] sampledDemodulated = new double[displayPoints];
        for (int i = 0; i < displayPoints; i++) {
            int idx = currentOffset + i * step;
            if (idx < time.length) {
                sampledTime[i] = time[idx];
                sampledMessage[i] = message[idx];
                sampledCarrier[i] = carrier[idx];
                sampledModulated[i] = modulated[idx];
                sampledDemodulated[i] = demodulated[idx];
            }
        }

        // Draw white grid
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(0.5f));
        int numVertical = 10;
        for (int i = 0; i <= numVertical; i++) {
            int x = margin + (int) ((i * (width - 2 * margin) / numVertical + pan) * zoom);
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
        g2d.setColor(new Color(180, 180, 180));
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawLine(margin, height / 2, width - margin, height / 2); // X-axis
        g2d.drawLine(margin, margin, margin, height - margin); // Y-axis

        // Draw axis labels
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2d.setColor(new Color(180, 180, 180));
        double maxTime = time[Math.min(currentOffset + windowSize - 1, time.length - 1)] - time[currentOffset];
        for (int i = 0; i <= numVertical; i++) {
            double t = time[currentOffset] + i * maxTime / numVertical;
            int x = margin + (int) ((i * (width - 2 * margin) / numVertical + pan) * zoom);
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
            int x1 = margin + (int) (((i / (double)(displayPoints - 1)) * (width - 2 * margin) + pan) * zoom);
            int x2 = margin + (int) (((i + 1) / (double)(displayPoints - 1)) * (width - 2 * margin) + pan) * zoom;
            int y1 = height / 2 - (int) (sampledMessage[i] / maxAmplitude * (height / 4));
            int y2 = height / 2 - (int) (sampledMessage[i + 1] / maxAmplitude * (height / 4));
            if (x1 >= margin && x1 <= width - margin && x2 >= margin && x2 <= width - margin) {
                g2d.drawLine(x1, y1, x2, y2);
            }
        }

        // Plot carrier signal
        g2d.setColor(Color.GREEN);
        for (int i = 0; i < displayPoints - 1; i++) {
            int x1 = margin + (int) (((i / (double)(displayPoints - 1)) * (width - 2 * margin) + pan) * zoom);
            int x2 = margin + (int) (((i + 1) / (double)(displayPoints - 1)) * (width - 2 * margin) + pan) * zoom;
            int y1 = height / 2 - (int) (sampledCarrier[i] / maxAmplitude * (height / 4));
            int y2 = height / 2 - (int) (sampledCarrier[i + 1] / maxAmplitude * (height / 4));
            if (x1 >= margin && x1 <= width - margin && x2 >= margin && x2 <= width - margin) {
                g2d.drawLine(x1, y1, x2, y2);
            }
        }

        // Plot modulated signal
        g2d.setColor(Color.RED);
        for (int i = 0; i < displayPoints - 1; i++) {
            int x1 = margin + (int) (((i / (double)(displayPoints - 1)) * (width - 2 * margin) + pan) * zoom);
            int x2 = margin + (int) (((i + 1) / (double)(displayPoints - 1)) * (width - 2 * margin) + pan) * zoom;
            int y1 = height / 2 - (int) (sampledModulated[i] / maxAmplitude * (height / 4));
            int y2 = height / 2 - (int) (sampledModulated[i + 1] / maxAmplitude * (height / 4));
            if (x1 >= margin && x1 <= width - margin && x2 >= margin && x2 <= width - margin) {
                g2d.drawLine(x1, y1, x2, y2);
            }
        }

        // Plot demodulated signal
        if (demodulated != null && demodulated.length > 0 && demodulated[0] != 0) {
            g2d.setColor(Color.YELLOW);
            for (int i = 0; i < displayPoints - 1; i++) {
                int x1 = margin + (int) (((i / (double)(displayPoints - 1)) * (width - 2 * margin) + pan) * zoom);
                int x2 = margin + (int) (((i + 1) / (double)(displayPoints - 1)) * (width - 2 * margin) + pan) * zoom;
                int y1 = height / 2 - (int) (sampledDemodulated[i] / maxAmplitude * (height / 4));
                int y2 = height / 2 - (int) (sampledDemodulated[i + 1] / maxAmplitude * (height / 4));
                if (x1 >= margin && x1 <= width - margin && x2 >= margin && x2 <= width - margin) {
                    g2d.drawLine(x1, y1, x2, y2);
                }
            }
        }
    }

    /**
     * Resets zoom and pan for all open simulation windows.
     */
    public static void resetAll() {
        for (TimeDomainSimulationWindow window : openWindows) {
            window.zoom = 1.0;
            window.pan = 0;
            window.repaint();
        }
    }
}