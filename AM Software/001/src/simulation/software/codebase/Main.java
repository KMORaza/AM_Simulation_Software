package simulation.software.codebase;

import javax.swing.*;
import java.awt.*;

public class Main extends JFrame {
    public Main() {
        setTitle("Amplitude Modulation");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        // Set Windows Classic Look and Feel
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Set black background
        getContentPane().setBackground(new Color(0, 0, 0)); // Pure black for 90s aesthetic

        // Create tabbed pane for different AM variants
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(new Color(0, 0, 0));
        tabbedPane.setForeground(new Color(192, 192, 192)); // Light gray text
        tabbedPane.setFont(new Font("Dialog", Font.BOLD, 12)); // Bold 90s-style font

        String[] variants = {"DSB-AM", "DSB-SC", "SSB", "VSB", "QAM"};
        
        for (String variant : variants) {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBackground(new Color(0, 0, 0));
            ControlPanel controlPanel = new ControlPanel(variant);
            SignalPlotPanel plotPanel = new SignalPlotPanel(variant);
            controlPanel.setPlotPanel(plotPanel);
            panel.add(controlPanel, BorderLayout.WEST);
            panel.add(plotPanel, BorderLayout.CENTER);
            tabbedPane.addTab(variant, panel);
        }

        add(tabbedPane);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Main frame = new Main();
            frame.setVisible(true);
        });
    }
}