package org.dnttr.zephyr.management;

import org.jetbrains.annotations.ApiStatus;

/**
 * Direct use of this class is discouraged and should be avoided.
 * <br><br>
 * Tampering with these bindings may lead compromise the integrity of the vm and therefore undefined behavior.
 * This class is not intended for public use and is subject to change without notice.
 *
 * @implNote All parameters that are marked with _ptr are return buffers. While the native code will fill them with data, it is the caller's responsibility to ensure that these buffers are of sufficient size.
 *
 * @author dnttr
 * @since 1.0.0
 */

@ApiStatus.Internal
public final class Bindings {

    /**
     * Creates a new session.
     *
     * @param u session identifier
     * @return 0 on success, non-zero on failure
     */
    public native int ffi_open_session(long u);

    /**
     * Closes an existing session.
     *
     * @param u unique identifier for the session
     * @return 0 on success, non-zero on failure
     */
    public native int ffi_close_session(long u);

    public native int ffi_push(long uuid, byte[] data);
}
