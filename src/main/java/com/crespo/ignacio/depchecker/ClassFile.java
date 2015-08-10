package com.crespo.ignacio.depchecker;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.base.Optional;

public class ClassFile {

    private final ClassNode mNode;
    Set<String> mUsedClasses = new HashSet<String>();
    private final File mClassFile;

    public ClassFile(final File file) {
        mClassFile = file;
        mNode = new ClassNode(Opcodes.ASM4);
    }

    public boolean isAbstract() {
        return (mNode.access & Opcodes.ACC_ABSTRACT) != 0;
    }

    public String getSource() {
        return mNode.sourceFile;
    }

    public Optional<String> getSuperType() {
        return Optional.fromNullable(mNode.superName);
    }

    public boolean addUsage(final String type) {
        if (!ClassFileUtils.isIgnored(type)) {
            return mUsedClasses.add(type.replace('/', '.'));
        }
        return false;
    }

    public String getType() {
        return mNode.name;
    }

    public String getBytecodeType() {
        return mNode.name.replace('.', '/');
    }

    public Set<String> getUsedClasses() {
        return new HashSet<String>(mUsedClasses);
    }

    public void setAccess(final int access) {
        mNode.access = access;
    }

    public File getClassFile() {
        return mClassFile;
    }

    public void setType(final String type) {
        mNode.name = type.replace('/', '.');
        ClassFileUtils.putType(mNode.name, this);
    }

    public void addUsage(final Set<String> typesFromDesc) {
        for (final String type : typesFromDesc) {
            addUsage(type);
        }
    }

    public void setSuperType(final String type) {
        // dont ignore here
        if (type != null) {
            mNode.superName = type.replace('/', '.');
        }
    }

    public void setSource(final String source) {
        mNode.sourceFile = source;
    }

    @Override
    public String toString() {
        final ToStringHelper helper = Objects.toStringHelper(getClass());
        helper.add("file", mClassFile);
        return helper.toString();
    }
}
