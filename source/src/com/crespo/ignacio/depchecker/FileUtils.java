package com.crespo.ignacio.depchecker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    static List<File> findClassesInFolder(final String folderPath) {
        final List<File> classes = new ArrayList<File>();
        final File folder = new File(folderPath);
        if (folder.isDirectory()) {
            final File[] files = folder.listFiles();
            for (final File file : files) {
                if (file.isDirectory()) {
                    classes.addAll(findClassesInFolder(file.getAbsolutePath()));
                } else if (file.getName().endsWith(".class")) {
                    classes.add(file);
                }
            }
        }
        return classes;
    }

}
