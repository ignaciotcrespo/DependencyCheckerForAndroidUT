package com.crespo.ignacio.depchecker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

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
        processClassesInFolderAndSubfolders(folderToSearchClasses);

        final Set<ClassFile> classesToRun = getClassesToRun();

        DependencySuiteDump.dumpToFolder(folderToSaveGeneratedClass, classesToRun);
    }

    private Set<ClassFile> getClassesToRun() throws IOException {
        System.out.println("Classes to analyze");
        System.out.println("------------------");
        final Set<ClassFile> classesToRun = new HashSet<ClassFile>();
        final LineNumberReader lnr = new LineNumberReader(new InputStreamReader(System.in));
        String line;
        while (lnr.ready() && (line = lnr.readLine()) != null) {
            line = line.trim();
            System.out.println("Modified class: "+ line);
            if (FileUtils.isJavaFile(line)) {
                analyzeDependenciesInJavaFileIfItIsTest(classesToRun, line);
            }
        }
        System.out.println("------------------");
        return classesToRun;
    }

    private void analyzeDependenciesInJavaFileIfItIsTest(final Set<ClassFile> classesToRun, final String javaFilePath) {
        final String sourceModified = javaFilePath.substring(javaFilePath.lastIndexOf('/') + 1);
        // check all known sources, if they depend on this changed source
        final Collection<Set<ClassFile>> allClassesPerSource = mSourceToClasses.values();
        for (final Set<ClassFile> classesPerSource : allClassesPerSource) {
            analyzeDependenciesInTestSource(classesToRun, sourceModified, classesPerSource);
        }
    }

    private void analyzeDependenciesInTestSource(final Set<ClassFile> classesToRun, final String sourceModified, final Set<ClassFile> classesPerSource) {
        for (final ClassFile classFromSource : classesPerSource) {
            // only check test classes that are not abstract, and not already checked
            if (!classesToRun.contains(classFromSource) && !classFromSource.isAbstract() && ClassFileUtils.isTest(classFromSource)) {
                if (classUsesSource(classFromSource, sourceModified)) {
                    // run this test only!
                    System.out.println("   Test found: "+ classFromSource);
                    classesToRun.add(classFromSource);
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
        mSourceToClasses = new HashMapSet<String, ClassFile>();
        final Properties props = PropertiesUtils.loadProperties(new File("dependency.properties"));
        ClassFileUtils.initialize(props);
    }

    private boolean classUsesSource(final ClassFile clazz, final String source) {
        if (clazz.getSource().equals(source)) {
            // the source belongs to the same class
            return true;
        }
        return classUsesSource(clazz, source, new HashSet<ClassFile>());
    }

    private boolean classUsesSource(final ClassFile clazz, final String source, final Set<ClassFile> analyzedClasses) {
        if (clazz == null) {
            return false;
        }
        if (analyzedClasses.contains(clazz)) {
            // ignore
            return false;
        }
        boolean uses = false;
        analyzedClasses.add(clazz);
        final Set<String> usages = clazz.getUsedClasses();
        first: for (final String usedType : usages) {
            final Set<ClassFile> classesFromSource = mSourceToClasses.get(source);
            for (final ClassFile classFromSource : classesFromSource) {
                final String typeFromClass = classFromSource.getType();
                if (usedType.equals(typeFromClass)) {
                    uses = true;
                    break first;
                }
                // search children
                final ClassFile classFromUsedType = ClassFileUtils.getFromType(usedType);
                uses = classUsesSource(classFromUsedType, source, analyzedClasses);
                if (uses) {
                    break first;
                }
            }
        }
        return uses;
    }

    void addClassForSource(final String source, final ClassFile classPath) {
        if (mSourceToClasses.add(source, classPath)) {
            // do nothing
        }
    }

}
