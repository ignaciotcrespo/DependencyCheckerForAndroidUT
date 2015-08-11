package com.crespo.ignacio.depchecker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.*;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

public class DependencyChecker {

    private HashMapSet<String, ClassFile> mSourceToClasses;

    void start(final String folderToSearchClasses, final String folderToSaveGeneratedClass) throws FileNotFoundException, IOException {
        initialize();
        process(folderToSearchClasses, folderToSaveGeneratedClass);
    }

    private void process(final String folderToSearchClasses, final String folderToSaveGeneratedClass) throws FileNotFoundException, IOException {
        final Set<String> lines = parseInputWithListOfSources();

        processClassesInFolderAndSubfolders(folderToSearchClasses);

        final Set<ClassFile> classesToRun = getClassesToRun(lines);

        if (classesToRun.isEmpty()) {
            Console.log("No tests found to run, exiting with 2");
            System.exit(2);
        }

        DependencySuiteDump.dumpToFolder(folderToSaveGeneratedClass, classesToRun);
    }

    private Set<ClassFile> getClassesToRun(Set<String> lines) throws IOException {
        final Set<ClassFile> classesToRun = new HashSet<>();
        for (String singleLine : lines) {
            Console.log("Modified class: " + singleLine);
            if (FileUtils.isJavaFile(singleLine)) {
                analyzeDependenciesInJavaFileIfItIsTest(classesToRun, singleLine);
            }
        }
        return classesToRun;
    }

    private Set<String> parseInputWithListOfSources() throws IOException {
        Console.log("Analyzing classes, searching for tests...");
        final Set<String> lines = new HashSet<>();
        final LineNumberReader lnr = new LineNumberReader(new InputStreamReader(System.in));
        String line;
        while (lnr.ready() && (line = lnr.readLine()) != null) {
            line = line.trim();
            if (!line.isEmpty() && line.endsWith(".java")) {
                lines.add(line);
            }
        }
        if (lines.isEmpty()) {
            Console.log("Not .java files modified, exiting with 1");
            System.exit(1);
        }
        return lines;
    }

    private void analyzeDependenciesInJavaFileIfItIsTest(final Set<ClassFile> classesToRun, final String javaFilePath) {
        previousTree.clear();
        final String sourceModified = javaFilePath.substring(javaFilePath.lastIndexOf('/') + 1);
        // check all known sources, if they depend on this changed source
        final Collection<Set<ClassFile>> allClassesPerSource = mSourceToClasses.values();
        for (final Set<ClassFile> classesPerSource : allClassesPerSource) {
            analyzeDependenciesInTestSource(classesToRun, sourceModified, classesPerSource);
        }
    }

    ArrayList<String> previousTree = new ArrayList<>();

    private void analyzeDependenciesInTestSource(final Set<ClassFile> classesToRun, final String sourceModified, final Set<ClassFile> classesPerSource) {
        for (final ClassFile classFromSource : classesPerSource) {
            // only check test classes that are not abstract, and not already checked
            if (classFromSource.isTest()) {
                ArrayList<String> tree = new ArrayList<>();
                if (classUsesSource(classFromSource, sourceModified, tree)) {


                    // run this test only!
                    if (!classesToRun.contains(classFromSource)) {
                        classesToRun.add(classFromSource);


                        for (int i = 0; i < tree.size(); i++) {
                            String previous = previousTree.size() > i ? previousTree.get(i) : "";
                            if (!previous.equals(tree.get(i))) {
                                System.out.print("     ");
                                for (int j = 0; j < i; j++) {
                                    System.out.print("|    ");
                                }
                                if(i == tree.size()-1){
                                    System.out.print("^-------[ ");
                                } else {
                                    System.out.print("^----* ");
                                }
                                System.out.print(tree.get(i));
                                if(i == tree.size()-1){
                                    System.out.println(" ]-------");
                                } else {
                                    System.out.println("");
                                }
                            }
                        }
                        System.out.print("     ");
                        for (int i = 0; i < tree.size(); i++) {
                            System.out.print("|    ");
                        }
                        System.out.println("");

                        previousTree.clear();
                        previousTree.addAll(tree);


//                        System.out.print("   [*] ");
                    } else {
//                        System.out.print("   [ ] ");
                    }
//                    System.out.println("Test found: " + classFromSource);
                    break;
                }
            }
        }
    }

    private void processClassesInFolderAndSubfolders(final String folderPath) throws FileNotFoundException, IOException {
        final List<File> classes = FileUtils.findClassesInFolder(folderPath);
        for (final File file : classes) {
            final ClassFile classFile = new ClassFile(file);
            visitClass(classFile);
        }
    }

    void visitClass(final ClassFile classPath) throws FileNotFoundException, IOException {
        final ClassVisitor visitor = new DependencyClassVisitor(Opcodes.ASM4, classPath, this);
        final InputStream in = new FileInputStream(classPath.getClassFile().getAbsolutePath());
        final ClassReader classReader = new ClassReader(in);
        classReader.accept(visitor, 0);
    }

    private void initialize() throws FileNotFoundException, IOException {
        mSourceToClasses = new HashMapSet<>();
        File file = new File("dependency.properties");
        if (!file.exists()) {
            Console.log("dependency.properties not found! Using default");
        }
        final Properties props = PropertiesUtils.loadProperties(file);
        ClassFileUtils.initialize(props);
    }

    private boolean classUsesSource(final ClassFile clazz, final String source, ArrayList<String> tree) {
        if (clazz.getSource().equals(source)) {
            // the source belongs to the same class
            return true;
        }
        return classUsesSource(clazz, source, new HashSet<ClassFile>(), tree);
    }

    private boolean classUsesSource(final ClassFile clazz, final String source, final Set<ClassFile> analyzedClasses, List<String> tree) {
        if (clazz == null) {
            return false;
        }
        if (analyzedClasses.contains(clazz)) {
            // ignore
            return false;
        }
        boolean uses = false;
        analyzedClasses.add(clazz);
        final Set<ClassFile> classesFromSource = mSourceToClasses.get(source);
        for (final ClassFile classFromSource : classesFromSource) {
            final String typeFromClass = classFromSource.getType();
            if (clazz.getType().equals(typeFromClass)) {
                uses = true;
                break;
            }
        }
        if (!uses) {
            final Set<String> usages = clazz.getUsedClasses();
            first:
            for (final String usedType : usages) {
                for (final ClassFile classFromSource : classesFromSource) {
                    final String typeFromClass = classFromSource.getType();
                    if (usedType.equals(typeFromClass)) {
                        uses = true;
                        break first;
                    }
                    // search children
                    final ClassFile classFromUsedType = ClassFileUtils.getFromType(usedType);
                    uses = classUsesSource(classFromUsedType, source, analyzedClasses, tree);
                    if (uses) {
                        //System.out.println(classFromUsedType);
                        break first;
                    }
                }
            }
        }
        if (uses) {
            tree.add(clazz.getType());
        }
        return uses;
    }

    void addClassForSource(final String source, final ClassFile classPath) {
        if (mSourceToClasses.add(source, classPath)) {
            // do nothing
        }
    }

}
