package dex.autoswitch.harness;

import com.google.auto.service.AutoService;
import dex.autoswitch.platform.NeoForgePlatformHelper;
import dex.autoswitch.platform.services.IPlatformHelper;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;

@AutoService(IPlatformHelper.class)
public class NeoForgeTestPlatformHelper extends NeoForgePlatformHelper {
    private MinecraftServer server;

    public void setServer(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public RegistryAccess getRegistryAccess() {
        if (server != null) {
            return server.registryAccess();
        }

        return super.getRegistryAccess();
    }
}
