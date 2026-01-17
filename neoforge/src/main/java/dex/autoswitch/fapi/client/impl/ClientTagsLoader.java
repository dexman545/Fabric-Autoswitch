/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dex.autoswitch.fapi.client.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.JsonElement;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModFileInfo;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StrictJsonParser;

/**
 * Adapted from Fabric API.
 * Converts the loading of tag files to immediately read in the bytes due to how neo handles loading.
 */
public class ClientTagsLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger("fabric-client-tags-api-v1");
    /**
     * Load a given tag from the available mods into a set of {@link ResourceLocation}s.
     * Parsing based on {@link net.minecraft.tags.TagLoader#load(net.minecraft.server.packs.resources.ResourceManager)}
     */
    public static LoadedTag loadTag(TagKey<?> tagKey) {
        var tags = new HashSet<TagEntry>();
        HashSet<byte[]> tagFiles = getTagFiles(tagKey.registry(), tagKey.location());

        for (byte[] tagPath : tagFiles) {
            try (var tagReader = new InputStreamReader(new ByteArrayInputStream(tagPath), StandardCharsets.UTF_8)) {
                JsonElement jsonElement = StrictJsonParser.parse(tagReader);
                TagFile maybeTagFile = TagFile.CODEC.parse(new Dynamic<>(JsonOps.INSTANCE, jsonElement))
                        .result().orElse(null);

                if (maybeTagFile != null) {
                    if (maybeTagFile.replace()) {
                        tags.clear();
                    }

                    tags.addAll(maybeTagFile.entries());
                }
            } catch (IOException e) {
                LOGGER.error("Error loading tag: {}", tagKey, e);
            }
        }

        HashSet<ResourceLocation> completeIds = new HashSet<>();
        HashSet<ResourceLocation> immediateChildIds = new HashSet<>();
        HashSet<TagKey<?>> immediateChildTags = new HashSet<>();

        for (TagEntry tagEntry : tags) {
            tagEntry.build(new TagEntry.Lookup<>() {
                @Override
                public @NonNull ResourceLocation element(@NonNull ResourceLocation id, boolean required) {
                    immediateChildIds.add(id);
                    return id;
                }

                @Nullable
                @Override
                public Collection<ResourceLocation> tag(@NonNull ResourceLocation id) {
                    TagKey<?> tag = TagKey.create(tagKey.registry(), id);
                    immediateChildTags.add(tag);
                    return ClientTagsImpl.getOrCreatePartiallySyncedTag(tag).completeIds;
                }
            }, completeIds::add);
        }

        // Ensure that the tag does not refer to itself
        immediateChildTags.remove(tagKey);

        return new LoadedTag(Collections.unmodifiableSet(completeIds), Collections.unmodifiableSet(immediateChildTags),
                Collections.unmodifiableSet(immediateChildIds));
    }

    public record LoadedTag(Set<ResourceLocation> completeIds, Set<TagKey<?>> immediateChildTags, Set<ResourceLocation> immediateChildIds) {
    }

    /**
     * @param resourceKey the {@link ResourceKey} of the {@link TagKey}
     * @param identifier  the {@link ResourceLocation} of the tag
     * @return the paths to all tag json files within the available mods
     */
    private static HashSet<byte[]> getTagFiles(ResourceKey<? extends Registry<?>> resourceKey, ResourceLocation identifier) {
        return getTagFiles(Registries.tagsDirPath(resourceKey), identifier);
    }

    /**
     * @return the paths to all tag json files within the available mods
     */
    private static HashSet<byte[]> getTagFiles(String tagType, ResourceLocation identifier) {
        String tagFile = "data/%s/%s/%s.json".formatted(identifier.getNamespace(), tagType, identifier.getPath());
        return getResourcePaths(tagFile);
    }

    /**
     * @return all paths from the available mods that match the given internal path
     */
    private static HashSet<byte[]> getResourcePaths(String path) {
        HashSet<byte[]> out = new HashSet<>();

        try {
            for (IModFileInfo modFile : ModList.get().getModFiles()) {
                var jarContents = modFile.getFile().getContents();
                if (jarContents.containsFile(path)) {
                    try {
                        out.add(jarContents.readFile(path));
                    } catch (IOException e) {
                        LOGGER.error("Error reading tag file: {}", path, e);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error reading tag file: {}", path, e);
        }

        return out;
    }
}
