package autoswitch.config.commands;

import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import org.apache.commons.lang3.EnumUtils;

import net.minecraft.command.CommandSource;

@SuppressWarnings({"unchecked", "rawtypes"})
public class GenericEnumArgument implements ArgumentType {
    private final Collection<String> EXAMPLES = new ReferenceArrayList<>();
    private final Class<?> clazz;

    public <Y extends Enum<Y>> GenericEnumArgument(Class<?> yClass) {
        clazz = yClass;
        if (!yClass.isEnum()) return;
        for (Object ec : yClass.getEnumConstants()) {
            EXAMPLES.add(((Y)ec).name());
        }
    }

    @Override
    public Object parse(StringReader reader) throws CommandSyntaxException {
        return EnumUtils.getEnum((Class<? extends Enum>) clazz, reader.readUnquotedString().toUpperCase(Locale.ENGLISH));
    }

    @Override
    public CompletableFuture<Suggestions> listSuggestions(CommandContext context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(EXAMPLES, builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

}
