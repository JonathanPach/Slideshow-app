package com.company;

import com.company.view.GalleryView;
import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GalleryView::new);
    }
}
