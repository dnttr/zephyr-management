package org.dnttr.zephyr.management;

import java.nio.ByteBuffer;

/**
 * @author dnttr
 */

public record Texture(ByteBuffer buffer, int width, int height) {

}
