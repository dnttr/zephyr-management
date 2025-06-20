import org.dnttr.zephyr.management.managers.FileManager;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * @author dnttr
 */

public class FileReaderTest {

    @Test
    public void testReadFile() {
        try {
            new FileManager();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
