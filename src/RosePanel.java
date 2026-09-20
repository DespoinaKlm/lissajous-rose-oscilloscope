import javax.swing.*;
import java.awt.*;
import java.awt.geom.GeneralPath;

// 
// Draws a rose curve on an oscilloscope-style screen.
// 
// Parametric equations (polar form):
//   r(t) = cos(k · t)
//   x(t) = r(t) · cos(t + φ)
//   y(t) = r(t) · sin(t + φ)
// 
// k controls the number of petals:
//   odd k  → k petals
//   even k → 2k petals
// 
// φ rotates the whole figure.
// 
// Reference: https://mathworld.wolfram.com/RoseCurve.html

public class RosePanel extends JPanel {

    private static final int POINTS = 1024; // points that will be plotted for the curve

    private int    k     = 3;
    private double phase = 0.0;

    // sets up the black canvas and locks in the preferred panel size
    public RosePanel() {
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(560, 520));
    }

    // stores the new petal count and rotation angle then asks swing to redraw
    public void setParams(int k, double phi) {
        this.k     = k;
        this.phase = phi;
        repaint();
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
        drawEquation(g2, w, h);
    }

    // paints faint grid lines and brighter centre axes like a real oscilloscope screen
    private void drawGraticule(Graphics2D g2, int w, int h) {
        g2.setColor(new Color(0, 55, 0));
        g2.setStroke(new BasicStroke(0.5f));
        for (int i = 1; i < 10; i++) {
            g2.drawLine(w*i/10, 0, w*i/10, h);
            g2.drawLine(0, h*i/10, w, h*i/10);
        }
        g2.setColor(new Color(0, 90, 0));
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawLine(w/2, 0, w/2, h);
        g2.drawLine(0, h/2, w, h/2);
    }

    // samples the curve at evenly spaced angles and connects them with a green line
    private void drawTrace(Graphics2D g2, int w, int h, int pad) {
        // drawable width / height
        int dw = w - 2*pad;
        int dh = h - 2*pad;
        GeneralPath path = new GeneralPath();

        for (int i = 0; i <= POINTS; i++) {
            double t  = 2.0 * Math.PI * i / POINTS;
            double r  = Math.cos(k * t);
            double x  =  r * Math.cos(t + phase);
            double y  =  r * Math.sin(t + phase);
            float  sx = pad + (float)((x + 1.0) / 2.0 * dw);
            float  sy = pad + (float)((1.0 - y) / 2.0 * dh); // Y flipped
            if (i == 0) path.moveTo(sx, sy); else path.lineTo(sx, sy);
        }
        g2.setColor(new Color(0, 255, 65));
        g2.setStroke(new BasicStroke(1.8f));
        g2.draw(path);
    }

    // draws the green frame that wraps the oscilloscope screen
    private void drawBorder(Graphics2D g2, int w, int h) {
        g2.setColor(new Color(0, 150, 40));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRect(0, 0, w-1, h-1);
    }

    // renders a translucent info box in the bottom left showing the formula and petal count
    private void drawEquation(Graphics2D g2, int w, int h) {
        g2.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        FontMetrics fm = g2.getFontMetrics();

        int petals = (k % 2 == 0) ? 2 * k : k;
        int deg    = (int) Math.round(Math.toDegrees(phase));

        String line1 = String.format("r(t) = cos( %dt )", k);
        String line3 = petals + " petals";

        String line2;
        if (deg == 0) {
            line2 = "x = r·cos(t),  y = r·sin(t)";
        } else {
            line2 = String.format("x = r·cos(t+%d°),  y = r·sin(t+%d°)", deg, deg);
        }

        int lh   = fm.getHeight();
        int boxW = Math.max(fm.stringWidth(line1), Math.max(fm.stringWidth(line2), fm.stringWidth(line3))) + 16;
        int boxH = lh * 3 + 14;
        int bx   = 8, by = h - boxH - 8;

        g2.setColor(new Color(0, 0, 0, 170));
        g2.fillRect(bx, by, boxW, boxH);
        g2.setColor(new Color(0, 140, 40));
        g2.drawRect(bx, by, boxW, boxH);
        g2.setColor(new Color(0, 220, 60));
        g2.drawString(line1, bx + 8, by + lh);
        g2.drawString(line2, bx + 8, by + lh * 2 + 2);
        g2.setColor(new Color(0, 160, 50));
        g2.drawString(line3, bx + 8, by + lh * 3 + 2);
    }
}
