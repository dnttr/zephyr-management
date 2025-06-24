package org.dnttr.zephyr.bridge.internal;

/**
 * @author dnttr
 */

public final class ZAKit {

    public static native int ffi_zm_push_shader(String name, String source);

    public static native int ffi_zm_finish_loading();
}
