package simulation.software.codebase;

import javax.swing.*;
import java.awt.*;

/**
 * Main application window for the Amplitude Modulation simulation.
 */
public class Main extends JFrame {
    public Main() {
        setTitle("Amplitude Modulation Simulator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1600, 900);
        setMinimumSize(new Dimension(1200, 600)); // Prevent layout issues
        setLocationRelativeTo(null);

        // Set Windows Classic look and feel
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
            // Enforce classic Windows style
            UIManager.put("WindowsLookAndFeel.updateStyle", "classic");
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to system look and feel
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        setLayout(new BorderLayout(10, 10));

        // Toolbar
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("AM Simulator");
        titleLabel.setFont(new Font("Bahnschrift", Font.BOLD, 16));
        toolbar.add(titleLabel);
        toolbar.addSeparator(new Dimension(20, 0));

        JButton resetButton = new JButton("Reset Zoom/Pan");
        resetButton.setFont(new Font("Bahnschrift", Font.BOLD, 12));
        resetButton.addActionListener(e -> {
            for (Component c : getContentPane().getComponents()) {
                if (c instanceof JTabbedPane) {
                    JTabbedPane tabs = (JTabbedPane) c;
                    for (int i = 0; i < tabs.getTabCount(); i++) {
                        JPanel panel = (JPanel) tabs.getComponentAt(i);
                        for (Component comp : panel.getComponents()) {
                            if (comp instanceof SignalPlotPanel) {
                                ((SignalPlotPanel) comp).resetView();
                            }
                        }
                    }
                }
            }
            // Reset any open simulation windows
            TimeDomainSimulationWindow.resetAll();
        });
        toolbar.add(resetButton);
        add(toolbar, BorderLayout.NORTH);

        // Tabbed pane for AM variants
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Dialog", Font.BOLD, 14));
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] variants = {"DSB-AM", "DSB-SC", "SSB", "VSB", "QAM"};
        
        for (String variant : variants) {
            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            ControlPanel controlPanel = new ControlPanel(variant);
            SignalPlotPanel plotPanel = new SignalPlotPanel(variant);
            controlPanel.setPlotPanel(plotPanel);
            panel.add(controlPanel, BorderLayout.WEST);
            panel.add(plotPanel, BorderLayout.CENTER);
            tabbedPane.addTab(variant, panel);
        }

        add(tabbedPane, BorderLayout.CENTER);
    }

    /**
     * Main method to launch the application.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Main frame = new Main();
            frame.setVisible(true);
        });
    }
}