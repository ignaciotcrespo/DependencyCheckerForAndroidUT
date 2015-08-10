package com.crespo.ignacio.depchecker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class PropertiesUtils {

    static String[] getPropertyArray(final Properties props, final String name) {
        final String value = props.getProperty(name);
        if (value != null) {
            return value.split(",");
        }
        return new String[]{};
    }

    static Properties loadProperties(final File file) throws FileNotFoundException, IOException {
        final Properties props = new Properties();
        if (file.exists()) {
            props.load(new FileInputStream(file));
        }
        return props;
    }

}
