package dex.autoswitch.test;

import dex.autoswitch.Constants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class LoadingTest {
    @Test
    void configLoaded() {
        // Test loading of defaults into config file
        // Mostly here so gradle won't complain about no tests existing
        assertFalse(Constants.CONFIG.getConfiguration().isEmpty());
    }
}
