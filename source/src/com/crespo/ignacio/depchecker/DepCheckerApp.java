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
import java.util.Properties;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

public class DepCheckerApp {

    private static HashMapSet<String, String> sUsagesClasspathToType;
    private static HashMapSet<String, String> sSourceToClasses;
    private static HashMap<String, String> sClassToSource;
    private static DoubleHashMap<String, String> sClassToType;
    private static Set<String> sIgnoredPackages = new HashSet<String>();
    private static Set<String> sTestBaseTypes = new HashSet<String>();
    private static Set<String> sAbstractClasses = new HashSet<String>();
    private static HashMap<String, String> sSuperTypeForClass = new HashMap<String, String>();

    protected static void addUsage(final String classPath, final String type) {
        if (type != null) {
            // check ignored
            for (final String ignored : sIgnoredPackages) {
                if (type.startsWith(ignored)) {
                    return;
                }
            }

            if (sUsagesClasspathToType.add(classPath, type)) {
                // do nothing
            }
        }
    }

    public static void main(final String[] args) throws IOException {
        final String paramFolderToSearchClasses = args[0];
        final String paramFolderToSaveGeneratedClass = args[1];

        final Properties props = loadProperties();

        addBaseTestClasses(props);
        addIgnoredPackages(props);
        initialize();

        processClassesInFolderAndSubfolders(paramFolderToSearchClasses);

        generateClassesToSourceCollection();

        final Set<String> classesToRun = getClassesToRun();

        final Set<String> typesToRun = getTypesToRun(classesToRun);
        showJavaSuite(typesToRun);
        saveClassSuite(typesToRun, paramFolderToSaveGeneratedClass);
    }

    private static void addBaseTestClasses(final Properties props) {
        final String[] values = getPropertyArray(props, "test.classes");
        for (final String ignored : values) {
            sTestBaseTypes.add(ignored);
        }
    }

    private static String[] getPropertyArray(final Properties props, final String name) {
        final String value = props.getProperty(name);
        final String[] values = value.split(",");
        return values;
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

    private static Set<String> getTypesToRun(final Set<String> classesToRun) {
        final Set<String> typesToRun = new HashSet<String>();
        for (final String testClass : classesToRun) {
            // avoid inner classes
            if (testClass.indexOf('$') < 0) {
                final String type = sClassToType.get(testClass);
                if (type != null) {
                    typesToRun.add(type);
                }
            }
        }
        return typesToRun;
    }

    private static Set<String> getClassesToRun() throws IOException {
        final Set<String> classesToRun = new HashSet<String>();
        final LineNumberReader lnr = new LineNumberReader(new InputStreamReader(System.in));
        String line;
        while (lnr.ready() && (line = lnr.readLine()) != null) {
            line = line.trim();
            if (isJavaFile(line)) {
                analyzeDependenciesInJavaFileIfItIsTest(classesToRun, line);
            }
        }
        return classesToRun;
    }

    private static void analyzeDependenciesInJavaFileIfItIsTest(final Set<String> classesToRun, final String filePath) {
        final String sourceModified = filePath.substring(filePath.lastIndexOf('/') + 1);
        // check all known sources, if they depend on this changed source
        final Set<String> sources = sSourceToClasses.keySet();
        for (final String source : sources) {
            analyzeDependenciesInTestSource(classesToRun, sourceModified, source);
        }
    }

    private static boolean isTest(final String clazz) {
        return isTest(clazz, new HashSet<String>());
    }

    private static boolean isTest(final String clazz, final Set<String> checkedClasses) {
        if (clazz == null) {
            return false;
        }
        checkedClasses.add(clazz);
        boolean isSuperATest = false;
        if (sSuperTypeForClass.containsKey(clazz)) {
            final String superType = sSuperTypeForClass.get(clazz);
            for (final String testBaseType : sTestBaseTypes) {
                if (superType.replace('/', '.').equals(testBaseType)) {
                    isSuperATest = true;
                    break;
                }
            }
            if (!isSuperATest) {
                final String superClass = sClassToType.getKeyFromValue(superType);
                if (superClass != null) {
                    if (!checkedClasses.contains(superClass)) {
                        isSuperATest = isTest(superClass, checkedClasses);
                    }
                }
            }
        }
        return isSuperATest;
    }

    private static void analyzeDependenciesInTestSource(final Set<String> classesToRun, final String sourceModified, final String source) {
        final Set<String> classesFromSource = sSourceToClasses.get(source);
        for (final String classFromSource : classesFromSource) {
            // only check test classes that are not abstract, and not already checked
            if (!classesToRun.contains(classFromSource) && !isAbstract(classFromSource) && isTest(classFromSource)) {
                if (classUsesSource(classFromSource, sourceModified)) {
                    // run this test only!
                    classesToRun.add(classFromSource);
                    break;
                }
            }
        }
    }

    private static boolean isAbstract(final String clazz) {
        return sAbstractClasses.contains(clazz);
    }

    private static boolean isJavaFile(final String filePath) {
        return filePath.endsWith(".java") && filePath.indexOf('/') > 0;
    }

    private static void generateClassesToSourceCollection() {
        for (final String source : sSourceToClasses.keySet()) {
            final Set<String> classesFromSource = sSourceToClasses.get(source);
            for (final String clazzFromSource : classesFromSource) {
                sClassToSource.put(clazzFromSource, source);
            }
        }
    }

    private static void processClassesInFolderAndSubfolders(final String folderPath) throws FileNotFoundException, IOException {
        final List<File> classes = FileUtils.findClassesInFolder(folderPath);
        for (final File classFile : classes) {
            final String classPath = classFile.getAbsolutePath();
            visitClass(classPath);
        }
    }

    private static void initialize() {
        sSourceToClasses = new HashMapSet<String, String>();
        sClassToSource = new HashMap<String, String>();
        sUsagesClasspathToType = new HashMapSet<String, String>();
        sClassToType = new DoubleHashMap<String, String>();
    }

    private static void addIgnoredPackages(final Properties props) {
        final String[] values = getPropertyArray(props, "ignored.packages");
        for (final String ignored : values) {
            if (ignored.endsWith("/")) {
                sTestBaseTypes.add(ignored);
            } else {
                sTestBaseTypes.add(ignored + "/");
            }
        }
    }

    public static boolean classUsesSource(final String clazz, final String source) {
        if (sClassToSource.containsKey(clazz)) {
            if (sClassToSource.get(clazz).equals(source)) {
                // the source belongs to the same class
                return true;
            }
        }
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
        first: for (final String usedType : usages) {
            final Set<String> classesFromSource = sSourceToClasses.get(source);
            for (final String classFromSource : classesFromSource) {
                final String typeFromClass = sClassToType.get(classFromSource);
                if (usedType.equals(typeFromClass)) {
                    uses = true;
                    break first;
                }
                // search children
                final String classFromUsedType = sClassToType.getKeyFromValue(usedType);
                uses = classUsesSource(classFromUsedType, source, analyzedClasses);
                if (uses) {
                    break first;
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
    }

    public static void addClassForSource(final String source, final String classPath) {
        if (sSourceToClasses.add(source, classPath)) {
            // do nothing
        }
    }

    public static void addAbstractClass(final String classPath) {
        sAbstractClasses.add(classPath);
    }

    public static void addSuperClass(final String classPath, final String superName) {
        sSuperTypeForClass.put(classPath, superName);
    }

    private static Properties loadProperties() throws FileNotFoundException, IOException {
        final Properties props = new Properties();
        props.load(new FileInputStream("dependency.properties"));
        return props;
    }

}
