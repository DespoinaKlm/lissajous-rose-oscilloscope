import javax.swing.*;
import java.awt.*;
import java.awt.geom.GeneralPath;

// draws the lissajous figure on screen so you can see what the oscilloscope is going to show
// call setParams() whenever a slider changes — it just calls repaint() and the panel redraws itself
public class LissajousPanel extends JPanel {

    private static final int POINTS = 1024; // enough resolution for any A:B ratio up to about 10:11

    private int    freqA = 2, freqB = 3;
    private double phase = 0.0;

    // sets up the black canvas and locks in the preferred panel size
    public LissajousPanel() {
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(560, 520));
    }

    // updates frequency ratio and phase then schedules a repaint
    public void setParams(int a, int b, double phi) {
        freqA = a; freqB = b; phase = phi;
        repaint(); // schedules a call to paintComponent on the swing event thread
    }

    // calls each drawing helper in sequence
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        int w = getWidth(), h = getHeight(), pad = 24;

        drawGraticule(g2, w, h);
        drawTrace(g2, w, h, pad);
        drawBorder(g2, w, h);
        drawEquations(g2, w, h);
    }

    // faint 10x10 grid like a real oscilloscope screen
    private void drawGraticule(Graphics2D g2, int w, int h) {
        g2.setColor(new Color(0, 55, 0));
        g2.setStroke(new BasicStroke(0.5f));
        for (int i = 1; i < 10; i++) {
            g2.drawLine(w*i/10, 0, w*i/10, h);
            g2.drawLine(0, h*i/10, w, h*i/10);
        }
        g2.setColor(new Color(0, 90, 0));
        g2.drawLine(w/2, 0, w/2, h);
        g2.drawLine(0, h/2, w, h/2);
    }

    // the lissajous curve itself
    // parametric equations (see https://mathworld.wolfram.com/LissajousCurve.html):
    //   x(t) = sin(A * t + phase)
    //   y(t) = sin(B * t)
    // t goes from 0 to 2π, x and y are in [-1, 1]
    //
    // to get screen coordinates from normalised values:
    //   screen_x = pad + (x + 1) / 2  *  drawable_width
    //   screen_y = pad + (1 - y) / 2  *  drawable_height ( Y flipped because screen Y grows down )
    //
    // GeneralPath docs: https://docs.oracle.com/javase/8/docs/api/java/awt/geom/GeneralPath.html
    private void drawTrace(Graphics2D g2, int w, int h, int pad) {
        int dw = w - 2*pad, dh = h - 2*pad;
        GeneralPath path = new GeneralPath();
        for (int i = 0; i <= POINTS; i++) {
            double t  = 2.0 * Math.PI * i / POINTS;
            double x  = Math.sin(freqA * t + phase);
            double y  = Math.sin(freqB * t);
            float  sx = pad + (float)((x + 1.0) / 2.0 * dw);
            float  sy = pad + (float)((1.0 - y) / 2.0 * dh);
            if (i == 0) path.moveTo(sx, sy); else path.lineTo(sx, sy);
        }
        g2.setColor(new Color(0, 255, 65));
        g2.setStroke(new BasicStroke(1.8f));
        g2.draw(path);
    }

    // draws the green frame that wraps the scope screen
    private void drawBorder(Graphics2D g2, int w, int h) {
        g2.setColor(new Color(0, 150, 40));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRect(0, 0, w-1, h-1);
    }

    // shows the actual equations with the current values
    private void drawEquations(Graphics2D g2, int w, int h) {
        g2.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        FontMetrics fm = g2.getFontMetrics();
        int deg = (int) Math.round(Math.toDegrees(phase));

        String lineX = deg == 0
                ? String.format("x(t) = sin( %dt )", freqA)
                : String.format("x(t) = sin( %dt + %d° )", freqA, deg);
        String lineY = String.format("y(t) = sin( %dt )", freqB);

        int lh   = fm.getHeight();
        int boxW = Math.max(fm.stringWidth(lineX), fm.stringWidth(lineY)) + 16;
        int boxH = lh * 2 + 14;
        int bx   = 8, by = h - boxH - 8;

        g2.setColor(new Color(0, 0, 0, 170));
        g2.fillRect(bx, by, boxW, boxH);
        g2.setColor(new Color(0, 140, 40));
        g2.drawRect(bx, by, boxW, boxH);
        g2.setColor(new Color(0, 220, 60));
        g2.drawString(lineX, bx + 8, by + lh);
        g2.drawString(lineY, bx + 8, by + lh*2 + 2);
    }
}
