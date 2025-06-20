package org.dnttr.zephyr.management.managers;

import lombok.Getter;
import org.dnttr.zephyr.bridge.ZMKit;
import org.dnttr.zephyr.management.config.impl.StringFile;

import java.io.IOException;
import java.util.HashMap;

/**
 * @author dnttr
 */
 
public class FileManager {

    @Getter
    private final HashMap<String, String> shaders;

    public FileManager() throws IOException {
        this.shaders = new HashMap<>();

        var shaderFile = new StringFile();

        this.shaders.put("vertex", shaderFile.load("shaders/vertex.vert"));
        this.shaders.put("fragment", shaderFile.load("shaders/fragment.frag"));
    }

    public void push() {
        this.shaders.forEach(ZMKit::ffi_zm_push_shader);
    }
}
