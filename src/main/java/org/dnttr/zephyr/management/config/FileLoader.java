package org.dnttr.zephyr.management.config;

import lombok.Getter;
import org.dnttr.zephyr.management.config.files.impl.StringFile;

import java.io.IOException;
import java.util.HashMap;

/**
 * @author dnttr
 */
 
public class FileLoader {

    @Getter
    private final HashMap<String, String> shaders;

    public FileLoader() throws IOException {
        this.shaders = new HashMap<>();
        var shaderFile = new StringFile();

        this.shaders.put("vertex", shaderFile.load("shaders/shader.glsl"));
    }
}
