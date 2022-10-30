package autoswitch.config.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.command.CommandSource;

public class GenericRepeatingArgumentType<U, T extends ArgumentType<U>> implements ArgumentType<Collection<U>> {
    private final T argType;

    public GenericRepeatingArgumentType(T argType) {
        this.argType = argType;
    }

    @Override
    public Collection<U> parse(StringReader reader) throws CommandSyntaxException {
        var out = new ArrayList<U>();
        while (reader.canRead()) {
            out.add(argType.parse(new StringReader(reader.readStringUntil(' '))));
        }

        return out;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        if (!(context.getSource() instanceof CommandSource)) {
            return Suggestions.empty();
        } else {
            return CommandSource.suggestMatching(getExamples(), builder);
        }
    }

    @Override
    public Collection<String> getExamples() {
        //todo listify?
        return argType.getExamples();
    }

}
