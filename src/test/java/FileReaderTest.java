import org.dnttr.zephyr.management.config.FileLoader;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * @author dnttr
 */

public class FileReaderTest {

    @Test
    public void testReadFile() {
        FileLoader fileLoader = new FileLoader();
        try {
            fileLoader.getConfigFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
