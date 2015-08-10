package com.crespo.ignacio.depchecker;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import com.google.common.base.Optional;

public class ClassFileUtils {

    private static Set<String> sTestBaseTypes = new HashSet<String>();

    private static Set<String> sIgnoredPackages = new HashSet<String>();

    private static HashMap<String, ClassFile> sTypeToClass = new HashMap<String, ClassFile>();

    private static void addBaseTestClasses(final Properties props) {
        final String[] values = PropertiesUtils.getPropertyArray(props, "test.classes");
        for (final String ignored : values) {
            sTestBaseTypes.add(ignored);
        }
    }

    private static void addIgnoredPackages(final Properties props) {
        final String[] values = PropertiesUtils.getPropertyArray(props, "ignored.packages");
        for (final String ignored : values) {
            ClassFileUtils.sIgnoredPackages.add(ignored);
        }
    }

    static boolean isIgnored(final String type) {
        boolean ignore = false;
        if (type == null) {
            ignore = true;
        } else {
            for (final String ignored : ClassFileUtils.sIgnoredPackages) {
                if (type.startsWith(ignored)) {
                    ignore = true;
                    break;
                }
            }
        }
        return ignore;
    }

    static boolean isTest(final ClassFile clazz) {
        final boolean is = ClassFileUtils.isTest(clazz, new HashSet<ClassFile>());
        return is;
    }

    static boolean isTest(final ClassFile clazz, final Set<ClassFile> checkedClasses) {
        if (clazz == null) {
            return false;
        }
        checkedClasses.add(clazz);
        boolean isSuperATest = false;
        final Optional<String> superType = clazz.getSuperType();
        if (superType.isPresent()) {
            for (final String testBaseType : sTestBaseTypes) {
                if (superType.get().equals(testBaseType)) {
                    isSuperATest = true;
                    break;
                }
            }
            if (!isSuperATest) {
                final ClassFile superClass = ClassFileUtils.sTypeToClass.get(superType.get());
                if (superClass != null) {
                    if (!checkedClasses.contains(superClass)) {
                        isSuperATest = isTest(superClass, checkedClasses);
                    }
                }
            }
        }
        return isSuperATest;
    }

    public static void putType(final String type, final ClassFile classFile) {
        sTypeToClass.put(type, classFile);
    }

    public static ClassFile getFromType(final String type) {
        return sTypeToClass.get(type);
    }

    static void initialize(final Properties props) {
        addBaseTestClasses(props);
        addIgnoredPackages(props);
    }

}
