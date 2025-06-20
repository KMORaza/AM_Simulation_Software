# Software zur Simulation der Amplitudenmodulation (Amplitude Modulation Simulation Software)

Software zur Simulation der Amplitudenmodulation, geschrieben in Java (This software simulates amplitude modulation and is written in Java)

## Functioning Logic

The software simulates AM signals, processes them through modulation, demodulation, and analysis, and supports time- and frequency-domain operations.

- **Signal Representation**:
  - Stores time array, message, carrier, modulated, demodulated signals, and frequency spectrum.
  - Uses high sampling rate (e.g., 44.1 kHz) for digital accuracy.
  - Manages data for all AM variants.
- **Signal Processing**:
  - Executes modulation, demodulation, noise addition, and spectrum computation.
  - Dynamically updates signals based on parameters (e.g., message frequency fm, carrier frequency fc, modulation index m).
  - Applies digital filters for demodulation and variant-specific processing.
- **Visualization**:
  - Plots time-domain signals (message, carrier, modulated, demodulated).
  - Computes frequency-domain spectra via Fast Fourier Transform (FFT).
  - Supports real-time signal animation and dynamic spectrum updates.
- **Analysis**:
  - Calculates Total Harmonic Distortion (THD) from harmonic peaks in FFT spectrum.
  - Computes Signal-to-Noise Ratio (SNR) by comparing signal and noise power.
  - Visualizes harmonics and noise in frequency plots.
- **Data Export**:
  - Exports signal data to CSV files for external use.

The software is modular, with components handling signal generation, processing, and analysis.

## Simulation and Modeling

The software models AM signals in time and frequency domains, simulating realistic signal behavior. Key aspects are:

- **Time-Domain Simulation**:
  - **Signal Generation**:
    - Message: `m(t) = Am * sin(2 * pi * fm * t)`, with amplitude `Am`, frequency `fm`.
    - Carrier: `c(t) = Ac * cos(2 * pi * fc * t)`, with amplitude `Ac`, frequency `fc`.
  - **Modulation**:
    - DSB-AM: `s(t) = Ac * [1 + m * m(t)] * cos(2 * pi * fc * t)`, `m` is modulation index.
    - DSB-SC: `s(t) = Ac * m(t) * cos(2 * pi * fc * t)`, no carrier.
    - SSB: Transmits one sideband (upper/lower).
    - VSB: One sideband plus partial other sideband.
    - QAM: Two signals on orthogonal carriers.
  - **Demodulation**:
    - Envelope detection: Rectify and low-pass filter.
    - Coherent detection: Multiply with carrier, then filter.
  - **Animation**: Updates plots at ~60 FPS for continuous signal simulation.
- **Frequency-Domain Simulation**:
  - **Spectrum**:
    - FFT transforms signals to show peaks at `fm`, `fc`, and sidebands `(fc ± fm)`.
  - **Dynamic Updates**:
    - Spectra adjust in real-time with parameter changes.
  - **Filtering**:
    - Low-pass for demodulation, band-pass for SSB/VSB.
- **Modeling**:
  - Discrete-time signals with sampling frequency `fs >> 2 * (fc + fm)`.
  - Noise as additive white Gaussian noise (AWGN) with adjustable amplitude.
- **Variants**:
  - DSB-AM: Carrier + sidebands.
  - DSB-SC: Sidebands only.
  - SSB: Single sideband.
  - VSB: Sideband + vestige.
  - QAM: Quadrature modulation.

## Utilization of Algorithms

- **Fast Fourier Transform (FFT)**:
  - Purpose: Generates frequency spectra for visualization, THD, SNR.
  - Algorithm: Cooley-Tukey FFT, O(N log N) complexity.
  - Used in: `SpectrumAnalysisFFT`, `TotalHarmonicDistortion`.
- **Digital Filtering**:
  - Low-pass: Removes high frequencies in demodulation (e.g., Butterworth).
  - Band-pass: Isolates sidebands in SSB/VSB.
  - Algorithm: FIR or IIR filters via DSP methods.
- **Modulation/Demodulation**:
  - Modulation: Time-domain multiplication per AM equations.
  - Demodulation: Envelope (rectify + filter) or coherent (carrier multiply + filter).
  - Algorithm: Optimized for real-time processing.
- **Noise Generation**:
  - AWGN via Gaussian random numbers.
  - Algorithm: Box-Muller transform.
  - Purpose: Channel noise for SNR.
- **THD Calculation**:
  - Formula: `THD = sqrt(V2^2 + V3^2 + ... + Vn^2) / V1`, `V1` is fundamental, `V2-Vn` are harmonics.
  - Algorithm: Peak detection in FFT, power summation.
- **SNR Calculation**:
  - Formula: `SNR = 10 * log10(Psignal / Pnoise)`, `Psignal` and `Pnoise` from frequency bands.
  - Algorithm: Power integration in FFT spectrum.
- **Animation**:
  - Incremental signal updates at 16 ms intervals (~60 FPS).
  - Algorithm: Efficient signal computation and rendering.

## Physics Models

- **AM Signal**:
  - Carrier amplitude varies with message.
  - DSB-AM: `s(t) = Ac * [1 + m * cos(2 * pi * fm * t)] * cos(2 * pi * fc * t)`.
  - Spectrum: Peaks at `fc`, `fc ± fm`.
- **Signals**:
  - Message: `m(t) = Am * sin(2 * pi * fm * t)`, e.g., `fm = 1 kHz`.
  - Carrier: `c(t) = Ac * cos(2 * pi * fc * t)`, e.g., `fc = 10 kHz`.
  - Physics: Electromagnetic wave modulation.
- **Noise**:
  - AWGN models thermal noise, zero mean, variable variance.
  - Physics: Random signal fluctuations.
- **Demodulation**:
  - Envelope: Rectification + filtering.
  - Coherent: Carrier synchronization + filtering.
  - Physics: Baseband signal recovery.
- **Spectrum**:
  - Power distribution across frequencies.
  - Physics: Fourier decomposition into sinusoids.
- **THD**:
  - Measures distortion from harmonics.
  - Physics: Nonlinear effects at integer multiples of fundamental.
- **SNR**:
  - Signal quality vs. noise.
  - Physics: Power ratio for communication performance.

## Screenshots

![](https://raw.githubusercontent.com/KMORaza/AM_Simulation_Software/refs/heads/main/AM%20Software/009/Screenshot.png)
