package com.crespo.ignacio.depchecker;

import java.io.IOException;
import java.io.OutputStream;

public class StreamUtils {

    public static void close(final OutputStream os) {
        if (os != null) {
            try {
                os.close();
            } catch (final IOException exc) {
                exc.printStackTrace();
            }
        }
    }

}
