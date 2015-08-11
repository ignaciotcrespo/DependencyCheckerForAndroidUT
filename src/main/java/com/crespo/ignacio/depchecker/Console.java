package com.crespo.ignacio.depchecker;

import com.google.common.base.Strings;

/**
 * Created by crespo on 11/08/15.
 */
public class Console {

    public static void log(String text) {
        String[] lines = text.split("\n");
        int maxLen = getMaxLen(lines);
        showLineTop(maxLen);
        for (int i = 0; i <lines.length; i++) {
            logline("| " + Strings.padEnd(lines[i], maxLen, ' ') + " |");
        }
        showLineBottom(maxLen);
    }

    private static int getMaxLen(String[] lines) {
        int maxLen = 0;
        for (int i = 0; i <lines.length; i++) {
            maxLen = Math.max(maxLen, lines[i].length());
        }
        return maxLen;
    }

    private static void logline(String line) {
        System.out.println(line);
    }

    private static void showLineTop(int len) {
        System.out.print("/-");
        for (int i = 0; i < len; i++) {
            System.out.print("-");
        }
        System.out.println("-\\");
    }

    private static void showLineBottom(int len) {
        System.out.print("\\-");
        for (int i = 0; i < len; i++) {
            System.out.print("-");
        }
        System.out.println("-/");
    }
}
