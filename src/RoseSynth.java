
// Converts rose curve UI parameters into numbers the SynthEngine audio thread can use.
// 
// Rose curve equations:
//   r(t) = cos(k · t)
//   x(t) = r(t) · cos(t + φ)     left channel
//   y(t) = r(t) · sin(t + φ)     right channel
// 
// The curve closes after one full rotation (t = 0 → 2π) for any integer k,
// 

public class RoseSynth {

    private final SynthEngine engine;

    private int    k       = 3;
    private double phase   = 0.0;
    private int    traceHz = 60;

    // stores the audio engine reference that will receive all parameter changes
    public RoseSynth(SynthEngine engine) {
        this.engine = engine;
    }

    // updates the petal count and syncs the audio engine
    public void setK(int k)        { this.k     = k;   push(); }

    // updates the rotation angle and syncs the audio engine
    public void setPhase(double p) { this.phase  = p;   push(); }
    
    // updates how many full curve loops play per second and syncs the audio engine
    public void setTraceHz(int hz) { this.traceHz = hz; push(); }

    // recalculates the per-sample phase step and sends all current values to the engine
    private void push() {
        double step = 2.0 * Math.PI * traceHz / engine.getSampleRate();
        engine.setRoseParams(k, phase, step);
    }
}
