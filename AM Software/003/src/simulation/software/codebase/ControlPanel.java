package simulation.software.codebase;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * GUI panel for configuring AM signal parameters.
 */
public class ControlPanel extends JPanel {
    private JTextField carrierFreqField, modulationIndexField, multiToneFreqField, multiToneAmpField, noiseAmplitudeField, pulseDutyField, phaseShiftField, samplesField, durationField, filterAlphaField;
    private JComboBox<String> waveformCombo, noiseTypeCombo, demodulationCombo;
    private JSlider modulationIndexSlider;
    private SignalPlotPanel plotPanel;
    private String variant;

    /**
     * Constructor for ControlPanel.
     *
     * @param variant AM variant (DSB-AM, DSB-SC, SSB, VSB, QAM)
     */
    public ControlPanel(String variant) {
        this.variant = variant;
        setLayout(new GridBagLayout());
        setBackground(new Color(0, 0, 0));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Control Panel", 0, 0, new Font("Dialog", Font.BOLD, 12), new Color(192, 192, 192)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        setPreferredSize(new Dimension(400, 700));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        Font labelFont = new Font("Dialog", Font.BOLD, 12);
        Font fieldFont = new Font("Dialog", Font.PLAIN, 12);

        // Signal Parameters Panel
        JPanel signalPanel = new JPanel(new GridBagLayout());
        signalPanel.setBackground(new Color(0, 0, 0));
        signalPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Signal Parameters", 0, 0, labelFont, new Color(192, 192, 192)));

        // Carrier Frequency
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel carrierLabel = new JLabel("Carrier Frequency (Hz):");
        carrierLabel.setForeground(new Color(192, 192, 192));
        carrierLabel.setFont(labelFont);
        carrierLabel.setToolTipText("Frequency of the carrier signal (50-5000 Hz)");
        signalPanel.add(carrierLabel, gbc);
        gbc.gridx = 1;
        carrierFreqField = new JTextField("1000", 10);
        carrierFreqField.setBackground(new Color(0, 0, 0));
        carrierFreqField.setForeground(new Color(192, 192, 192));
        carrierFreqField.setFont(fieldFont);
        signalPanel.add(carrierFreqField, gbc);

        // Waveform Type
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel waveformLabel = new JLabel("Waveform Type:");
        waveformLabel.setForeground(new Color(192, 192, 192));
        waveformLabel.setFont(labelFont);
        waveformLabel.setToolTipText("Select the message signal waveform");
        signalPanel.add(waveformLabel, gbc);
        gbc.gridx = 1;
        waveformCombo = new JComboBox<>(new String[]{"Sine", "Square", "Triangle", "Sawtooth", "Pulse"});
        waveformCombo.setBackground(new Color(0, 0, 0));
        waveformCombo.setForeground(new Color(192, 192, 192));
        waveformCombo.setFont(fieldFont);
        signalPanel.add(waveformCombo, gbc);

        // Multi-tone Frequencies
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel freqLabel = new JLabel("Message Frequencies (Hz):");
        freqLabel.setForeground(new Color(192, 192, 192));
        freqLabel.setFont(labelFont);
        freqLabel.setToolTipText("Comma-separated frequencies (e.g., 100,200,300)");
        signalPanel.add(freqLabel, gbc);
        gbc.gridx = 1;
        multiToneFreqField = new JTextField("100,200", 10);
        multiToneFreqField.setBackground(new Color(0, 0, 0));
        multiToneFreqField.setForeground(new Color(192, 192, 192));
        multiToneFreqField.setFont(fieldFont);
        signalPanel.add(multiToneFreqField, gbc);

        // Multi-tone Amplitudes
        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel ampLabel = new JLabel("Amplitudes:");
        ampLabel.setForeground(new Color(192, 192, 192));
        ampLabel.setFont(labelFont);
        ampLabel.setToolTipText("Comma-separated amplitudes (e.g., 1,0.5)");
        signalPanel.add(ampLabel, gbc);
        gbc.gridx = 1;
        multiToneAmpField = new JTextField("1,1", 10);
        multiToneAmpField.setBackground(new Color(0, 0, 0));
        multiToneAmpField.setForeground(new Color(192, 192, 192));
        multiToneAmpField.setFont(fieldFont);
        signalPanel.add(multiToneAmpField, gbc);

        // Modulation Index
        gbc.gridx = 0;
        gbc.gridy = 4;
        JLabel modIndexLabel = new JLabel("Modulation Index:");
        modIndexLabel.setForeground(new Color(192, 192, 192));
        modIndexLabel.setFont(labelFont);
        modIndexLabel.setToolTipText("Modulation index (0-2)");
        signalPanel.add(modIndexLabel, gbc);
        gbc.gridx = 1;
        modulationIndexField = new JTextField("0.5", 5);
        modulationIndexField.setBackground(new Color(0, 0, 0));
        modulationIndexField.setForeground(new Color(192, 192, 192));
        modulationIndexField.setFont(fieldFont);
        signalPanel.add(modulationIndexField, gbc);
        gbc.gridy = 5;
        modulationIndexSlider = new JSlider(0, 200, 50);
        modulationIndexSlider.setBackground(new Color(0, 0, 0));
        modulationIndexSlider.setForeground(new Color(192, 192, 192));
        modulationIndexSlider.addChangeListener(e -> modulationIndexField.setText(String.format("%.2f", modulationIndexSlider.getValue() / 100.0)));
        signalPanel.add(modulationIndexSlider, gbc);

        // Pulse Duty Cycle
        gbc.gridx = 0;
        gbc.gridy = 6;
        JLabel pulseDutyLabel = new JLabel("Pulse Duty Cycle (%):");
        pulseDutyLabel.setForeground(new Color(192, 192, 192));
        pulseDutyLabel.setFont(labelFont);
        pulseDutyLabel.setToolTipText("Duty cycle for pulse waveform (0-100)");
        signalPanel.add(pulseDutyLabel, gbc);
        gbc.gridx = 1;
        pulseDutyField = new JTextField("50", 10);
        pulseDutyField.setBackground(new Color(0, 0, 0));
        pulseDutyField.setForeground(new Color(192, 192, 192));
        pulseDutyField.setFont(fieldFont);
        signalPanel.add(pulseDutyField, gbc);

        // Phase Shift (for QAM)
        if (variant.equals("QAM")) {
            gbc.gridx = 0;
            gbc.gridy = 7;
            JLabel phaseLabel = new JLabel("Phase Shift (degrees):");
            phaseLabel.setForeground(new Color(192, 192, 192));
            phaseLabel.setFont(labelFont);
            phaseLabel.setToolTipText("Phase shift for QAM (0-360)");
            signalPanel.add(phaseLabel, gbc);
            gbc.gridx = 1;
            phaseShiftField = new JTextField("90", 10);
            phaseShiftField.setBackground(new Color(0, 0, 0));
            phaseShiftField.setForeground(new Color(192, 192, 192));
            phaseShiftField.setFont(fieldFont);
            signalPanel.add(phaseShiftField, gbc);
        }

        // Sample Count
        gbc.gridx = 0;
        gbc.gridy = 8;
        JLabel samplesLabel = new JLabel("Sample Count:");
        samplesLabel.setForeground(new Color(192, 192, 192));
        samplesLabel.setFont(labelFont);
        samplesLabel.setToolTipText("Number of samples (1024-16384)");
        signalPanel.add(samplesLabel, gbc);
        gbc.gridx = 1;
        samplesField = new JTextField("4096", 10);
        samplesField.setBackground(new Color(0, 0, 0));
        samplesField.setForeground(new Color(192, 192, 192));
        samplesField.setFont(fieldFont);
        signalPanel.add(samplesField, gbc);

        // Duration
        gbc.gridx = 0;
        gbc.gridy = 9;
        JLabel durationLabel = new JLabel("Duration (s):");
        durationLabel.setForeground(new Color(192, 192, 192));
        durationLabel.setFont(labelFont);
        durationLabel.setToolTipText("Signal duration (0.01-1)");
        signalPanel.add(durationLabel, gbc);
        gbc.gridx = 1;
        durationField = new JTextField("0.05", 10);
        durationField.setBackground(new Color(0, 0, 0));
        durationField.setForeground(new Color(192, 192, 192));
        durationField.setFont(fieldFont);
        signalPanel.add(durationField, gbc);

        // Filter Alpha
        gbc.gridx = 0;
        gbc.gridy = 10;
        JLabel filterAlphaLabel = new JLabel("Filter Alpha:");
        filterAlphaLabel.setForeground(new Color(192, 192, 192));
        filterAlphaLabel.setFont(labelFont);
        filterAlphaLabel.setToolTipText("Low-pass filter alpha (0.01-1)");
        signalPanel.add(filterAlphaLabel, gbc);
        gbc.gridx = 1;
        filterAlphaField = new JTextField("0.1", 10);
        filterAlphaField.setBackground(new Color(0, 0, 0));
        filterAlphaField.setForeground(new Color(192, 192, 192));
        filterAlphaField.setFont(fieldFont);
        signalPanel.add(filterAlphaField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(signalPanel, gbc);

        // Noise Settings Panel
        JPanel noisePanel = new JPanel(new GridBagLayout());
        noisePanel.setBackground(new Color(0, 0, 0));
        noisePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Noise Settings", 0, 0, labelFont, new Color(192, 192, 192)));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        JLabel noiseTypeLabel = new JLabel("Noise Type:");
        noiseTypeLabel.setForeground(new Color(192, 192, 192));
        noiseTypeLabel.setFont(labelFont);
        noiseTypeLabel.setToolTipText("Select noise type");
        noisePanel.add(noiseTypeLabel, gbc);
        gbc.gridx = 1;
        noiseTypeCombo = new JComboBox<>(new String[]{"None", "White", "Gaussian", "Pink"});
        noiseTypeCombo.setBackground(new Color(0, 0, 0));
        noiseTypeCombo.setForeground(new Color(192, 192, 192));
        noiseTypeCombo.setFont(fieldFont);
        noisePanel.add(noiseTypeCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel noiseAmpLabel = new JLabel("Noise Amplitude:");
        noiseAmpLabel.setForeground(new Color(192, 192, 192));
        noiseAmpLabel.setFont(labelFont);
        noiseAmpLabel.setToolTipText("Noise amplitude (0-1)");
        noisePanel.add(noiseAmpLabel, gbc);
        gbc.gridx = 1;
        noiseAmplitudeField = new JTextField("0.1", 10);
        noiseAmplitudeField.setBackground(new Color(0, 0, 0));
        noiseAmplitudeField.setForeground(new Color(192, 192, 192));
        noiseAmplitudeField.setFont(fieldFont);
        noisePanel.add(noiseAmplitudeField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        add(noisePanel, gbc);

        // Demodulation Settings Panel
        JPanel demodPanel = new JPanel(new GridBagLayout());
        demodPanel.setBackground(new Color(0, 0, 0));
        demodPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Demodulation Settings", 0, 0, labelFont, new Color(192, 192, 192)));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        JLabel demodLabel = new JLabel("Demodulation Type:");
        demodLabel.setForeground(new Color(192, 192, 192));
        demodLabel.setFont(labelFont);
        demodLabel.setToolTipText("Select demodulation method");
        demodPanel.add(demodLabel, gbc);
        gbc.gridx = 1;
        demodulationCombo = new JComboBox<>(new String[]{"None", "Coherent", "Non-Coherent"});
        demodulationCombo.setBackground(new Color(0, 0, 0));
        demodulationCombo.setForeground(new Color(192, 192, 192));
        demodulationCombo.setFont(fieldFont);
        demodPanel.add(demodulationCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        add(demodPanel, gbc);

        // Action Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(new Color(0, 0, 0));

        JButton updateButton = new JButton("Update Signal");
        updateButton.setBackground(new Color(0, 0, 0));
        updateButton.setForeground(new Color(192, 192, 192));
        updateButton.setFont(labelFont);
        updateButton.setToolTipText("Update the signal with current parameters");
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateSignal();
            }
        });
        buttonPanel.add(updateButton);

        JButton exportButton = new JButton("Export Data");
        exportButton.setBackground(new Color(0, 0, 0));
        exportButton.setForeground(new Color(192, 192, 192));
        exportButton.setFont(labelFont);
        exportButton.setToolTipText("Export signal data to CSV");
        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportData();
            }
        });
        buttonPanel.add(exportButton);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        add(buttonPanel, gbc);
    }

    /**
     * Sets the plot panel for updating signals.
     */
    public void setPlotPanel(SignalPlotPanel plotPanel) {
        this.plotPanel = plotPanel;
    }

    /**
     * Updates the signal based on user inputs.
     */
    private void updateSignal() {
        try {
            double carrierFreq = Double.parseDouble(carrierFreqField.getText());
            if (carrierFreq < 50 || carrierFreq > 5000) {
                JOptionPane.showMessageDialog(this, "Carrier frequency must be between 50 and 5000 Hz.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String[] freqStrings = multiToneFreqField.getText().split(",");
            String[] ampStrings = multiToneAmpField.getText().split(",");
            if (freqStrings.length == 0 || ampStrings.length == 0) {
                JOptionPane.showMessageDialog(this, "Message frequencies and amplitudes cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (freqStrings.length != ampStrings.length) {
                JOptionPane.showMessageDialog(this, "Number of frequencies and amplitudes must match.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            double[] messageFreqs = new double[freqStrings.length];
            double[] amplitudes = new double[ampStrings.length];
            for (int i = 0; i < freqStrings.length; i++) {
                messageFreqs[i] = Double.parseDouble(freqStrings[i].trim());
                if (messageFreqs[i] <= 0) {
                    JOptionPane.showMessageDialog(this, "Message frequencies must be positive.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            for (int i = 0; i < ampStrings.length; i++) {
                amplitudes[i] = Double.parseDouble(ampStrings[i].trim());
                if (amplitudes[i] <= 0) {
                    JOptionPane.showMessageDialog(this, "Amplitudes must be positive.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            double modulationIndex = Double.parseDouble(modulationIndexField.getText());
            if (modulationIndex < 0 || modulationIndex > 2) {
                JOptionPane.showMessageDialog(this, "Modulation index must be between 0 and 2.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double noiseAmplitude = Double.parseDouble(noiseAmplitudeField.getText());
            if (noiseAmplitude < 0 || noiseAmplitude > 1) {
                JOptionPane.showMessageDialog(this, "Noise amplitude must be between 0 and 1.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double phaseShift = phaseShiftField != null ? Double.parseDouble(phaseShiftField.getText()) : 0;
            if (variant.equals("QAM") && (phaseShift < 0 || phaseShift > 360)) {
                JOptionPane.showMessageDialog(this, "Phase shift must be between 0 and 360 degrees for QAM.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double pulseDutyCycle = Double.parseDouble(pulseDutyField.getText());
            if (pulseDutyCycle < 0 || pulseDutyCycle > 100) {
                JOptionPane.showMessageDialog(this, "Pulse duty cycle must be between 0 and 100%.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int samples = Integer.parseInt(samplesField.getText());
            if (samples < 1024 || samples > 16384) {
                JOptionPane.showMessageDialog(this, "Sample count must be between 1024 and 16384.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double duration = Double.parseDouble(durationField.getText());
            if (duration < 0.01 || duration > 1) {
                JOptionPane.showMessageDialog(this, "Duration must be between 0.01 and 1 second.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double filterAlpha = Double.parseDouble(filterAlphaField.getText());
            if (filterAlpha < 0.01 || filterAlpha > 1) {
                JOptionPane.showMessageDialog(this, "Filter alpha must be between 0.01 and 1.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String waveformType = (String) waveformCombo.getSelectedItem();
            String noiseType = (String) noiseTypeCombo.getSelectedItem();
            String demodulationType = (String) demodulationCombo.getSelectedItem();

            AMSignal signal = new AMSignal(variant, carrierFreq, messageFreqs, amplitudes, modulationIndex, phaseShift,
                    waveformType, noiseType, noiseAmplitude, demodulationType, pulseDutyCycle, samples, duration, filterAlpha);
            plotPanel.updateSignal(signal);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid input format. Please enter numeric values.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Exports the current signal data to a CSV file.
     */
    private void exportData() {
        if (plotPanel.getCurrentSignal() != null) {
            DataExporter.exportToCSV(plotPanel.getCurrentSignal(), variant + "_signal_data.csv");
            JOptionPane.showMessageDialog(this, "Data exported to " + variant + "_signal_data.csv", "Export Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "No signal data to export.", "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}