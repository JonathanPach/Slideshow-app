package com.company.view;

import javax.swing.*;
import java.awt.*;

public class BookmarkAnimator {

    private final JComponent glassPane;

    public BookmarkAnimator(JComponent glassPane) {
        this.glassPane = glassPane;
    }

    public void animate(JButton sourceButton) {
        java.net.URL url = getClass().getResource("/icons/book_yoko.png");
        if (url == null) {
            System.err.println("Missing resource: /icons/book_yoko.png");
            return;
        }

        ImageIcon rawIcon = new ImageIcon(url);
        Image scaled = rawIcon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
        ImageIcon icon = new ImageIcon(scaled);

        Point p = SwingUtilities.convertPoint(
                sourceButton.getParent(),
                sourceButton.getLocation(),
                glassPane
        );

        int startX = p.x + sourceButton.getWidth()  / 2 - icon.getIconWidth()  / 2;
        int startY = p.y                             - icon.getIconHeight() / 2;

        FadingIcon anim = new FadingIcon(icon);
        anim.setLocation(startX, startY);
        glassPane.add(anim);
        glassPane.repaint();

        runAnimation(anim, startX, startY);
    }

    private void runAnimation(FadingIcon anim, int startX, int startY) {
        final int durationMs = 600;
        final int fpsDelay   = 16;
        final long start     = System.currentTimeMillis();

        Timer t = new Timer(fpsDelay, null);
        t.addActionListener(e -> {
            long elapsed  = System.currentTimeMillis() - start;
            float progress = Math.min(1.0f, elapsed / (float) durationMs);
            float eased   = (float) (1 - Math.pow(1 - progress, 3));

            anim.setLocation(startX, startY - (int) (40 * eased));
            anim.setAlpha(1.0f - progress);

            if (progress >= 1.0f) {
                t.stop();
                glassPane.remove(anim);
                glassPane.repaint();
            }
        });
        t.start();
    }

    // ── inner class ──────────────────────────────────────────────────────────

    private static class FadingIcon extends JComponent {

        private final Icon icon;
        private float alpha = 1.0f;

        FadingIcon(Icon icon) {
            this.icon = icon;
            setSize(icon.getIconWidth(), icon.getIconHeight());
            setOpaque(false);
        }

        void setAlpha(float a) {
            alpha = Math.max(0f, Math.min(1f, a));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            icon.paintIcon(this, g2, 0, 0);
            g2.dispose();
        }
    }
}