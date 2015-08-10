package com.crespo.ignacio.depchecker;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.InstructionAdapter;

final class DependencyClassVisitor extends ClassVisitor {

    final ClassFile mClassPath;
    private final DependencyChecker mChecker;

    DependencyClassVisitor(final int arg0, final ClassFile classPath, final DependencyChecker checker) {
        super(arg0);
        mClassPath = classPath;
        mChecker = checker;
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature, final String superName,
            final String[] interfaces) {
        mClassPath.setAccess(access);
        mClassPath.setType(name);
        mClassPath.addUsage(AsmUtils.extractTypesFromDesc(signature));
        mClassPath.addUsage(superName);
        mClassPath.setSuperType(superName);
        if (interfaces != null) {
            for (final String interf : interfaces) {
                mClassPath.addUsage(interf);
            }
        }
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        mClassPath.addUsage(AsmUtils.extractTypesFromDesc(desc));
        return super.visitAnnotation(desc, visible);
    }

    @Override
    public FieldVisitor visitField(final int access, final String name, final String desc, final String signature, final Object value) {
        mClassPath.addUsage(AsmUtils.extractTypesFromDesc(desc));
        mClassPath.addUsage(AsmUtils.extractTypesFromDesc(signature));
        return super.visitField(access, name, desc, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        mClassPath.addUsage(AsmUtils.extractTypesFromDesc(desc));
        mClassPath.addUsage(AsmUtils.extractTypesFromDesc(signature));
        if (exceptions != null) {
            for (final String exc : exceptions) {
                mClassPath.addUsage(exc);
            }
        }
        final MethodVisitor oriMv = new DependencyMethodVisitor(mClassPath, Opcodes.ASM4);
        // An instructionAdapter is a special MethodVisitor that
        // lets us process instructions easily
        final InstructionAdapter instMv = new DependencyMethodInstructionsAdapter(oriMv);
        return instMv;
    }

    @Override
    public void visitSource(final String fileName, final String arg1) {
        mChecker.addClassForSource(fileName, mClassPath);
        mClassPath.setSource(fileName);
        super.visitSource(fileName, arg1);
    }

    private static final class DependencyMethodInstructionsAdapter extends InstructionAdapter {
        private DependencyMethodInstructionsAdapter(final MethodVisitor mv) {
            super(mv);
        }

        @Override
        public void visitInsn(final int opcode) {
            super.visitInsn(opcode);
        }
    }
}