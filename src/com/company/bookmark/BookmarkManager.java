package com.company.bookmark;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class BookmarkManager {

    private final Set<File> bookmarkedImages = new LinkedHashSet<>();

    public boolean add(File file) {
        return bookmarkedImages.add(file); // returns false if already present
    }

    public boolean contains(File file) {
        return bookmarkedImages.contains(file);
    }

    public boolean isEmpty() {
        return bookmarkedImages.isEmpty();
    }

    public List<File> getAll() {
        return new ArrayList<>(bookmarkedImages); // defensive copy, caller can't mutate our set
    }

    public void clear() {
        bookmarkedImages.clear();
    }
}