package autoswitch.datagen.providers;

import java.util.concurrent.CompletableFuture;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;

import net.minecraft.util.registry.RegistryWrapper;

public class EntityTypeTagProvider extends FabricTagProvider.EntityTypeTagProvider  {
    public EntityTypeTagProvider(FabricDataOutput output,
                                 CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup arg) {

    }

}
