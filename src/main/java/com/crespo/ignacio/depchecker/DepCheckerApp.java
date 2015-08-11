package com.crespo.ignacio.depchecker;

import java.io.File;
import java.io.IOException;

public class DepCheckerApp {

    public static void main(final String[] args) throws IOException {
        String repositoryFolder = args.length > 0 ? args[0] : ".";
        String classesFolder = args.length > 1 ? args[1] : ".";
        Console.log("REPOSITORY FOLDER TO READ MODIFIED .JAVA FILES: " + new File(repositoryFolder).getAbsolutePath() + "\n" +
                "FOLDER TO SAVE DependencySuite.class suite: " + new File(classesFolder).getAbsolutePath());
        final DependencyChecker checker = new DependencyChecker();
        checker.start(repositoryFolder, classesFolder);
    }

}
