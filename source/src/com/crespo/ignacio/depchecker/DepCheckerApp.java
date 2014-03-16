package com.crespo.ignacio.depchecker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

public class DepCheckerApp {

    private static HashMap<String, Set<String>> sUsagesClasspathToType;
    private static HashMap<String, Set<String>> sUsedByTyeToClasspath;
    private static HashMap<String, Set<String>> sSourceToClasses;
    private static HashMap<String, String> sClassesToSource;
    private static HashMap<String, String> sClassToType;
    private static HashMap<String, String> sTypeToClass;
    private static Set<String> sIgnoredPackages = new HashSet<String>();

    protected static void addUsage(final String classPath, final String type) {
        if (type != null) {
            // check ignored
            for (final String ignored : sIgnoredPackages) {
                if (type.startsWith(ignored)) {
                    return;
                }
            }

            if (!sUsagesClasspathToType.containsKey(classPath)) {
                sUsagesClasspathToType.put(classPath, new HashSet<String>());
            }
            if (sUsagesClasspathToType.get(classPath).add(type)) {
                // add used by
                if (!sUsedByTyeToClasspath.containsKey(type)) {
                    sUsedByTyeToClasspath.put(type, new HashSet<String>());
                }
                sUsedByTyeToClasspath.get(type).add(classPath);
            }
        }
    }

    public static void main(final String[] args) throws IOException {

        addIgnoredPackages();
        initialize();

        processClassesinFolder(args[0]);

        generateClassesToSourceCollection();

        final Set<String> sourcesToRun = getSourcesToRun();

        if (sourcesToRun.size() > 0) {
            final Set<String> typesToRun = getTypesToRun(sourcesToRun);
            showJavaSuite(typesToRun);
            saveClassSuite(typesToRun, args[1]);
        } else {
            System.out.println("No UT to run!");
        }
    }

