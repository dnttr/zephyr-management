package org.dnttr.zephyr.management.config.impl;

import lombok.Cleanup;
import org.dnttr.zephyr.management.config.IFile;
import org.jetbrains.annotations.NotNull;

import java.io.*;

public final class FontFile implements IFile<byte[]> {

    @Override
    public byte[] load(@NotNull String path) throws IOException {
        @Cleanup InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);

        if (is == null) {
            throw new FileNotFoundException("File " + path + " not found");
        }

        return is.readAllBytes();
    }
}