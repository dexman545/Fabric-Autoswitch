package autoswitch.config.commands;

import java.lang.reflect.Method;

import autoswitch.config.util.ConfigReflection;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public interface GenericCommand {

    /**
     * This method exists to function as a predicate.
     *
     * @return if the argument type is null.
     */
    static boolean wasGenerated(GenericCommand c) {
        return c.argumentType() != null;
    }

    @Nullable ArgumentType<?> argumentType();

    String name();

    default String parameter() {
        return "option";
    }

    default String translationKey() {
        if (owningOption() instanceof Method m) {
            return "currently.autoswitch." + ConfigReflection.translationKey(m);
        }

        return "noText";
    }

    Command<FabricClientCommandSource> command();

    String failureMessage();

    default int repetitions() {
        return 1;
    }

    Object owningOption();

}
