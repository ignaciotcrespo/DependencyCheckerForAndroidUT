package com.crespo.ignacio.depchecker;

import java.util.Set;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class DependencySuiteDump implements Opcodes {

    public static byte[] dump(final Set<String> testTypes) throws Exception {

        final ClassWriter cw = new ClassWriter(0);
        MethodVisitor mv;

        cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, "DependencySuite", null, "junit/framework/TestSuite", null);

        cw.visitSource("DependencySuite.java", null);

        {
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            final Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "junit/framework/TestSuite", "<init>", "()V");
            mv.visitInsn(RETURN);
            final Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitLocalVariable("this", "LDependencySuite;", null, l0, l1, 0);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "suite", "()Ljunit/framework/Test;", null, null);
            mv.visitCode();
            final Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitTypeInsn(NEW, "junit/framework/TestSuite");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "junit/framework/TestSuite", "<init>", "()V");
            mv.visitVarInsn(ASTORE, 0);
            final Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESTATIC, "DependencySuite", "addTests", "(Ljunit/framework/TestSuite;)V");
            final Label l2 = new Label();
            mv.visitLabel(l2);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitInsn(ARETURN);
            final Label l3 = new Label();
            mv.visitLabel(l3);
            mv.visitLocalVariable("suite", "Ljunit/framework/TestSuite;", null, l1, l3, 0);
            mv.visitMaxs(2, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PRIVATE + ACC_STATIC, "addTests", "(Ljunit/framework/TestSuite;)V", null, null);
            mv.visitCode();
            if (testTypes.size() > 0) {
                final Label firstLabel = new Label();
                int c = 0;
                for (final String type : testTypes) {
                    if (c == 0) {
                        mv.visitLabel(firstLabel);
                    } else {
                        mv.visitLabel(new Label());
                    }
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitLdcInsn(Type.getType("L" + type + ";"));
                    mv.visitMethodInsn(INVOKEVIRTUAL, "junit/framework/TestSuite", "addTestSuite", "(Ljava/lang/Class;)V");
                    c++;
                }
                final Label l3 = new Label();
                mv.visitLabel(l3);
                mv.visitInsn(RETURN);
                final Label l4 = new Label();
                mv.visitLabel(l4);
                mv.visitLocalVariable("suite", "Ljunit/framework/TestSuite;", null, firstLabel, l4, 0);
            } else {
                final Label l3 = new Label();
                mv.visitLabel(l3);
                mv.visitInsn(RETURN);
                final Label l4 = new Label();
                mv.visitLabel(l4);
                mv.visitLocalVariable("suite", "Ljunit/framework/TestSuite;", null, l3, l4, 0);
            }
            mv.visitMaxs(2, 1);
            mv.visitEnd();
        }
        cw.visitEnd();

        return cw.toByteArray();
    }
}
