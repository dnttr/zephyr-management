package org.dnttr.zephyr.management;

import org.dnttr.zephyr.management.managers.FileManager;

import java.io.IOException;

/**
 * @author dnttr
 */

//TODO: make more robust resource loading system.
public class Loader {

    public static void load() {
        try {
            new FileManager().push();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
