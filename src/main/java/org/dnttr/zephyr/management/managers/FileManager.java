package org.dnttr.zephyr.management.managers;

import lombok.Getter;
import org.dnttr.zephyr.bridge.internal.ZAKit;
import org.dnttr.zephyr.management.config.impl.ShaderFile;
import org.dnttr.zephyr.management.config.impl.TextureFile;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;

/**
 * @author dnttr
 */
 
public class FileManager {

    @Getter
    private final HashMap<String, String> shaders;

    @Getter
    private final HashMap<String, ByteBuffer> textures;

    public FileManager() throws IOException {
        this.shaders = new HashMap<>();
        this.textures = new HashMap<>();

        var shaderFile = new ShaderFile();
        var textureFile = new TextureFile();

        this.textures.put("test.png", textureFile.load("textures/test.png"));

        this.shaders.put("rectangle_vert", shaderFile.load("shaders/rectangle_vert.glsl"));
        this.shaders.put("rectangle_frag", shaderFile.load("shaders/rectangle_frag.glsl"));
    }

    public void push() {
        this.shaders.forEach(ZAKit::ffi_zm_push_shader);
        this.textures.forEach(ZAKit::ffi_zm_push_texture);

        ZAKit.ffi_zm_finish_loading();
    }
}
