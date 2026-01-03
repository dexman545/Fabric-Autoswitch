package dex.autoswitch.platform;

import java.nio.file.Path;

import com.google.auto.service.AutoService;
import dex.autoswitch.platform.services.IPlatformHelper;

import net.minecraft.tags.TagKey;

import net.fabricmc.fabric.api.tag.client.v1.ClientTags;
import net.fabricmc.loader.api.FabricLoader;

@AutoService(IPlatformHelper.class)
public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public <T> boolean isInTagGeneral(TagKey<T> tagKey, T t) {
        return ClientTags.isInWithLocalFallback(tagKey, t);
    }
}
