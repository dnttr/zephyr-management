package org.dnttr.zephyr.bridge.ipc;

import org.dnttr.zephyr.ClientIPC;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class IpcBridge {

    private IpcBridge() {}

    /**
     * Pushes a shader to the C++ client.
     * The shader source is Base64 encoded to ensure it can be transmitted
     * as a single, safe line of text.
     *
     * @param name The name of the shader.
     * @param source The raw GLSL source code.
     */
    public static void pushShader(String name, String source) {
        String encodedSource = Base64.getEncoder().encodeToString(source.getBytes(StandardCharsets.UTF_8));
        ClientIPC.sendMessage("PUSH_SHADER:" + name + ":" + encodedSource);
    }

    /**
     * Pushes a texture to the C++ client.
     * The texture's byte buffer is Base64 encoded.
     *
     * @param name The name of the texture.
     * @param buffer The raw byte data of the texture.
     * @param width The width of the texture.
     * @param height The height of the texture.
     */
    public static void pushTexture(String name, byte[] buffer, int width, int height) {
        String encodedBuffer = Base64.getEncoder().encodeToString(buffer);
        ClientIPC.sendMessage("PUSH_TEXTURE:" + name + ":" + encodedBuffer + ":" + width + ":" + height);
    }

    /**
     * Pushes a font file to the C++ client.
     * The font's byte array is Base64 encoded.
     *
     * @param name The name of the font.
     * @param bytes The raw byte data of the font file.
     */
    public static void pushFont(String name, byte[] bytes) {
        String encodedBytes = Base64.getEncoder().encodeToString(bytes);
        ClientIPC.sendMessage("PUSH_FONT:" + name + ":" + encodedBytes);
    }

    /**
     * Signals to the C++ client that all initial resources have been sent.
     */
    public static void finishLoading() {
        ClientIPC.sendMessage("FINISH_LOADING");
    }
}