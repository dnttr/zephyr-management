package org.dnttr.zephyr;

import org.dnttr.zephyr.management.config.impl.NativeFile;

import java.io.IOException;

/**
 * @author dnttr
 */

public class Loader {

    public static void main(String[] args) {
        try {
            NativeFile nativeFile = new NativeFile();

            System.load(nativeFile.load("natives/libsodium.26.dylib"));
            System.load(nativeFile.load("natives/libsodium.dylib"));
            System.load(nativeFile.load("natives/libznb.dylib"));
            System.load(nativeFile.load("natives/libze.dylib"));

            Application.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
