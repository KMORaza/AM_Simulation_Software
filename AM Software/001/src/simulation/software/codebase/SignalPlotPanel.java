package simulation.software.codebase;

import javax.swing.*;
import java.awt.*;

public class SignalPlotPanel extends JPanel {
    private AMSignal signal;
    private String variant;

    public SignalPlotPanel(String variant) {
        this.variant = variant;
        setPreferredSize(new Dimension(900, 600));
        setBackground(new Color(0, 0, 0)); // Pure black background
    }

    public void updateSignal(AMSignal signal) {
        this.signal = signal;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (signal == null) {
            g2d.setColor(new Color(192, 192, 192)); // Light gray text
            g2d.setFont(new Font("Dialog", Font.BOLD, 12));
            g2d.drawString("No signal data", getWidth() / 2 - 50, getHeight() / 2);
            return;
        }

        double[] time = signal.getTime();
        double[] modulatedSignal = signal.getSignal();
        double[] message = signal.getMessage();
        double[] carrier = signal.getCarrier();
        double[] demodulatedSignal = signal.getDemodulatedSignal();
        double[] frequency = signal.getFrequency();
        double[] spectrum = signal.getSpectrum();

        int width = getWidth();
        int height = getHeight();
        int margin = 50;

        // Time-domain plot (top half)
        int timePlotHeight = height / 2;
        g2d.setColor(new Color(192, 192, 192)); // Light gray axes
        g2d.drawLine(margin, timePlotHeight / 2, width - margin, timePlotHeight / 2); // X-axis
        g2d.drawLine(margin, margin, margin, timePlotHeight - margin); // Y-axis

        // Plot message signal
        g2d.setColor(Color.BLUE);
        for (int i = 0; i < time.length - 1; i++) {
            int x1 = margin + (int) ((time[i] / time[time.length - 1]) * (width - 2 * margin));
            int x2 = margin + (int) ((time[i + 1] / time[time.length - 1]) * (width - 2 * margin));
            int y1 = timePlotHeight / 2 - (int) (message[i] * (timePlotHeight / 4));
            int y2 = timePlotHeight / 2 - (int) (message[i + 1] * (timePlotHeight / 4));
            g2d.drawLine(x1, y1, x2, y2);
        }

        // Plot carrier signal
        g2d.setColor(Color.GREEN);
        for (int i = 0; i < time.length - 1; i++) {
            int x1 = margin + (int) ((time[i] / time[time.length - 1]) * (width - 2 * margin));
            int x2 = margin + (int) ((time[i + 1] / time[time.length - 1]) * (width - 2 * margin));
            int y1 = timePlotHeight / 2 - (int) (carrier[i] * (timePlotHeight / 4));
            int y2 = timePlotHeight / 2 - (int) (carrier[i + 1] * (timePlotHeight / 4));
            g2d.drawLine(x1, y1, x2, y2);
        }

        // Plot modulated signal
        g2d.setColor(Color.RED);
        for (int i = 0; i < time.length - 1; i++) {
            int x1 = margin + (int) ((time[i] / time[time.length - 1]) * (width - 2 * margin));
            int x2 = margin + (int) ((time[i + 1] / time[time.length - 1]) * (width - 2 * margin));
            int y1 = timePlotHeight / 2 - (int) (modulatedSignal[i] * (timePlotHeight / 4));
            int y2 = timePlotHeight / 2 - (int) (modulatedSignal[i + 1] * (timePlotHeight / 4));
            g2d.drawLine(x1, y1, x2, y2);
        }

        // Plot demodulated signal (if available)
        if (demodulatedSignal != null && demodulatedSignal.length > 0 && demodulatedSignal[0] != 0) {
            g2d.setColor(Color.YELLOW);
            for (int i = 0; i < time.length - 1; i++) {
                int x1 = margin + (int) ((time[i] / time[time.length - 1]) * (width - 2 * margin));
                int x2 = margin + (int) ((time[i + 1] / time[time.length - 1]) * (width - 2 * margin));
                int y1 = timePlotHeight / 2 - (int) (demodulatedSignal[i] * (timePlotHeight / 4));
                int y2 = timePlotHeight / 2 - (int) (demodulatedSignal[i + 1] * (timePlotHeight / 4));
                g2d.drawLine(x1, y1, x2, y2);
            }
        }

        // Frequency-domain plot (bottom half)
        int freqPlotHeight = height / 2;
        g2d.setColor(new Color(192, 192, 192)); // Light gray axes
        g2d.drawLine(margin, height - freqPlotHeight / 2, width - margin, height - freqPlotHeight / 2); // X-axis
        g2d.drawLine(margin, height - freqPlotHeight + margin, margin, height - margin); // Y-axis

        // Plot spectrum
        g2d.setColor(Color.MAGENTA);
        double maxSpectrum = 0;
        for (double s : spectrum) {
            if (s > maxSpectrum) maxSpectrum = s;
        }
        if (maxSpectrum == 0) maxSpectrum = 1; // Avoid division by zero
        for (int i = 0; i < frequency.length - 1; i++) {
            int x1 = margin + (int) ((frequency[i] / frequency[frequency.length - 1]) * (width - 2 * margin));
            int x2 = margin + (int) ((frequency[i + 1] / frequency[frequency.length - 1]) * (width - 2 * margin));
            int y1 = height - margin - (int) (spectrum[i] / maxSpectrum * (freqPlotHeight / 2 - margin));
            int y2 = height - margin - (int) (spectrum[i + 1] / maxSpectrum * (freqPlotHeight / 2 - margin));
            g2d.drawLine(x1, y1, x2, y2);
        }

        // Draw legend
        g2d.setFont(new Font("Dialog", Font.BOLD, 12));
        g2d.setColor(Color.BLUE);
        g2d.drawString("Message Signal", width - 150, 30);
        g2d.setColor(Color.GREEN);
        g2d.drawString("Carrier Signal", width - 150, 50);
        g2d.setColor(Color.RED);
        g2d.drawString("Modulated Signal", width - 150, 70);
        if (demodulatedSignal != null && demodulatedSignal.length > 0 && demodulatedSignal[0] != 0) {
            g2d.setColor(Color.YELLOW);
            g2d.drawString("Demodulated Signal", width - 150, 90);
        }
        g2d.setColor(Color.MAGENTA);
        g2d.drawString("Spectrum", width - 150, 110);
    }
}