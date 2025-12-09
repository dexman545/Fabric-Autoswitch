package dex.autoswitch.platform;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import com.google.auto.service.AutoService;
import dex.autoswitch.platform.services.IPlatformHelper;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

@AutoService(IPlatformHelper.class)
public class NeoForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.getCurrent().isProduction();
    }

    @Override
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public <T> boolean isInTagGeneral(TagKey<T> tagKey, T t) {
        // Neo does not have client tags, so use MC normally
        // BlockState#is(TagKey) and such exists, but without forwarding the original type here we can't use it
        var reg = getRegistry(getRegistryAccess(), tagKey);
        if (reg.isPresent()) {
            if (reg.get().get(tagKey).isPresent()) {
                var entry = getRegistryEntry(reg.get(), tagKey, t);
                if (entry.isPresent()) {
                    return entry.get().is(tagKey);
                }
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    private static <T> Optional<? extends Registry<T>> getRegistry(RegistryAccess registryAccess, TagKey<T> tagKey) {
        Objects.requireNonNull(tagKey);

        // Check if the tag represents a dynamic registry
        var maybeRegistry = registryAccess.lookup(tagKey.registry());
        if (maybeRegistry.isPresent()) return maybeRegistry;

        // Return the default registries
        return (Optional<? extends Registry<T>>) BuiltInRegistries.REGISTRY.getOptional(tagKey.registry().identifier());
    }

    private static <U extends Registry<T>, T> Optional<Holder<T>> getRegistryEntry(U registry, TagKey<T> tagKey, T entry) {
        if (!tagKey.isFor(registry.key())) {
            return Optional.empty();
        }

        Optional<ResourceKey<T>> maybeKey = registry.getResourceKey(entry);

        return maybeKey.map(registry::getOrThrow);
    }
}
