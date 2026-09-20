// 
// Translates lissajous UI parameters (frequency ratios, phase, trace rate)
// into numbers the SynthEngine audio thread can use.
// 
// Lissajous equations (Wikipedia):
//   x(t) = sin(A * t + delta)
//   y(t) = sin(B * t)
//   https://en.wikipedia.org/wiki/Lissajous_curve
// 
// The only non-obvious bit is converting "trace Hz" (how many full loops
// the beam traces per second) into a per-sample phase increment.
// A full loop is 2π radians, so:
//   phaseStep = 2π * traceHz / sampleRate

public class LissajousSynth {

    private final SynthEngine engine;

    private int    freqA   = 2;
    private int    freqB   = 3;
    private double phase   = 0.0;
    private int    traceHz = 60;

    // stores the shared engine reference that will receive all parameter updates
    public LissajousSynth(SynthEngine engine) {
        this.engine = engine;
    }

    // updates frequency A and pushes all current params to the audio engine
    public void setFreqA(int a)      { freqA   = a;   push(); }

    // updates frequency B and pushes all current params to the audio engine
    public void setFreqB(int b)      { freqB   = b;   push(); }

    // updates the phase offset and pushes all current params to the audio engine
    public void setPhase(double phi) { phase   = phi; push(); }


    // updates the trace rate and pushes all current params to the audio engine
    public void setTraceHz(int hz)   { traceHz = hz;  push(); }

    // recalculates the per sample phase step and sends all current values to the engine
    private void push() {
        double step = 2.0 * Math.PI * traceHz / engine.getSampleRate();
        engine.setLissajousParams(freqA, freqB, phase, step);
    }
}
