package simulation.software.codebase;

import javax.swing.*;
import java.awt.*;

public class Main extends JFrame {
    public Main() {
        setTitle("Amplitude Modulation");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1500, 900);
        setLocationRelativeTo(null);

        // Set Windows Classic Look and Feel
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Set black background
        getContentPane().setBackground(new Color(0, 0, 0));
        setLayout(new BorderLayout(10, 10));

        // Toolbar
        JToolBar toolbar = new JToolBar();
        toolbar.setBackground(new Color(0, 0, 0));
        toolbar.setFloatable(false);
        JButton resetButton = new JButton("Reset Zoom/Pan");
        resetButton.setBackground(new Color(0, 0, 0));
        resetButton.setForeground(new Color(192, 192, 192));
        resetButton.setFont(new Font("Dialog", Font.BOLD, 12));
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
        });
        toolbar.add(resetButton);
        add(toolbar, BorderLayout.NORTH);

        // Tabbed pane for AM variants
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(new Color(0, 0, 0));
        tabbedPane.setForeground(new Color(192, 192, 192));
        tabbedPane.setFont(new Font("Dialog", Font.BOLD, 12));

        String[] variants = {"DSB-AM", "DSB-SC", "SSB", "VSB", "QAM"};
        
        for (String variant : variants) {
            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBackground(new Color(0, 0, 0));
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Main frame = new Main();
            frame.setVisible(true);
        });
    }
}