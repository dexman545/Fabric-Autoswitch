package dex.autoswitch.test.tests;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongepowered.asm.mixin.MixinEnvironment;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;

public class MixinAuditTest {
    @BeforeAll
    static void beforeAll() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void auditMixins() {
        MixinEnvironment.getCurrentEnvironment().audit();
    }
}
