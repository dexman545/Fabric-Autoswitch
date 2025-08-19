package dex.autoswitch.platform.services;

import java.nio.file.Path;
import java.util.Set;

import dex.autoswitch.Tags;

import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.tags.TagKey;

public interface IPlatformHelper {

    /**
     * Gets the name of the current platform
     *
     * @return The name of the current platform.
     */
    String getPlatformName();

    /**
     * Checks if a mod with the given id is loaded.
     *
     * @param modId The mod to check if it is loaded.
     * @return True if the mod is loaded, false otherwise.
     */
    boolean isModLoaded(String modId);

    /**
     * Check if the game is currently in a development environment.
     *
     * @return True if in a development environment, false otherwise.
     */
    boolean isDevelopmentEnvironment();

    /**
     * @return The config directory of the game.
     */
    Path getConfigDir();

    /**
     * This is the actual implementation of {@link #isInTag(TagKey, Object)} for checking if an object is in a tag.
     *
     * @param tagKey tag to check for
     * @param t object to check for
     * @param <T> object type
     * @return if the given tag contains the given object
     */
    <T> boolean isInTagGeneral(TagKey<T> tagKey, T t);

    /**
     * A utility method to get the {@link RegistryAccess} of the game.
     */
    default RegistryAccess getRegistryAccess() {
        //noinspection ConstantValue
        if (Minecraft.getInstance() != null) {
            if (Minecraft.getInstance().level != null) {
                //noinspection ConstantValue
                if (Minecraft.getInstance().level.registryAccess() != null) {
                    return Minecraft.getInstance().level.registryAccess();
                }
            }
        }

        return null;
    }

    /**
     * Checks if the given object is in the given tag, handling custom tags from {@link Tags}.
     *
     * @see Tags
     * @see #isInTagGeneral(TagKey, Object)
     *
     * @param tagKey tag to check for
     * @param t object to check for
     * @param <T> object type
     * @return if the given tag contains the given object
     */
    default <T> boolean isInTag(TagKey<T> tagKey, T t) {
        return switch (Tags.getTag(tagKey)) {
            case Tags.Group.CustomPredicate<T> v -> v.predicate().test(t);
            case Tags.Group.CustomTag<T>(Set<T> entries, Set<TagKey<T>> includedTags) -> {
                if (entries.contains(t)) {
                    yield true;
                }

                for (TagKey<T> includedTag : includedTags) {
                    if (isInTag(includedTag, t)) {
                        yield true;
                    }
                }

                yield false;
            }
            case null -> isInTagGeneral(tagKey, t);
        };
    }

    /**
     * Gets the name of the environment type as a string.
     *
     * @return The name of the environment type.
     */
    default String getEnvironmentName() {
        return isDevelopmentEnvironment() ? "development" : "production";
    }
}
