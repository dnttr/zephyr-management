package org.dnttr.zephyr.management.managers;

import lombok.Getter;
import org.dnttr.zephyr.bridge.internal.ZAKit;
import org.dnttr.zephyr.management.Texture;
import org.dnttr.zephyr.management.config.impl.FontFile;
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
    private final HashMap<String, Texture> textures;

    @Getter
    private final HashMap<String, byte[]> fonts;

    public FileManager() throws IOException {
        this.shaders = new HashMap<>();
        this.textures = new HashMap<>();
        this.fonts = new HashMap<>();

        var shaderFile = new ShaderFile();
        var textureFile = new TextureFile();
        var fontFile = new FontFile();

        this.textures.put("test.png", textureFile.load("textures/test.png"));

        this.shaders.put("rectangle_vert", shaderFile.load("shaders/rectangle_vert.glsl"));
        this.shaders.put("rectangle_frag", shaderFile.load("shaders/rectangle_frag.glsl"));
        this.shaders.put("texture_frag", shaderFile.load("shaders/texture_frag.glsl"));
        this.shaders.put("texture_vert", shaderFile.load("shaders/texture_vert.glsl"));
        this.shaders.put("text_frag", shaderFile.load("shaders/text_frag.glsl"));
        this.shaders.put("text_vert", shaderFile.load("shaders/text_vert.glsl"));

        this.fonts.put("Roboto_Condensed-Medium", fontFile.load("fonts/Roboto_Condensed-Medium.ttf"));
    }

    public void push() {
        this.shaders.forEach(ZAKit::ffi_zm_push_shader);
        this.fonts.forEach(ZAKit::ffi_zm_push_font);
        this.textures.forEach((name, texture) -> ZAKit.ffi_zm_push_texture(name, texture.buffer(),  texture.width(), texture.height()));

        ZAKit.ffi_zm_finish_loading();
    }
}
