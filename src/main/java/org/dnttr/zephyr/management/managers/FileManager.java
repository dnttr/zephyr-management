package org.dnttr.zephyr.management.managers;

import lombok.Getter;
import org.dnttr.zephyr.bridge.internal.ZAKit;
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

        this.shaders.put("rectangle_vert", shaderFile.load("shaders/rectangle_vert.glsl"));
        this.shaders.put("rectangle_frag", shaderFile.load("shaders/rectangle_frag.glsl"));
    }

    public void push() {
        this.shaders.forEach(ZAKit::ffi_zm_push_shader);
        ZAKit.ffi_zm_finish_loading();
    }
}
