package com.company.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GalleryModel {

    private final List<File> imageFiles = new ArrayList<>();
    private int currentIndex = -1;

    public void loadFiles(List<File> files, boolean shuffle) {
        imageFiles.clear();
        imageFiles.addAll(files);
        if (shuffle) {
            Collections.shuffle(imageFiles);
        }
        currentIndex = -1;
    }

    public File next() {
        if (imageFiles.isEmpty()) return null;
        currentIndex = (currentIndex + 1) % imageFiles.size();
        return current();
    }

    public File previous() {
        if (imageFiles.isEmpty()) return null;
        currentIndex = (currentIndex - 1 + imageFiles.size()) % imageFiles.size();
        return current();
    }

    public File current() {
        if (imageFiles.isEmpty() || currentIndex < 0) return null;
        return imageFiles.get(currentIndex);
    }

    public void setIndex(int index) {
        if (index >= 0 && index < imageFiles.size()) {
            currentIndex = index;
        }
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public int getSize() {
        return imageFiles.size();
    }

    public boolean isEmpty() {
        return imageFiles.isEmpty();
    }

    public List<File> getFiles() {
        return new ArrayList<>(imageFiles); // defensive copy
    }
}
