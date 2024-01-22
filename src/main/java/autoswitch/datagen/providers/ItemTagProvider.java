package autoswitch.datagen.providers;

import java.util.concurrent.CompletableFuture;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;

public class ItemTagProvider extends FabricTagProvider.ItemTagProvider {
    public ItemTagProvider(FabricDataOutput output,
                           CompletableFuture<HolderLookup.Provider> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider arg) {
        tag(TagKey.create(Registries.ITEM, new ResourceLocation("autoswitch:any")))
                .addOptionalTag(ConventionalItemTags.AXES.location())//todo how to handle
                .addOptionalTag(ItemTags.AXES.location())
                //.addOptionalTag(ConventionalItemTags.BOWS)
                .addOptionalTag(ConventionalItemTags.SHEARS.location())
                .addOptionalTag(ConventionalItemTags.SHOVELS.location())
                .addOptionalTag(ItemTags.SHOVELS.location())
                .addOptionalTag(ConventionalItemTags.SWORDS.location())
                .addOptionalTag(ItemTags.SWORDS.location())
                .addOptionalTag(ConventionalItemTags.SPEARS.location())
                .addOptionalTag(ConventionalItemTags.HOES.location())
                .addOptionalTag(ItemTags.HOES.location())
                .addOptionalTag(ConventionalItemTags.PICKAXES.location())
                .addOptionalTag(ItemTags.PICKAXES.location());
    }

}
