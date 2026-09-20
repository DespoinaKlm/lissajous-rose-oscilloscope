import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

// main window — Lissajous mode and Rose curve mode
// connect 3.5mm audio out to oscilloscope CH1 (X) and CH2 (Y), set it to XY mode
public class OscilloscopeApp extends JFrame {

    private final SynthEngine    synthEngine    = new SynthEngine();
    private final LissajousSynth lissajousSynth = new LissajousSynth(synthEngine);
    private final RoseSynth      roseSynth      = new RoseSynth(synthEngine);
    private final LissajousPanel lissajousPanel = new LissajousPanel();
    private final RosePanel      rosePanel      = new RosePanel();

    private final JPanel centreCards    = new JPanel(new CardLayout());
    private final JPanel rightModeCards = new JPanel(new CardLayout());

    private String  currentMode = "lissajous";
    private JButton playBtn, stopBtn;

    // lissajous controls
    private JSpinner      spinA, spinB;
    private JSlider       lissPhaseSlider;
    private Timer         lissAutoTimer;
    private JToggleButton lissAutoBtn;
    private int           lissAutoDir   = 1;
    private double        lissAutoAccum = 0;

    // rose controls
    private JSpinner      roseSpinK;
    private JSlider       rosePhaseSlider;
    private Timer         roseAutoTimer;
    private JToggleButton roseAutoBtn;
    private int           roseAutoDir   = 1;
    private double        roseAutoAccum = 0;

    // builds the two-panel layout and wires up the window close handler
    public OscilloscopeApp() {
        super("Oscilloscope Synthesizer");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(6, 6));

