package autoswitch.datagen.providers;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;

public class ItemTagProvider extends FabricTagProvider.ItemTagProvider {
    public ItemTagProvider(FabricDataGenerator dataGenerator) {
        super(dataGenerator);
    }

    @Override
    protected void generateTags() {
        getOrCreateTagBuilder(TagKey.of(Registry.ITEM_KEY, new Identifier("autoswitch:any")))
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
