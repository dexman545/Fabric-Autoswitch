package autoswitch.config.commands;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import autoswitch.config.util.Comment;
import autoswitch.config.util.ConfigReflection;

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

import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;

import net.minecraft.text.LiteralText;

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
     * {@link Command<FabricClientCommandSource>}
     *                   that executes when a command is run.
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

    public Set<GenericCommand> getCommands() {
        Set<GenericCommand> out = new ObjectArraySet<>();
        for (Method method : methods) {
            out.add(buildCommandOption(method, maker));
        }
        return out;
    }

    private static GenericCommand buildCommandOption(
            final Method method, BiFunction<String, Method, Command<FabricClientCommandSource>> maker) {
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
        return (c) -> builder.then(ClientCommandManager.literal(c.name()).executes(context -> {
            context.getSource().sendError(new LiteralText("Please specify an option."));
            context.getSource().sendError(new LiteralText(c.failureMessage()));
            return 1;
        }).then(ClientCommandManager.argument(c.paramater(), c.argumentType()).executes(c.command())));
    }

    /**
     * Generates an argument type from a method.
     *
     * @param method the method to generate the argument type for.
     *
     * @return the available argument type.
     */
    public static @Nullable ArgumentType<?> argumentType(@NotNull Method method) {
        Class<?> clazz = method.getReturnType();
        if (clazz.equals(Boolean.class)) return BoolArgumentType.bool();
        if (clazz.isEnum()) return new GenericEnumArgument(clazz);
        if (clazz.equals(Float.class)) return FloatArgumentType.floatArg();
        if (clazz.equals(Double.class)) return DoubleArgumentType.doubleArg();
        if (clazz.equals(Integer.class)) return IntegerArgumentType.integer();
        if (clazz.equals(String.class)) return StringArgumentType.greedyString();
        return null;
    }

}
