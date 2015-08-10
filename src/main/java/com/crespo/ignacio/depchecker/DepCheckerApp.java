package com.crespo.ignacio.depchecker;

import java.io.File;
import java.io.IOException;

public class DepCheckerApp {

    // TODO
    // detect test classes when:
    // - extends junit testcase
    // - or has at least one method startin with "test"
    // - or has at least one method with junit annotation @Test
    // - not abstract


    public static void main(final String[] args) throws IOException {
        String repositoryFolder = args.length > 0 ? args[0] : ".";
        String classesFolder = args.length > 1 ? args[1] : ".";
        System.out.println("---------------------------------------");
        System.out.println("REPOSITORY FOLDER TO READ MODIFIED .JAVA FILES: " + new File(repositoryFolder).getAbsolutePath());
        System.out.println("FOLDER TO SAVE DependencySuite.class suite: " + new File(classesFolder).getAbsolutePath());
        System.out.println("---------------------------------------");
        final DependencyChecker checker = new DependencyChecker();
        checker.start(repositoryFolder, classesFolder);
    }

}
