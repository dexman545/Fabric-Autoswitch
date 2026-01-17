package dex.autoswitch.platform;

import java.nio.file.Path;

import com.google.auto.service.AutoService;
import dex.autoswitch.fapi.client.api.ClientTags;
import dex.autoswitch.platform.services.IPlatformHelper;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.CommonHooks;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;

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
        return !FMLLoader.isProduction();
    }

    @Override
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public <T> boolean isInTagGeneral(TagKey<T> tagKey, T t) {
        return ClientTags.isInWithLocalFallback(tagKey, t);
    }

    @Override
    public ItemEnchantments getItemEnchantments(ItemStack stack) {
        var lookup = CommonHooks.resolveLookup(Registries.ENCHANTMENT);
        if (lookup != null) {
            return stack.getAllEnchantments(lookup);
        }

        return stack.getTagEnchantments();
    }
}
