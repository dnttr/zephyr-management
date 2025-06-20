package org.dnttr.zephyr.bridge;

/**
 * @author dnttr
 */

public final class ZMKit {

    public static native int ffi_zm_push_shader(String name, String source);
}
