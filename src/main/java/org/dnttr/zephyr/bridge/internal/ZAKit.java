package org.dnttr.zephyr.bridge.internal;

import java.nio.ByteBuffer;

/**
 * @author dnttr
 */

public final class ZAKit {

    public static native int ffi_zm_push_shader(String name, String buffer);

    public static native int ffi_zm_push_texture(String name, ByteBuffer buffer, int width, int height);

    public static native int ffi_zm_push_font(String name, byte[] buffer);

    public static native int ffi_zm_finish_loading();
}
