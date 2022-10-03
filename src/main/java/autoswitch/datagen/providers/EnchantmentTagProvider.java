package autoswitch.datagen.providers;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.registry.Registry;

public class EnchantmentTagProvider extends FabricTagProvider<Enchantment> {
    /**
     * Construct a new {@link FabricTagProvider} with the default computed path.
     *
     * <p>Common implementations of this class are provided. For example @see BlockTagProvider
     *
     * @param dataGenerator The data generator instance
     */
    public EnchantmentTagProvider(FabricDataGenerator dataGenerator) {
        super(dataGenerator, Registry.ENCHANTMENT);
    }

    @Override
    protected void generateTags() {

    }

}
