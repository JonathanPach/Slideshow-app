package com.company.controller;

import com.company.bookmark.BookmarkManager;
import com.company.model.GalleryModel;

import javax.swing.Timer;
import java.io.File;
import java.util.List;
import java.util.function.Consumer;

public class SlideshowController {

    private final GalleryModel mainModel;
    private final GalleryModel bookmarkModel;
    private final BookmarkManager bookmarkManager;
    private final Consumer<File> onImageChanged;
    private final Consumer<String> onCounterChanged;

    private GalleryModel activeModel;
    private boolean isBookmarkMode = false;
    private boolean isPaused = false;
    private Timer timer;

    private static final int DEFAULT_DELAY_MS = 5000;

    public SlideshowController(
            GalleryModel mainModel,
            BookmarkManager bookmarkManager,
            Consumer<File> onImageChanged,
            Consumer<String> onCounterChanged) {

        this.mainModel       = mainModel;
        this.bookmarkModel   = new GalleryModel();
        this.bookmarkManager = bookmarkManager;
        this.onImageChanged  = onImageChanged;
        this.onCounterChanged = onCounterChanged;
        this.activeModel     = mainModel;

        timer = new Timer(DEFAULT_DELAY_MS, e -> next());
    }

    public void start() {
        isPaused = false;
        timer.restart();
        next();
    }

    public void next() {
        File file = activeModel.next();
        if (file != null) {
            onImageChanged.accept(file);
            updateCounter();
            if (!isPaused) timer.restart();
        }
    }

    public void previous() {
        File file = activeModel.previous();
        if (file != null) {
            onImageChanged.accept(file);
            updateCounter();
            if (!isPaused) timer.restart();
        }
    }

    public void togglePause() {
        if (isPaused) {
            timer.start();
        } else {
            timer.stop();
        }
        isPaused = !isPaused;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void setDelay(int ms) {
        timer.setDelay(ms);
    }

    public boolean bookmarkCurrent() {
        File current = activeModel.current();
        if (current == null) return false;
        return bookmarkManager.add(current);
    }

    public boolean isCurrentBookmarked() {
        File current = activeModel.current();
        if (current == null) return false;
        return bookmarkManager.contains(current);
    }

    public boolean toggleBookmarkMode() {
        if (isBookmarkMode) {
            activeModel = mainModel;
        } else {
            List<File> bookmarks = bookmarkManager.getAll();
            if (bookmarks.isEmpty()) return false; // signal to caller: nothing to show
            bookmarkModel.loadFiles(bookmarks, false);
            bookmarkModel.setIndex(0);
            activeModel = bookmarkModel;
        }

        isBookmarkMode = !isBookmarkMode;
        File current = activeModel.current();
        if (current != null) {
            onImageChanged.accept(current);
            updateCounter();
        }
        return true;
    }

    public boolean isBookmarkMode() {
        return isBookmarkMode;
    }

    private void updateCounter() {
        String text = activeModel.isEmpty()
                ? "0 / 0"
                : (activeModel.getCurrentIndex() + 1) + " / " + activeModel.getSize();
        onCounterChanged.accept(text);
    }
}