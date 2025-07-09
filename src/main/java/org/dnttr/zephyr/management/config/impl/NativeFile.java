package org.dnttr.zephyr.management.config.impl;

import lombok.Cleanup;
import org.dnttr.zephyr.management.config.IFile;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * @author dnttr
 */
public class NativeFile implements IFile<String> {

    @Override
    public String load(@NotNull String resourcePath) throws IOException {
        Path jarDir;
        try {
            String runningJarPath = NativeFile.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            String decodedPath = URLDecoder.decode(runningJarPath, StandardCharsets.UTF_8);

            jarDir = Paths.get(decodedPath).getParent();
        } catch (Exception e) {
            jarDir = Paths.get(".");
        }

        Path dependenciesDir = jarDir.resolve("dependencies");

        @Cleanup InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);

        if (is == null) {
            throw new IOException("File " + resourcePath + " not found in JAR resources");
        }

        if (!Files.exists(dependenciesDir)) {
            Files.createDirectories(dependenciesDir);
        }

        String fileName = Paths.get(resourcePath).getFileName().toString();
        Path outputPath = dependenciesDir.resolve(fileName);

        if (!Files.exists(outputPath)) {
            Files.copy(is, outputPath, StandardCopyOption.REPLACE_EXISTING);
        }

        return outputPath.toAbsolutePath().toString();
    }
}