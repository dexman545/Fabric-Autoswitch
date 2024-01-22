package autoswitch.config.commands;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import autoswitch.AutoSwitch;
import autoswitch.config.io.ConfigEstablishment;
import autoswitch.config.util.Comment;
import autoswitch.config.util.ConfigReflection;
import autoswitch.config.util.GameConfigEditorUtil;
import autoswitch.config.util.TranslationKey;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import net.minecraft.network.chat.Component;

/**
 * Helper class for generating commands from a class file's methods.
 */
public class CommandGenerator {
    private final Method[] methods;
    private final BiFunction<String, Method, Command<FabricClientCommandSource>> maker;

    /**
     * Takes a class or interface and generates commands based on the methods declared within it.
     *
     * @param maker converter of a command name, the method it is for, to a {@link Command<FabricClientCommandSource>}
     *              that executes when a command is run.
     * @param clazz class to generate commands from.
     */
    public CommandGenerator(Class<?> clazz, BiFunction<String, Method, Command<FabricClientCommandSource>> maker) {
        this(clazz, maker, false);
    }

    /**
     * Takes a class or interface and generates commands based on the methods declared within it.
     *
     * @param clazz      class to generate commands from.
     * @param maker      converter of a command name, the method it is for, to a
     *                   {@link Command<FabricClientCommandSource>} that executes when a command is run.
     * @param allMethods whether to use only declared methods or all methods including those inherited.
     */
    public CommandGenerator(Class<?> clazz, BiFunction<String, Method, Command<FabricClientCommandSource>> maker,
                            boolean allMethods) {
        this.maker = maker;
        if (allMethods) {
            // todo add filter, eg. for Object's methods
            this.methods = clazz.getMethods();
        } else {
            this.methods = clazz.getDeclaredMethods();
        }

    }

    private static GenericCommand buildCommandOption(final Method method,
                                                     BiFunction<String, Method, Command<FabricClientCommandSource>> maker) {
        return new GenericCommand() {
            @Override
            public ArgumentType<?> argumentType() {
                return CommandGenerator.argumentType(method);
            }

            @Override
            public String name() {
                return ConfigReflection.key(method);
            }

            @Override
            public Command<FabricClientCommandSource> command() {
                return maker.apply(name(), method);
            }

            @Override
            public String failureMessage() {
                Comment c = method.getAnnotation(Comment.class);
                if (c == null) {
                    return "";
                }
                return c.value();
            }

            @Override
            public int repetitions() {
                Class<?> clazz;
                if ((clazz = getEnum4Collection(method)) != null) {
                    return clazz.getEnumConstants().length;
                }
                return 1;
            }

            @Override
            public Object owningOption() {
                return method;
            }
        };
    }

    /**
     * Generates a consumer that converts a {@link GenericCommand} to a command as part of the builder.
     *
     * @param builder the command builder to add the command(s) to.
     *
     * @return the consumer.
     */
    public static Consumer<GenericCommand> createGeneric2RealCommandConverter(
            LiteralArgumentBuilder<FabricClientCommandSource> builder) {

        return (c) -> {
            if (c.argumentType() instanceof GenericEnumArgument gea) {
                if (gea.isCollection()) {
                    Command<FabricClientCommandSource> execution = context -> {
                        var option = context.getArgument("option", gea.getEnum());
                        var enabled = context.getArgument("enabled", boolean.class);
                        Collection<Object> currentValue = null;
                        Consumer<Object> con = o -> {};
                        if (c.owningOption() instanceof Method m) {
                            currentValue = GameConfigEditorUtil.FEATURE_CONFIG.currentValue(m);
                            con = GameConfigEditorUtil.FEATURE_CONFIG.modifyConfig(m);
                        }

                        if (currentValue != null) {
                            if (enabled) {
                                currentValue.add(option);
                            } else {
                                currentValue.remove(option);
                            }

                            con.accept(currentValue);
                        }

                        try {
                            ConfigEstablishment.writeConfigFiles();
                            context.getSource().sendFeedback(Component.nullToEmpty("Config file updated."));
                        } catch (IOException e) {
                            context.getSource().sendError(Component.nullToEmpty("Failed to update config file."));
                            AutoSwitch.logger.error("Failed to update config file", e);
                            return 1;
                        }
                        return 0;
                    };

                    var options = ClientCommandManager.argument(c.parameter(), c.argumentType())
                                                      .then(ClientCommandManager.argument("enabled", BoolArgumentType.bool())
                                                                                .executes(execution));
                    var subNodes = ClientCommandManager.literal(c.name())
                                                       .executes(context -> {
                                                           context.getSource()
                                                                  .sendFeedback(
                                                                          Component.translatable(c.translationKey(),
                                                                                            AutoSwitch.featureCfg
                                                                                                    .getProperty(c.name())));
                                                           return 0;
                                                       })
                                                       .then(options);
                    builder.then(subNodes);

                    return;
                }
            }

            builder.then(ClientCommandManager.literal(c.name()).executes(context -> {
                context.getSource().sendError(Component.nullToEmpty("Please specify an option."));
                context.getSource().sendError(Component.nullToEmpty(c.failureMessage()));
                return 1;
            }).then(ClientCommandManager.argument(c.parameter(), c.argumentType()).executes(c.command())));
        };
    }

    /**
     * Generates an argument type from a method.
     *
     * @param method the method to generate the argument type for.
     *
     * @return the available argument type.
     */
    @SuppressWarnings("unchecked")
    public static <U extends Enum<U>> @Nullable ArgumentType<?> argumentType(@NotNull Method method) {
        Class<?> clazz = method.getReturnType();
        if (clazz.equals(Boolean.class)) return BoolArgumentType.bool();
        if (clazz.isEnum()) return new GenericEnumArgument<U>((Class<U>) clazz, false);
        if (clazz.equals(Float.class)) return FloatArgumentType.floatArg();
        if (clazz.equals(Double.class)) return DoubleArgumentType.doubleArg();
        if (clazz.equals(Integer.class)) return IntegerArgumentType.integer();
        if (clazz.equals(String.class)) return StringArgumentType.greedyString();
        var maybeEnum = getEnum4Collection(method);
        if (maybeEnum != null) return new GenericEnumArgument<U>((Class<U>) maybeEnum, true);
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <U extends Enum<U>> Class<U> getEnum4Collection(Method method) {
        var clazz = method.getReturnType();
        if (Collection.class.isAssignableFrom(clazz)) {
            if (method.getGenericReturnType() instanceof ParameterizedType parameterizedType) {
                var t = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                return (Class<U>) t;
            }
        }

        return null;
    }

    public Set<GenericCommand> getCommands() {
        Set<GenericCommand> out = new ObjectArraySet<>();
        for (Method method : methods) {
            TranslationKey ano;
            if ((ano = method.getAnnotation(TranslationKey.class)) != null) {
                if (!ano.showInUi()) {
                    continue;
                }
            }
            out.add(buildCommandOption(method, maker));
        }
        return out;
    }

}
