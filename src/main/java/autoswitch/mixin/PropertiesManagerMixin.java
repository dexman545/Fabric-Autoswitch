package autoswitch.mixin;

import autoswitch.config.SortedProperties;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Properties;

@Mixin(targets = "org.aeonbits.owner.PropertiesManager", remap = false)
abstract class PropertiesManagerMixin {
    @ModifyVariable(method = "<init>", at = @At(value = "FIELD", target = "Lorg/aeonbits/owner/PropertiesManager;properties:Ljava/util/Properties;", opcode = Opcodes.PUTFIELD, shift = At.Shift.BEFORE))
    private Properties sorted(final Properties properties) {
        return new SortedProperties(properties);
    }

    @Redirect(method = { "reload", "clear" }, at = @At(value = "NEW", target = "java/util/Properties"))
    private Properties sorted() {
        return new SortedProperties();
    }
}
