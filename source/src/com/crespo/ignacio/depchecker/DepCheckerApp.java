package com.crespo.ignacio.depchecker;

import java.io.IOException;

public class DepCheckerApp {

    public static void main(final String[] args) throws IOException {
        final String paramFolderToSearchClasses = args[0];
        final String paramFolderToSaveGeneratedClass = args[1];
        final DependencyChecker checker = new DependencyChecker();
        checker.start(paramFolderToSearchClasses, paramFolderToSaveGeneratedClass);
    }

}
