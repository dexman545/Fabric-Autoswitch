package autoswitch.datagen.providers;

import java.util.concurrent.CompletableFuture;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;

import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;

public class ItemTagProvider extends FabricTagProvider.ItemTagProvider {
    public ItemTagProvider(FabricDataOutput output,
                           CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup arg) {
        getOrCreateTagBuilder(TagKey.of(RegistryKeys.ITEM, new Identifier("autoswitch:any")))
                .addOptionalTag(ConventionalItemTags.AXES)
                //.addOptionalTag(ConventionalItemTags.BOWS)
                .addOptionalTag(ConventionalItemTags.SHEARS)
                .addOptionalTag(ConventionalItemTags.SHOVELS)
                .addOptionalTag(ConventionalItemTags.SWORDS)
                .addOptionalTag(ConventionalItemTags.SPEARS)
                .addOptionalTag(ConventionalItemTags.HOES)
                .addOptionalTag(ConventionalItemTags.PICKAXES);
    }

}
