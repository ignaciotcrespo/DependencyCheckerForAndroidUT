package com.crespo.ignacio.depchecker;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.base.Optional;
import org.objectweb.asm.tree.MethodNode;

public class ClassFile {

    private final ClassNode mNode;
    Set<String> mUsedClasses = new HashSet<String>();
    private final File mClassFile;
    private boolean mIsTest;

    public ClassFile(final File file) {
        mClassFile = file;
        mNode = new ClassNode(Opcodes.ASM4);
        try {
            ClassReader cr = new ClassReader(new FileInputStream(file));
            cr.accept(mNode, 0);

            ClassFileUtils.putType(mNode.name.replace('/', '.'), this);

            checkTest();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkTest() {
        mIsTest = false;
        boolean isAbstract = (mNode.access & Opcodes.ACC_ABSTRACT) != 0;
        if (!isAbstract) {
            //check if there is a method starting with "test"
            for (Object mtd : mNode.methods) {
                MethodNode method = (MethodNode) mtd;
                boolean isPrivate = (method.access & Opcodes.ACC_PRIVATE) != 0;
                if (!isPrivate) {
                    // check annotations
                    if (method.invisibleAnnotations != null) {
                        for (Object ann : method.invisibleAnnotations) {
                            AnnotationNode annotation = (AnnotationNode) ann;
                            Set<String> types = AsmUtils.extractTypesFromDesc(annotation.desc);
                            boolean annotatedTest = types.contains("org/junit/Test");
                            if (annotatedTest) {
                                mIsTest = true;
                                break;
                            }
                        }
                    }
                    if (!mIsTest) {
                        // check annotations
                        if (method.visibleAnnotations != null) {
                            for (Object ann : method.visibleAnnotations) {
                                AnnotationNode annotation = (AnnotationNode) ann;
                                Set<String> types = AsmUtils.extractTypesFromDesc(annotation.desc);
                                boolean annotatedTest = types.contains("org/junit/Test");
                                if (annotatedTest) {
                                    mIsTest = true;
                                    break;
                                }
                            }
                        }

                    }
                    if (!mIsTest) {
                        boolean namedTest = method.name.startsWith("test");
                        if (namedTest) {
                            mIsTest = true;
                            break;
                        }
                    }
                }
            }
        }
    }

    public String getSource() {
        return mNode.sourceFile;
    }

    public boolean addUsage(final String type) {
        if(mNode.name.equals(type)){
            return false;
        }
        if (!ClassFileUtils.isIgnored(type)) {
            return mUsedClasses.add(type.replace('/', '.'));
        }
        return false;
    }

    public String getType() {
        return mNode.name.replace('/', '.');
    }

    public String getBytecodeType() {
        return mNode.name;
    }

    public Set<String> getUsedClasses() {
        return new HashSet<>(mUsedClasses);
    }

    public File getClassFile() {
        return mClassFile;
    }

    public void addUsage(final Set<String> typesFromDesc) {
        for (final String type : typesFromDesc) {
            addUsage(type);
        }
    }

    @Override
    public String toString() {
        final ToStringHelper helper = Objects.toStringHelper(getClass());
        helper.add("file", mClassFile);
        return helper.toString();
    }

    public boolean isTest() {
        return mIsTest;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClassFile classFile = (ClassFile) o;

        return mClassFile.equals(classFile.mClassFile);

    }

    @Override
    public int hashCode() {
        return mClassFile.hashCode();
    }
}
