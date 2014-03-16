package com.crespo.ignacio.depchecker;

import java.util.HashSet;
import java.util.Set;

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

}
