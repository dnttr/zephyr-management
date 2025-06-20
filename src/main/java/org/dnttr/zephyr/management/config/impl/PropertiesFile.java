package org.dnttr.zephyr.management.config.impl;

import lombok.Cleanup;
import org.dnttr.zephyr.management.config.IFile;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author dnttr
 */

public final class PropertiesFile implements IFile<Properties> {

    @Override
    public Properties load(@NotNull String path) throws IOException {
        @Cleanup InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);

        if (is == null) {
            throw new FileNotFoundException("File " + path + " not found");
        }

        Properties properties = new Properties();
        properties.load(is);

        return properties;
    }
}
