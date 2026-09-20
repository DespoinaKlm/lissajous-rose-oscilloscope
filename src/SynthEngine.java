import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.ports.UnitOutputPort;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.UnitGenerator;

     
// Wraps JSyn and supports two audio modes:
// LISS — lissajous curve: x = sin(A·t + φ), y = sin(B·t)
// ROSE — rose curve:      x = cos(k·t)·cos(t + φ), y = cos(k·t)·sin(t + φ)
// follows some of the JSyn getting-started tutorial:
// https://softsynth.com/jsyn/docs/usersguide/

public class SynthEngine {

    private enum Mode { LISS, ROSE }

    // ── inner unit generator ──────
    private class XYGenerator extends UnitGenerator {

        // stereo output port — 2 channels (left and right) starting at 0
        final UnitOutputPort output = new UnitOutputPort(2, "Output", 0.0);

        // registers the stereo output port with the JSyn framework
        XYGenerator() { addPort(output); }

        // audio callback — picks the right generator for the current mode
        @Override
        public void generate(int start, int limit) {
            double[] outL = output.getValues(0);
            double[] outR = output.getValues(1);
            if (mode == Mode.LISS) fillLiss(outL, outR, start, limit);
            else                   fillRose(outL, outR, start, limit);
        }

        // x = sin(A*t + phase)  y = sin(B*t)
        private void fillLiss(double[] outL, double[] outR, int start, int limit) {
            double amp = amplitude;
            for (int i = start; i < limit; i++) {
                outL[i] = amp * Math.sin(lissA * phaseAccum + targetPhase);
                outR[i] = amp * Math.sin(lissB * phaseAccum);
                phaseAccum += phaseStep;
            }
        }

        // r = cos(k*t)  x = r*cos(t+phase)  y = r*sin(t+phase)
        private void fillRose(double[] outL, double[] outR, int start, int limit) {
            double amp = amplitude;
            for (int i = start; i < limit; i++) {
                double r = Math.cos(roseK * phaseAccum);
                outL[i] = amp * r * Math.cos(phaseAccum + targetPhase);
                outR[i] = amp * r * Math.sin(phaseAccum + targetPhase);
                phaseAccum += phaseStep;
            }
        }
    }

    // ── state shared between Swing EDT and audio thread ──────────────────────
    private volatile Mode   mode        = Mode.LISS;
    private volatile int    lissA       = 2;
    private volatile int    lissB       = 3;
    private volatile int    roseK       = 3;
    private volatile double targetPhase = 0.0;
    private volatile double phaseStep   = 0.01;
    private volatile double amplitude   = 0.8;

    // audio-thread-only state
    private double phaseAccum = 0.0;

    // ── JSyn wiring ──────────────────────────────────────────────────────────
    private final Synthesizer synth;
    private final XYGenerator gen;
    private final LineOut     lineOut;
    private boolean playing = false;

    // creates the synthesizer and wires the generator to the stereo line out
    public SynthEngine() {
        synth   = JSyn.createSynthesizer();
        gen     = new XYGenerator();
        lineOut = new LineOut();

        synth.add(gen);
        synth.add(lineOut);

        gen.output.connect(0, lineOut.input, 0); // left  = X
        gen.output.connect(1, lineOut.input, 1); // right = Y
    }

    // starts the synth and opens audio output if not already running
    public void play() {
        if (!playing) { synth.start(); lineOut.start(); playing = true; }
    }

    // closes audio output and halts the synthesizer
    public void stop() {
        if (playing) { lineOut.stop(); synth.stop(); playing = false; }
    }

    // shuts down audio when the window is closed
    public void dispose()       { stop(); }

    // returns the sample rate so callers can compute the correct phase step
    public int  getSampleRate() { return synth.getFrameRate(); }

    // adjusts the output level between zero and full scale
    public void setAmplitude(double amp) { amplitude = amp; }

    
    // switches to Lissajous mode and updates all frequency and phase values
    public void setLissajousParams(int a, int b, double phase, double step) {
        lissA       = a;
        lissB       = b;
        targetPhase = phase;
        phaseStep   = step;
        mode        = Mode.LISS;
    }

    // switches to Rose mode and updates the petal count and phase values
    public void setRoseParams(int k, double phase, double step) {
        roseK       = k;
        targetPhase = phase;
        phaseStep   = step;
        mode        = Mode.ROSE;
    }
}
