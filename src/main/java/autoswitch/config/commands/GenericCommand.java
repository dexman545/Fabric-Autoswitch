package autoswitch.config.commands;

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

    default String paramater() {
        return "option";
    }

    Command<FabricClientCommandSource> command();

    String failureMessage();

}
