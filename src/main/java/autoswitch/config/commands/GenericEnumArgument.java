package autoswitch.config.commands;

import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import org.apache.commons.lang3.EnumUtils;

import net.minecraft.command.CommandSource;

@SuppressWarnings({"unchecked", "rawtypes"})
public class GenericEnumArgument<Y extends Enum<Y>> implements ArgumentType {
    private final Collection<String> EXAMPLES = new ReferenceArrayList<>();
    private final Class<Y> clazz;
    private final boolean isCollection;

    public GenericEnumArgument(Class<Y> yClass, boolean isCollection) {
        clazz = yClass;
        this.isCollection = isCollection;
        if (!yClass.isEnum()) return;
        for (Y ec : yClass.getEnumConstants()) {
            EXAMPLES.add(ec.name());
        }
    }

    @Override
    public Object parse(StringReader reader) {
        return EnumUtils.getEnum(clazz, reader.readUnquotedString().toUpperCase(Locale.ENGLISH));
    }

    @Override
    public CompletableFuture<Suggestions> listSuggestions(CommandContext context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(EXAMPLES, builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public boolean isCollection() {
        return isCollection;
    }

    public Class<?> getEnum() {
        return clazz;
    }
}
