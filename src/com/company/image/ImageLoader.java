package com.company.image;

import javax.imageio.ImageIO;
import javax.swing.SwingWorker;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.function.Consumer;

public class ImageLoader {

    private int lastRequestedIndex = -1;

    public void load(File file, int requestIndex, Consumer<BufferedImage> onSuccess, Consumer<Exception> onError) {
        lastRequestedIndex = requestIndex;

        new SwingWorker<BufferedImage, Void>() {
            @Override
            protected BufferedImage doInBackground() throws Exception {
                return ImageIO.read(file);
            }

            @Override
            protected void done() {
                if (requestIndex != lastRequestedIndex) return; // stale, discard
                try {
                    BufferedImage img = get();
                    if (img == null) {
                        onError.accept(new Exception("Unreadable or unsupported image: " + file.getAbsolutePath()));
                        return;
                    }
                    onSuccess.accept(img);
                } catch (Exception ex) {
                    onError.accept(ex);
                }
            }
        }.execute();
    }
}