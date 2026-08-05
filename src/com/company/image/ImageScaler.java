package com.company.image;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageScaler {

    private ImageScaler() {}  // utility class, no instances needed

    public static BufferedImage scale(BufferedImage src, int targetWidth, int targetHeight) {
        BufferedImage scaled = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = scaled.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(src, 0, 0, targetWidth, targetHeight, null);
        g2.dispose();
        return scaled;
    }

    public static BufferedImage scaleFit(BufferedImage src, int containerWidth, int containerHeight) {
        double widthScale  = (double) containerWidth  / src.getWidth();
        double heightScale = (double) containerHeight / src.getHeight();
        double scale       = Math.min(widthScale, heightScale);

        int newWidth  = (int) (src.getWidth()  * scale);
        int newHeight = (int) (src.getHeight() * scale);

        return scale(src, newWidth, newHeight);
    }
}