        add(buildCentre(),     BorderLayout.CENTER);
        add(buildRightPanel(), BorderLayout.EAST);

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                if (lissAutoTimer != null) lissAutoTimer.stop();
                if (roseAutoTimer != null) roseAutoTimer.stop();
                synthEngine.dispose();
            }
        });

        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    // creates the radio buttons to switch between Lissajous and Rose modes
    private JPanel buildModeSelector() {
        JRadioButton rbLiss = new JRadioButton("Lissajous", true);
        JRadioButton rbRose = new JRadioButton("Rose",      false);
        ButtonGroup g = new ButtonGroup();
        g.add(rbLiss); g.add(rbRose);
        rbLiss.addActionListener(e -> switchMode("lissajous"));
        rbRose.addActionListener(e -> switchMode("rose"));

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row.add(new JLabel("Mode:")); row.add(rbLiss); row.add(rbRose);
        return row;
    }

    // stops audio and any running timers then flips both card panels to the chosen mode
    private void switchMode(String mode) {
        currentMode = mode;

        // stop audio and any running auto timers on every mode switch
        synthEngine.stop();
        playBtn.setEnabled(true);
        stopBtn.setEnabled(false);
        if (lissAutoTimer != null) { lissAutoTimer.stop(); lissAutoTimer = null; }
        if (lissAutoBtn  != null)    lissAutoBtn.setSelected(false);
        if (roseAutoTimer != null) { roseAutoTimer.stop(); roseAutoTimer = null; }
        if (roseAutoBtn  != null)    roseAutoBtn.setSelected(false);

        ((CardLayout) centreCards.getLayout()).show(centreCards, mode);
        ((CardLayout) rightModeCards.getLayout()).show(rightModeCards, mode);

        if (mode.equals("lissajous")) {
            lissajousSynth.setFreqA((int) spinA.getValue());
            lissajousSynth.setFreqB((int) spinB.getValue());
            lissajousSynth.setPhase(Math.toRadians(lissPhaseSlider.getValue()));
        } else {
            roseSynth.setK((int) roseSpinK.getValue());
            roseSynth.setPhase(Math.toRadians(rosePhaseSlider.getValue()));
        }
    }

    // wraps both visualisation panels in a card layout so only the active one shows
    private JPanel buildCentre() {
        centreCards.add(lissajousPanel, "lissajous");
        centreCards.add(rosePanel,      "rose");
        return centreCards;
    }

    // stacks the mode selector play-stop row shared sliders and mode-specific controls into the right sidebar
    private JPanel buildRightPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setPreferredSize(new Dimension(400, 0));

        panel.add(buildModeSelector());
        panel.add(Box.createVerticalStrut(10));
        panel.add(new JSeparator());
        panel.add(Box.createVerticalStrut(10));
        panel.add(buildPlayStop());
        panel.add(Box.createVerticalStrut(10));
        panel.add(new JSeparator());
        panel.add(Box.createVerticalStrut(10));
        panel.add(buildSharedSliders());
        panel.add(Box.createVerticalStrut(10));
        panel.add(new JSeparator());
        panel.add(Box.createVerticalStrut(10));

        rightModeCards.add(buildLissajousControls(), "lissajous");
        rightModeCards.add(buildRoseControls(),      "rose");
        panel.add(rightModeCards);

        return panel;
    }

    // creates the Play and Stop buttons and connects them to the synth engine
    private JPanel buildPlayStop() {
        playBtn = new JButton("▶  Play");
        stopBtn = new JButton("■  Stop");
        stopBtn.setEnabled(false);
        playBtn.addActionListener(e -> {
            synthEngine.play();
            playBtn.setEnabled(false); stopBtn.setEnabled(true);
        });
        stopBtn.addActionListener(e -> {
            synthEngine.stop();
            stopBtn.setEnabled(false); playBtn.setEnabled(true);
        });
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        row.add(playBtn); row.add(stopBtn);
        return row;
    }

    // builds the trace rate and signal level sliders shared across both modes
    private JPanel buildSharedSliders() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel tl = new JLabel("Trace Rate:  60 Hz");
        JSlider ts = new JSlider(10, 250, 60);
        ts.addChangeListener(e -> {
            int hz = ts.getValue();
            tl.setText("Trace Rate:  " + hz + " Hz");
            if (currentMode.equals("lissajous")) lissajousSynth.setTraceHz(hz);
            else                                 roseSynth.setTraceHz(hz);
        });
        panel.add(tl); panel.add(ts);
        panel.add(Box.createVerticalStrut(8));

        JLabel al = new JLabel("Signal Level:  80%");
        JSlider as = new JSlider(0, 100, 80);
        as.addChangeListener(e -> {
            al.setText("Signal Level:  " + as.getValue() + "%");
            synthEngine.setAmplitude(as.getValue() / 100.0);
        });
        panel.add(al); panel.add(as);

        return panel;
    }

    // ── lissajous controls ────────────────────────────────────────────────────

    // builds the frequency spinners phase slider and auto-sweep toggle for Lissajous mode
    private JPanel buildLissajousControls() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        spinA = new JSpinner(new SpinnerNumberModel(2, 1, 12, 1));
        spinB = new JSpinner(new SpinnerNumberModel(3, 1, 12, 1));

        JLabel phaseLabel = new JLabel("Phase δ:  0°");
        lissPhaseSlider = new JSlider(0, 360, 0);

        Runnable sync = () -> {
            int a = (int) spinA.getValue(), b = (int) spinB.getValue();
            double phi = Math.toRadians(lissPhaseSlider.getValue());
            phaseLabel.setText("Phase δ:  " + lissPhaseSlider.getValue() + "°");
            lissajousPanel.setParams(a, b, phi);
            lissajousSynth.setFreqA(a);
            lissajousSynth.setFreqB(b);
            lissajousSynth.setPhase(phi);
        };
        spinA.addChangeListener(e -> sync.run());
        spinB.addChangeListener(e -> sync.run());
        lissPhaseSlider.addChangeListener(e -> sync.run());

        lissAutoBtn = new JToggleButton("⟳ Auto");
        JLabel  speedLabel  = new JLabel("Speed:  1.5");
        JSlider speedSlider = new JSlider(10, 30, 15);
        speedSlider.setEnabled(false);
        speedSlider.addChangeListener(e ->
            speedLabel.setText(String.format("Speed:  %.1f", speedSlider.getValue() / 10.0)));

        lissAutoBtn.addActionListener(e -> {
            if (lissAutoBtn.isSelected()) {
                speedSlider.setEnabled(true);
                lissAutoDir = 1; lissAutoAccum = lissPhaseSlider.getValue();
                lissAutoTimer = new Timer(30, tick -> {
                    lissAutoAccum += lissAutoDir * (speedSlider.getValue() / 10.0);
                    if (lissAutoAccum >= 360) { lissAutoAccum = 360; lissAutoDir = -1; }
                    else if (lissAutoAccum <= 0) { lissAutoAccum = 0; lissAutoDir = 1; }
                    lissPhaseSlider.setValue((int) Math.round(lissAutoAccum));
                });
                lissAutoTimer.start();
            } else {
                if (lissAutoTimer != null) { lissAutoTimer.stop(); lissAutoTimer = null; }
                speedSlider.setEnabled(false);
            }
        });

        JPanel abRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        abRow.add(new JLabel("A:")); abRow.add(spinA);
        abRow.add(Box.createHorizontalStrut(8));
        abRow.add(new JLabel("B:")); abRow.add(spinB);

        JPanel phaseRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        phaseRow.add(phaseLabel); phaseRow.add(lissAutoBtn);

        panel.add(abRow);
        panel.add(Box.createVerticalStrut(6));
        panel.add(phaseRow);
        panel.add(lissPhaseSlider);
        panel.add(speedLabel);
        panel.add(speedSlider);
        return panel;
    }

    // ── rose controls ─────────────────────────────────────────────────────────

    // builds the petal count spinner rotation slider and auto-rotate toggle for Rose mode
    private JPanel buildRoseControls() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        roseSpinK = new JSpinner(new SpinnerNumberModel(3, 1, 12, 1));

        JLabel phaseLabel = new JLabel("Rotation:  0°");
        rosePhaseSlider = new JSlider(0, 360, 0);

        Runnable sync = () -> {
            int k      = (int) roseSpinK.getValue();
            double phi = Math.toRadians(rosePhaseSlider.getValue());
            phaseLabel.setText("Rotation:  " + rosePhaseSlider.getValue() + "°");
            rosePanel.setParams(k, phi);
            roseSynth.setK(k);
            roseSynth.setPhase(phi);
        };
        roseSpinK.addChangeListener(e -> sync.run());
        rosePhaseSlider.addChangeListener(e -> sync.run());

        roseAutoBtn = new JToggleButton("⟳ Auto rotate");
        JLabel  speedLabel  = new JLabel("Speed:  1.5");
        JSlider speedSlider = new JSlider(10, 30, 15);
        speedSlider.setEnabled(false);
        speedSlider.addChangeListener(e ->
            speedLabel.setText(String.format("Speed:  %.1f", speedSlider.getValue() / 10.0)));

        roseAutoBtn.addActionListener(e -> {
            if (roseAutoBtn.isSelected()) {
                speedSlider.setEnabled(true);
                roseAutoDir = 1; roseAutoAccum = rosePhaseSlider.getValue();
                roseAutoTimer = new Timer(30, tick -> {
                    roseAutoAccum += roseAutoDir * (speedSlider.getValue() / 10.0);
                    if (roseAutoAccum >= 360) { roseAutoAccum = 360; roseAutoDir = -1; }
                    else if (roseAutoAccum <= 0) { roseAutoAccum = 0; roseAutoDir = 1; }
                    rosePhaseSlider.setValue((int) Math.round(roseAutoAccum));
                });
                roseAutoTimer.start();
            } else {
                if (roseAutoTimer != null) { roseAutoTimer.stop(); roseAutoTimer = null; }
                speedSlider.setEnabled(false);
            }
        });

        JPanel kRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        kRow.add(new JLabel("Petals (k):")); kRow.add(roseSpinK);

        JPanel phaseRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        phaseRow.add(phaseLabel); phaseRow.add(roseAutoBtn);

        panel.add(kRow);
        panel.add(Box.createVerticalStrut(6));
        panel.add(phaseRow);
        panel.add(rosePhaseSlider);
        panel.add(speedLabel);
        panel.add(speedSlider);
        return panel;
    }

    // boots the app on the Swing event dispatch thread
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new OscilloscopeApp().setVisible(true));
    }
}
