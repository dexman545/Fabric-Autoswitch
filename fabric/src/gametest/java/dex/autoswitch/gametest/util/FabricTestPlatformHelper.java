package dex.autoswitch.gametest.util;

import com.google.auto.service.AutoService;
import dex.autoswitch.platform.FabricPlatformHelper;
import dex.autoswitch.platform.services.IPlatformHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import java.util.Objects;
import java.util.Optional;

@AutoService(IPlatformHelper.class)
public class FabricTestPlatformHelper extends FabricPlatformHelper {
    private RegistryAccess access;

    public void setServer(RegistryAccess access) {
        this.access = access;
    }

    @Override
    public RegistryAccess getRegistryAccess() {
        if (access != null) {
            return access;
        }

        return super.getRegistryAccess();
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
        return (Optional<? extends Registry<T>>) BuiltInRegistries.REGISTRY.getOptional(tagKey.registry().location());
    }

    private static <U extends Registry<T>, T> Optional<Holder<T>> getRegistryEntry(U registry, TagKey<T> tagKey, T entry) {
        if (!tagKey.isFor(registry.key())) {
            return Optional.empty();
        }

        Optional<ResourceKey<T>> maybeKey = registry.getResourceKey(entry);

        return maybeKey.map(registry::getOrThrow);
    }
}
