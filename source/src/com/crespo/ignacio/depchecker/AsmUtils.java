package com.crespo.ignacio.depchecker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

public class AsmUtils {

    static Set<String> extractTypesFromDesc(final String signature) {
        final Set<String> types = new HashSet<String>();
        if (!StringUtils.isNullOrEmpty(signature)) {
            boolean typeStarted = false;
            StringBuffer sbType = new StringBuffer();
            for (int i = 0; i < signature.length(); i++) {
                final char letter = signature.charAt(i);
                if (letter == 'L' && !typeStarted) {
                    typeStarted = true;
                    continue;
                }
                if (typeStarted && letter == ';' || letter == '<') {
                    if (sbType.length() > 0) {
                        types.add(sbType.toString());
                    }
                    sbType = new StringBuffer();
                    typeStarted = false;
                    continue;
                }
                if (typeStarted) {
                    if (letter != '<') {
                        sbType.append(letter);
                    }
                }
            }
        }
        return types;
    }

    static ClassNode getClassNodeFromFile(final File file) throws FileNotFoundException, IOException {
        final ClassNode node = new ClassNode(Opcodes.ASM4);
        final ClassReader cr = new ClassReader(new FileInputStream(file));
        // dont parse code, just read structure
        cr.accept(node, ClassReader.SKIP_CODE);
        return node;
    }

}
