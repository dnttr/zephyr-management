package org.dnttr.zephyr.management.config.impl;

import lombok.Cleanup;
import org.dnttr.zephyr.management.config.IFile;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author dnttr
 */

public final class StringFile implements IFile<String> {

    @Override
    public String load(@NotNull String path) throws IOException {
        @Cleanup InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);

        if (is == null) {
            throw new FileNotFoundException("File " + path + " not found");
        }

        byte[] bytes = is.readAllBytes();

        return new String(bytes, StandardCharsets.UTF_8);
    }
}
