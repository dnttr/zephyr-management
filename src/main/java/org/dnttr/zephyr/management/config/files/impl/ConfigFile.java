package org.dnttr.zephyr.management.config.files.impl;

import lombok.Cleanup;
import org.dnttr.zephyr.management.config.FileLoader;
import org.dnttr.zephyr.management.config.files.IFile;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author dnttr
 */

public final class ConfigFile implements IFile<Properties> {

    @Override
    public Properties load(@NotNull String path) throws IOException {
        @Cleanup InputStream inputStream = FileLoader.class.getClassLoader().getResourceAsStream(path);

        if (inputStream == null) {
            throw new FileNotFoundException("Property file '" + path + "' not found in the classpath");
        }

        Properties properties = new Properties();
        properties.load(inputStream);

        return properties;
    }
}
