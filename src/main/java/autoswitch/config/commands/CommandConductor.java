package autoswitch.config.commands;

import static autoswitch.AutoSwitch.doAS;
import static autoswitch.AutoSwitch.featureCfg;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import autoswitch.AutoSwitch;
import autoswitch.config.AutoSwitchConfig;
import autoswitch.config.io.ConfigEstablishment;
import autoswitch.mixin.impl.ConnectionHandler;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import net.minecraft.text.Text;

public class CommandConductor {
    /**
     * Converts a config key and method into {@link Command<FabricClientCommandSource>} that modifies the config file.
     * <p>
     * Only works for those that don't override {@link GenericCommand#paramater()}as it uses the default value.
     */
    private static final BiFunction<String, Method, Command<FabricClientCommandSource>> configCommandMaker =
            (name, method) -> (context) -> {
                featureCfg.setProperty(name,
                                       context.getArgument("option" /*Default value of 'paramater' in GenericCommand*/,
                                                           method.getReturnType()).toString());
                try {
                    ConfigEstablishment.writeConfigFiles();
                    context.getSource().sendFeedback(Text.of("Config file updated."));
                } catch (IOException e) {
                    context.getSource().sendError(Text.of("Failed to update config file."));
                    AutoSwitch.logger.error("Failed to update config file", e);
                    return 1;
                }
                return 0;
            };

    public static void registerAllCommands() {
        if (!featureCfg.enableConfigCommands()) return;

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher
                    .register(ClientCommandManager.literal("autoswitch_toggle")
                                                  .executes(context -> changeASToggle(context, !doAS)));

            // Create command builder and add message for when it is run on its own
            LiteralArgumentBuilder<FabricClientCommandSource> autoswitchCommandBuilder =
                    ClientCommandManager.literal("autoswitch").executes(context -> {
                        context.getSource().sendFeedback(
                                Text.translatable("Commands for changing AutoSwitch's feature config options, " +
                                                  "except for the tool targets. Please see the config files for " +
                                                  "complete set of options and documentation. Rewrites the config files."));
                        return 1;
                    });

            // Create consumer for adding a GenericCommand to the builder
            Consumer<GenericCommand> genericCommandConsumer =
                    CommandGenerator.createGeneric2RealCommandConverter(autoswitchCommandBuilder);

            // Build config commands
            (new CommandGenerator(AutoSwitchConfig.class, configCommandMaker)).getCommands().stream()
                                                                              .filter(GenericCommand::wasGenerated)
                                                                              .forEach(genericCommandConsumer);

            // Add toggle command
            autoswitchCommandBuilder
                    .then(ClientCommandManager
                                  .literal("toggleSwitchEnabled")
                                  .then(ClientCommandManager.argument("allowed", BoolArgumentType.bool())
                                                            .executes(CommandConductor::changeASToggle)));

            autoswitchCommandBuilder
                    .then(ClientCommandManager.literal("resetSwitchState")
                                              .executes(CommandConductor::resetState));

            // Register commands
            dispatcher.register(autoswitchCommandBuilder);
        });
    }

    private static int changeASToggle(CommandContext<FabricClientCommandSource> context) {
        return changeASToggle(context, BoolArgumentType.getBool(context, "allowed"));
    }

    /**
     * Change AutoSwitch toggle to the new value if the context allows it.
     */
    private static int changeASToggle(CommandContext<FabricClientCommandSource> context, boolean newValue) {
        boolean allowed = context.getSource().getClient().isInSingleplayer() || featureCfg.switchInMP();

        if (!allowed) {
            context.getSource().sendError(Text.of("Switching is disabled on servers!"));
            return 1;
        }

        String tlKeyTruthy = "msg.autoswitch.toggle_true";
        String tlKeyFalsy = "msg.autoswitch.toggle_false";

        context.getSource().sendFeedback(Text.translatable(newValue ? tlKeyTruthy : tlKeyFalsy));

        doAS = newValue;

        return 0;
    }

    /**
     * Change AutoSwitch toggle to the new value if the context allows it.
     */
    private static int resetState(CommandContext<FabricClientCommandSource> context) {

        context.getSource().sendFeedback(Text.translatable("msg.autoswitch.reset"));

        ConnectionHandler.reset();

        return 0;
    }

}
