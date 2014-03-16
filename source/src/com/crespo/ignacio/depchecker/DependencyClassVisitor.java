package com.crespo.ignacio.depchecker;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.InstructionAdapter;

final class DependencyClassVisitor extends ClassVisitor {

    final String mClassPath;

    DependencyClassVisitor(final int arg0, final String classPath) {
        super(arg0);
        mClassPath = classPath;
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature, final String superName,
            final String[] interfaces) {
        DepCheckerApp.setClassType(mClassPath, name);
        DepCheckerApp.addUsage(mClassPath, AsmUtils.extractTypesFromDesc(signature));
        DepCheckerApp.addUsage(mClassPath, superName);
        if (interfaces != null) {
            for (final String interf : interfaces) {
                DepCheckerApp.addUsage(mClassPath, interf);
            }
        }
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        DepCheckerApp.addUsage(mClassPath, AsmUtils.extractTypesFromDesc(desc));
        return super.visitAnnotation(desc, visible);
    }

    @Override
    public FieldVisitor visitField(final int access, final String name, final String desc, final String signature, final Object value) {
        DepCheckerApp.addUsage(mClassPath, AsmUtils.extractTypesFromDesc(desc));
        DepCheckerApp.addUsage(mClassPath, AsmUtils.extractTypesFromDesc(signature));
        return super.visitField(access, name, desc, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        DepCheckerApp.addUsage(mClassPath, AsmUtils.extractTypesFromDesc(desc));
        DepCheckerApp.addUsage(mClassPath, AsmUtils.extractTypesFromDesc(signature));
        if (exceptions != null) {
            for (final String exc : exceptions) {
                DepCheckerApp.addUsage(mClassPath, exc);
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
        DepCheckerApp.addClassForSource(fileName, mClassPath);
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