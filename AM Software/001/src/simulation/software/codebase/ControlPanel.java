package simulation.software.codebase;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ControlPanel extends JPanel {
    private JTextField carrierFreqField, modulationIndexField, multiToneFreqField, noiseAmplitudeField;
    private JComboBox<String> waveformCombo, noiseTypeCombo, demodulationCombo;
    private JTextField phaseShiftField;
    private SignalPlotPanel plotPanel;
    private String variant;

    public ControlPanel(String variant) {
        this.variant = variant;
        setLayout(new GridLayout(9, 2, 10, 10));
        setBorder(BorderFactory.createEtchedBorder()); // 90s-style etched border
        setBackground(new Color(0, 0, 0)); // Pure black background
        setPreferredSize(new Dimension(300, 350));

        // Font for 90s aesthetic
        Font labelFont = new Font("Dialog", Font.BOLD, 12);
        Font fieldFont = new Font("Dialog", Font.PLAIN, 12);

        // Carrier Frequency
        JLabel carrierLabel = new JLabel("Carrier Frequency (Hz):");
        carrierLabel.setForeground(new Color(192, 192, 192));
        carrierLabel.setFont(labelFont);
        add(carrierLabel);
        carrierFreqField = new JTextField("1000", 10);
        carrierFreqField.setBackground(new Color(0, 0, 0));
        carrierFreqField.setForeground(new Color(192, 192, 192));
        carrierFreqField.setFont(fieldFont);
        add(carrierFreqField);

        // Waveform Type
        JLabel waveformLabel = new JLabel("Waveform Type:");
        waveformLabel.setForeground(new Color(192, 192, 192));
        waveformLabel.setFont(labelFont);
        add(waveformLabel);
        waveformCombo = new JComboBox<>(new String[]{"Sine", "Square", "Triangle"});
        waveformCombo.setBackground(new Color(0, 0, 0));
        waveformCombo.setForeground(new Color(192, 192, 192));
        waveformCombo.setFont(fieldFont);
        add(waveformCombo);

        // Multi-tone Frequencies
        JLabel freqLabel = new JLabel("Message Frequencies (Hz, comma-separated):");
        freqLabel.setForeground(new Color(192, 192, 192));
        freqLabel.setFont(labelFont);
        add(freqLabel);
        multiToneFreqField = new JTextField("100", 10);
        multiToneFreqField.setBackground(new Color(0, 0, 0));
        multiToneFreqField.setForeground(new Color(192, 192, 192));
        multiToneFreqField.setFont(fieldFont);
        add(multiToneFreqField);

        // Modulation Index
        JLabel modIndexLabel = new JLabel("Modulation Index:");
        modIndexLabel.setForeground(new Color(192, 192, 192));
        modIndexLabel.setFont(labelFont);
        add(modIndexLabel);
        modulationIndexField = new JTextField("0.5", 10);
        modulationIndexField.setBackground(new Color(0, 0, 0));
        modulationIndexField.setForeground(new Color(192, 192, 192));
        modulationIndexField.setFont(fieldFont);
        add(modulationIndexField);

        // Noise Type
        JLabel noiseTypeLabel = new JLabel("Noise Type:");
        noiseTypeLabel.setForeground(new Color(192, 192, 192));
        noiseTypeLabel.setFont(labelFont);
        add(noiseTypeLabel);
        noiseTypeCombo = new JComboBox<>(new String[]{"None", "White", "Pink", "Gaussian"});
        noiseTypeCombo.setBackground(new Color(0, 0, 0));
        noiseTypeCombo.setForeground(new Color(192, 192, 192));
        noiseTypeCombo.setFont(fieldFont);
        add(noiseTypeCombo);

        // Noise Amplitude
        JLabel noiseAmpLabel = new JLabel("Noise Amplitude:");
        noiseAmpLabel.setForeground(new Color(192, 192, 192));
        noiseAmpLabel.setFont(labelFont);
        add(noiseAmpLabel);
        noiseAmplitudeField = new JTextField("0.1", 10);
        noiseAmplitudeField.setBackground(new Color(0, 0, 0));
        noiseAmplitudeField.setForeground(new Color(192, 192, 192));
        noiseAmplitudeField.setFont(fieldFont);
        add(noiseAmplitudeField);

        // Demodulation Type
        JLabel demodLabel = new JLabel("Demodulation Type:");
        demodLabel.setForeground(new Color(192, 192, 192));
        demodLabel.setFont(labelFont);
        add(demodLabel);
        demodulationCombo = new JComboBox<>(new String[]{"None", "Coherent", "Non-Coherent"});
        demodulationCombo.setBackground(new Color(0, 0, 0));
        demodulationCombo.setForeground(new Color(192, 192, 192));
        demodulationCombo.setFont(fieldFont);
        add(demodulationCombo);

        // Phase Shift (for QAM only)
        if (variant.equals("QAM")) {
            JLabel phaseLabel = new JLabel("Phase Shift (degrees):");
            phaseLabel.setForeground(new Color(192, 192, 192));
            phaseLabel.setFont(labelFont);
            add(phaseLabel);
            phaseShiftField = new JTextField("90", 10);
            phaseShiftField.setBackground(new Color(0, 0, 0));
            phaseShiftField.setForeground(new Color(192, 192, 192));
            phaseShiftField.setFont(fieldFont);
            add(phaseShiftField);
        } else {
            add(new JLabel(""));
            add(new JLabel(""));
        }

        // Update Button
        JButton updateButton = new JButton("Update Signal");
        updateButton.setBackground(new Color(0, 0, 0));
        updateButton.setForeground(new Color(192, 192, 192));
        updateButton.setFont(labelFont);
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateSignal();
            }
        });
        add(updateButton);
    }

    public void setPlotPanel(SignalPlotPanel plotPanel) {
        this.plotPanel = plotPanel;
    }

    private void updateSignal() {
        try {
            double carrierFreq = Double.parseDouble(carrierFreqField.getText());
            String[] freqStrings = multiToneFreqField.getText().split(",");
            double[] messageFreqs = new double[freqStrings.length];
            for (int i = 0; i < freqStrings.length; i++) {
                messageFreqs[i] = Double.parseDouble(freqStrings[i].trim());
            }
            double modulationIndex = Double.parseDouble(modulationIndexField.getText());
            double noiseAmplitude = Double.parseDouble(noiseAmplitudeField.getText());
            double phaseShift = phaseShiftField != null ? Double.parseDouble(phaseShiftField.getText()) : 0;
            String waveformType = (String) waveformCombo.getSelectedItem();
            String noiseType = (String) noiseTypeCombo.getSelectedItem();
            String demodulationType = (String) demodulationCombo.getSelectedItem();

            if (carrierFreq <= 0 || messageFreqs.length == 0 || modulationIndex < 0 || noiseAmplitude < 0) {
                JOptionPane.showMessageDialog(this, "Please enter valid positive values.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            AMSignal signal = new AMSignal(variant, carrierFreq, messageFreqs, modulationIndex, phaseShift, waveformType, noiseType, noiseAmplitude, demodulationType);
            plotPanel.updateSignal(signal);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid input format. Please enter numeric values.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}