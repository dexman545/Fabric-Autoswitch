package autoswitch.datagen;

import autoswitch.datagen.providers.BlockTagProvider;
import autoswitch.datagen.providers.EnchantmentTagProvider;
import autoswitch.datagen.providers.EngLangProvider;
import autoswitch.datagen.providers.EntityTypeTagProvider;
import autoswitch.datagen.providers.ItemTagProvider;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class DataGeneration implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        var pack = fabricDataGenerator.createPack();
        // Provide some tags for use in AutoSwitch via Client/Local Tags API of Fabric API
        pack.addProvider(BlockTagProvider::new);
        pack.addProvider(EntityTypeTagProvider::new);
        pack.addProvider(EnchantmentTagProvider::new);
        pack.addProvider(ItemTagProvider::new);

        // Data generation for languages
        pack.addProvider(EngLangProvider::new);
    }

}
