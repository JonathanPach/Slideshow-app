package com.company.view;

import com.company.bookmark.BookmarkManager;
import com.company.controller.SlideshowController;
import com.company.image.ImageLoader;
import com.company.image.ImageScaler;
import com.company.model.GalleryModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GalleryView {

    // ── constants ────────────────────────────────────────────────────────────
    private static final int WINDOW_WIDTH    = 800;
    private static final int WINDOW_HEIGHT   = 600;
    private static final int MIN_DELAY_MS    = 1000;
    private static final int MAX_DELAY_MS    = 10000;
    private static final int DEFAULT_DELAY_MS = 5000;

    // ── core dependencies ────────────────────────────────────────────────────
    private final GalleryModel      model;
    private final BookmarkManager   bookmarkManager;
    private final ImageLoader       imageLoader;
    private final SlideshowController controller;
    //private final BookmarkAnimator  animator;

    // ── ui components ────────────────────────────────────────────────────────
    private final JFrame  frame;
    private final JLabel  imageLabel;
    private final JLabel  counterLabel;
    private final JButton bookmarkButton;
    private final JButton pauseButton;
    private final JButton previousButton;
    private final JButton nextButton;
    private final JButton viewBookmarksButton;
    private final JComponent glassPane;

    // ── image state ──────────────────────────────────────────────────────────
    private BufferedImage currentImage;

    public GalleryView() {
        // init dependencies
        model           = new GalleryModel();
        bookmarkManager = new BookmarkManager();
        imageLoader     = new ImageLoader();
      //  animator        = null; // set after glass pane is created below

        // build frame first so glass pane exists
        frame = new JFrame("Random Gallery Viewer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(new Color(32, 32, 32));

        // glass pane for animations
        JPanel glass = new JPanel(null);
        glass.setOpaque(false);
        frame.setGlassPane(glass);
        glass.setVisible(true);
        glassPane = glass;

        // now we can build the animator with the glass pane
        BookmarkAnimator bookmarkAnimator = new BookmarkAnimator(glassPane);

        // image display
        imageLabel = new JLabel("", SwingConstants.CENTER);
        frame.add(imageLabel, BorderLayout.CENTER);

        // build buttons
        JButton folderButton = buildButton("Select Folder");
        previousButton       = buildButton("Previous");
        pauseButton          = buildButton("Pause");
        nextButton           = buildButton("Next");
        bookmarkButton       = buildButton("Bookmark Image");
        viewBookmarksButton  = buildButton("View Bookmarked Images");

        // counter
        counterLabel = new JLabel("0 / 0");
        counterLabel.setForeground(Color.WHITE);
        counterLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));

        // slider
        JSlider timerSlider = new JSlider(MIN_DELAY_MS, MAX_DELAY_MS, DEFAULT_DELAY_MS);
        timerSlider.setMajorTickSpacing(2000);
        timerSlider.setPaintTicks(true);
        timerSlider.setPaintLabels(true);
        timerSlider.setBackground(new Color(39, 39, 39));
        timerSlider.setForeground(Color.WHITE);

        // control panel
        JPanel controlPanel = new JPanel();
        controlPanel.setBackground(new Color(39, 39, 39));
        controlPanel.add(folderButton);
        controlPanel.add(previousButton);
        controlPanel.add(pauseButton);
        controlPanel.add(nextButton);
        controlPanel.add(bookmarkButton);
        controlPanel.add(viewBookmarksButton);
        JLabel timerLabel = new JLabel("Timer (ms): ");
        timerLabel.setForeground(Color.WHITE);
        controlPanel.add(timerLabel);
        controlPanel.add(timerSlider);
        controlPanel.add(counterLabel);
        frame.add(controlPanel, BorderLayout.SOUTH);

        // wire controller — depends on model and callbacks into the view
        controller = new SlideshowController(
                model,
                bookmarkManager,
                file -> imageLoader.load(
                        file,
                        model.getCurrentIndex(),
                        this::onImageLoaded,
                        ex -> ex.printStackTrace()
                ),
                text -> counterLabel.setText(text)
        );

        // wire slider to controller
        timerSlider.addChangeListener(e -> controller.setDelay(timerSlider.getValue()));

        // wire buttons to controller
        folderButton.addActionListener(e -> selectFolder());
        previousButton.addActionListener(e -> controller.previous());
        pauseButton.addActionListener(e -> {
            controller.togglePause();
            pauseButton.setText(controller.isPaused() ? "Resume" : "Pause");
        });
        nextButton.addActionListener(e -> controller.next());
        bookmarkButton.addActionListener(e -> {
            if (controller.bookmarkCurrent()) {
                bookmarkAnimator.animate(bookmarkButton);
            } else {
                JOptionPane.showMessageDialog(frame, "Already bookmarked!");
            }
            updateBookmarkButton();
        });
        viewBookmarksButton.addActionListener(e -> {
            boolean switched = controller.toggleBookmarkMode();
            if (!switched) {
                JOptionPane.showMessageDialog(frame, "No bookmarked images.");
            } else {
                viewBookmarksButton.setText(
                        controller.isBookmarkMode() ? "View All" : "View Bookmarked Images"
                );
            }
            updateBookmarkButton();
        });

        // resize listener
        frame.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                resizeImageToFitWindow();
            }
        });

        // keyboard navigation
        bindKeys();

        frame.setVisible(true);
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private JButton buildButton(String label) {
        JButton btn = new JButton(label);
        btn.setBackground(new Color(63, 65, 68));
        btn.setForeground(Color.WHITE);
        return btn;
    }

    private void selectFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setMultiSelectionEnabled(true);

        if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) return;

        File[] folders = chooser.getSelectedFiles();
        if (folders == null || folders.length == 0) {
            folders = new File[]{ chooser.getSelectedFile() };
        }

        List<File> files = new ArrayList<>();
        for (File folder : folders) {
            File[] found = folder.listFiles((dir, name) -> {
                String lower = name.toLowerCase();
                return lower.endsWith(".jpg")  || lower.endsWith(".jpeg")
                        || lower.endsWith(".png")  || lower.endsWith(".gif");
            });
            if (found != null) Collections.addAll(files, found);
        }

        if (files.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No image files found in the selected folders.");
            return;
        }

        model.loadFiles(files, true);
        controller.start();
        updateBookmarkButton();
    }

    private void onImageLoaded(BufferedImage img) {
        currentImage = img;
        resizeImageToFitWindow();
        updateBookmarkButton();
    }

    private void resizeImageToFitWindow() {
        if (currentImage == null || imageLabel.getWidth() == 0 || imageLabel.getHeight() == 0) return;
        BufferedImage scaled = ImageScaler.scaleFit(currentImage, imageLabel.getWidth(), imageLabel.getHeight());
        imageLabel.setIcon(new ImageIcon(scaled));
    }

    private void updateBookmarkButton() {
        if (controller.isCurrentBookmarked()) {
            bookmarkButton.setText("★ Bookmarked");
            bookmarkButton.setBackground(new Color(90, 75, 40));
            bookmarkButton.setForeground(new Color(255, 210, 80));
        } else {
            bookmarkButton.setText("Bookmark Image");
            bookmarkButton.setBackground(new Color(63, 65, 68));
            bookmarkButton.setForeground(Color.WHITE);
        }
    }

    private void bindKeys() {
        JRootPane root = frame.getRootPane();
        InputMap  im   = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am   = root.getActionMap();

        im.put(KeyStroke.getKeyStroke("RIGHT"), "next");
        am.put("next", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { controller.next(); }
        });

        im.put(KeyStroke.getKeyStroke("LEFT"), "prev");
        am.put("prev", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { controller.previous(); }
        });

        im.put(KeyStroke.getKeyStroke("SPACE"), "pause");
        am.put("pause", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                controller.togglePause();
                pauseButton.setText(controller.isPaused() ? "Resume" : "Pause");
            }
        });

        im.put(KeyStroke.getKeyStroke("B"), "bookmark");
        am.put("bookmark", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (controller.bookmarkCurrent()) {
                    new BookmarkAnimator(glassPane).animate(bookmarkButton);
                } else {
                    JOptionPane.showMessageDialog(frame, "Already bookmarked!");
                }
                updateBookmarkButton();
            }
        });
    }
}