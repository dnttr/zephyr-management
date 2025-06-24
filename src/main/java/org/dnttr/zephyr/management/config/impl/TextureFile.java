package org.dnttr.zephyr.management.config.impl;

import lombok.Cleanup;
import org.dnttr.zephyr.management.config.IFile;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * @author dnttr
 */

public class TextureFile implements IFile<ByteBuffer> {

    @Override
    public ByteBuffer load(@NotNull String path) throws IOException {
        @Cleanup InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);

        if (is == null) {
            throw new FileNotFoundException("File " + path + " not found");
        }

        BufferedImage img = ImageIO.read(is);
        ByteBuffer buffer = ByteBuffer.allocateDirect(img.getWidth() * img.getHeight() * 4 /* RBGBA */);
        BufferedImage argbImage = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);

        argbImage.getGraphics().drawImage(img, 0, 0, null);

        int[] pixels = ((DataBufferInt) argbImage.getRaster().getDataBuffer()).getData();

        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int pixel = pixels[y * img.getWidth() + x];

                byte a = (byte) ((pixel >> 24) & 0xFF);
                byte r = (byte) ((pixel >> 16) & 0xFF);
                byte g = (byte) ((pixel >> 8) & 0xFF);
                byte b = (byte) (pixel & 0xFF);

                buffer.put(r);
                buffer.put(g);
                buffer.put(b);
                buffer.put(a);
            }
        }

        buffer.flip();
        return buffer;
    }
}
