package com.crespo.ignacio.depchecker;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

final class DependencyMethodVisitor extends MethodVisitor {
    private final ClassFile mClassPath;

    DependencyMethodVisitor(final ClassFile classPath, final int asmVersion) {
        super(asmVersion);
        mClassPath = classPath;
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        mClassPath.addUsage(AsmUtils.extractTypesFromDesc(desc));
        return super.visitAnnotation(desc, visible);
    }

    @Override
    public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc) {
        super.visitFieldInsn(opcode, owner, name, desc);
        mClassPath.addUsage(owner);
        mClassPath.addUsage(AsmUtils.extractTypesFromDesc(desc));
    }

    @Override
    public void visitLocalVariable(final String name, final String desc, final String signature, final Label start, final Label end, final int index) {
        super.visitLocalVariable(name, desc, signature, start, end, index);
        mClassPath.addUsage(AsmUtils.extractTypesFromDesc(desc));
        mClassPath.addUsage(AsmUtils.extractTypesFromDesc(signature));
    }

    @Override
    public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc) {
        super.visitMethodInsn(opcode, owner, name, desc);
        mClassPath.addUsage(owner);
        mClassPath.addUsage(AsmUtils.extractTypesFromDesc(desc));
    }

    @Override
    public void visitMultiANewArrayInsn(final String desc, final int dims) {
        super.visitMultiANewArrayInsn(desc, dims);
        mClassPath.addUsage(AsmUtils.extractTypesFromDesc(desc));
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(final int parameter, final String desc, final boolean visible) {
        mClassPath.addUsage(AsmUtils.extractTypesFromDesc(desc));
        return super.visitParameterAnnotation(parameter, desc, visible);
    }

    @Override
    public void visitTryCatchBlock(final Label start, final Label end, final Label handler, final String type) {
        super.visitTryCatchBlock(start, end, handler, type);
        mClassPath.addUsage(type);
    }

    @Override
    public void visitTypeInsn(final int opcode, final String type) {
        super.visitTypeInsn(opcode, type);
        mClassPath.addUsage(type);
    }

}