    private static void saveClassSuite(final Set<String> typesToRun, final String folder) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(new File(folder + File.separator + "DependencySuite.class"));
            fos.write(DependencySuiteDump.dump(typesToRun));
            System.out.println("");
            System.out.println("File DependencySuite.class successfully created");
        } catch (final Exception exc) {
            exc.printStackTrace();
        } finally {
            StreamUtils.close(fos);
        }
    }

    private static void showJavaSuite(final Set<String> typesToRun) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("public class DependencySuite extends junit.framework.TestSuite {\n");
        stringBuilder.append("\n");
        stringBuilder.append("    public static junit.framework.Test suite() {\n");
        stringBuilder.append("        final junit.framework.TestSuite suite = new junit.framework.TestSuite();\n");
        for (final String typeToRun : typesToRun) {
            stringBuilder.append("        suite.addTestSuite(" + typeToRun.replace('/', '.') + ".class);\n");
        }
        stringBuilder.append("        return suite;\n");
        stringBuilder.append("    }\n");
        stringBuilder.append("\n");
        stringBuilder.append("}");
        System.out.println(stringBuilder.toString());
    }

    private static Set<String> getTypesToRun(final Set<String> sourcesToRun) {
        final Set<String> typesToRun = new HashSet<String>();
        for (final String source : sourcesToRun) {
            final Set<String> testClasses = sSourceToClasses.get(source);
            for (final String testClass : testClasses) {
                // avoid inner classes
                if (testClass.indexOf('$') < 0) {
                    typesToRun.add(sClassToType.get(testClass));
                }
            }
        }
        return typesToRun;
    }

    private static Set<String> getSourcesToRun() throws IOException {
        final Set<String> sourcesToRun = new HashSet<String>();
        final LineNumberReader lnr = new LineNumberReader(new InputStreamReader(System.in));
        String line;
        while (lnr.ready() && (line = lnr.readLine()) != null) {
            line = line.trim();
            if (line.endsWith(".java") && line.indexOf('/') > 0) {
                final String nameJavaToCheck = line.substring(line.lastIndexOf('/') + 1);
                final Set<String> sources = sSourceToClasses.keySet();
                for (final String source : sources) {
                    if (source.endsWith("Test.java") && !sourcesToRun.contains(source)) {
                        // search usage inside the test
                        final Set<String> classesFromSource = sSourceToClasses.get(source);
                        for (final String classFromSource : classesFromSource) {
                            final boolean uses = classUsesSource(classFromSource, nameJavaToCheck);
                            if (uses) {
                                // run this test only!
                                sourcesToRun.add(source);
                                break;
                            }
                        }
                    }
                }
            }
        }
        return sourcesToRun;
    }

    private static void generateClassesToSourceCollection() {
        for (final String source : sSourceToClasses.keySet()) {
            final Set<String> classesFromSource = sSourceToClasses.get(source);
            for (final String clazzFromSource : classesFromSource) {
                sClassesToSource.put(clazzFromSource, source);
            }
        }
    }

    private static void processClassesinFolder(final String folderPath) throws FileNotFoundException, IOException {
        final List<File> classes = FileUtils.findClassesInFolder(folderPath);
        for (final File classFile : classes) {
            final String classPath = classFile.getAbsolutePath();
            visitClass(classPath);
        }
    }

    private static void initialize() {
        sSourceToClasses = new HashMap<String, Set<String>>();
        sClassesToSource = new HashMap<String, String>();
        sUsagesClasspathToType = new HashMap<String, Set<String>>();
        sUsedByTyeToClasspath = new HashMap<String, Set<String>>();
        sClassToType = new HashMap<String, String>();
        sTypeToClass = new HashMap<String, String>();
    }

    private static void addIgnoredPackages() {
        sIgnoredPackages.add("java/");
        sIgnoredPackages.add("android/");
        sIgnoredPackages.add("com/");
        sIgnoredPackages.add("org/");
    }

    public static boolean classUsesSource(final String clazz, final String source) {
        return classUsesSource(clazz, source, new HashSet<String>());
    }

    public static boolean classUsesSource(final String clazz, final String source, final Set<String> analyzedClasses) {
        boolean uses = false;
        if (analyzedClasses.contains(clazz)) {
            // ignore
            return false;
        }
        analyzedClasses.add(clazz);
        final Set<String> usages = sUsagesClasspathToType.get(clazz);
        if (usages != null) {
            first: for (final String usedType : usages) {
                final Set<String> classesFromSource = sSourceToClasses.get(source);
                if (classesFromSource != null) {
                    for (final String classFromSource : classesFromSource) {
                        final String typeFromClass = sClassToType.get(classFromSource);
                        if (usedType.equals(typeFromClass)) {
                            uses = true;
                            break first;
                        } else {
                            // search children
                            final String classFromUsedType = sTypeToClass.get(usedType);
                            uses = classUsesSource(classFromUsedType, source, analyzedClasses);
                            if (uses) {
                                // System.out.println("<- " + usedType);
                                break;
                            }
                        }
                    }
                }
            }
        }
        return uses;
    }

    private static void visitClass(final String classPath) throws FileNotFoundException, IOException {
        final ClassVisitor visitor = new DependencyClassVisitor(Opcodes.ASM4, classPath);
        final InputStream in = new FileInputStream(classPath);
        final ClassReader classReader = new ClassReader(in);
        classReader.accept(visitor, 0);
    }

    public static void addUsage(final String classPath, final Set<String> set) {
        for (final String type : set) {
            addUsage(classPath, type);
        }
    }

    public static void setClassType(final String classPath, final String type) {
        sClassToType.put(classPath, type);
        sTypeToClass.put(type, classPath);
    }

    public static void addClassForSource(final String source, final String classPath) {
        if (!sSourceToClasses.containsKey(source)) {
            sSourceToClasses.put(source, new HashSet<String>());
        }
        if (sSourceToClasses.get(source).add(classPath)) {
            // do nothing
        }
    }
}
