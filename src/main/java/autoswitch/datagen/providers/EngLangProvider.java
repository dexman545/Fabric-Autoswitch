package autoswitch.datagen.providers;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import autoswitch.config.AutoSwitchConfig;
import autoswitch.config.util.ConfigReflection;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;

import net.minecraft.core.HolderLookup;

public class EngLangProvider extends FabricLanguageProvider {
    Pattern WORD_FINDER = Pattern.compile("(([A-Z]?[a-z]+)|([A-Z]))");

    public EngLangProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(output, registryLookup);
    }

    @Override
    public void generateTranslations(HolderLookup.Provider registryLookup, TranslationBuilder translationBuilder) {
        //translationBuilder.add("", "");

        buildFeatureConfigTranslations(translationBuilder);

        // Load an existing language file.
        try {
            Path existingFilePath =
                    dataOutput.getModContainer().findPath("assets/aswitch/lang/en_us.json").get();
            translationBuilder.add(existingFilePath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to add existing language file!", e);
        }
    }

    private void buildFeatureConfigTranslations(TranslationBuilder translationBuilder) {
        for (Method method : AutoSwitchConfig.class.getDeclaredMethods()) {
            var key = ConfigReflection.translationKey(method);

            // Option title
            translationBuilder.add("title.autoswitch." + key, sentenceCase(findWordsInMixedCase(key)));
            // Option current value
            translationBuilder.add("currently.autoswitch." + key, sentenceCase(findWordsInMixedCase(key)) + ": %s");
        }
    }

    private String sentenceCase(List<String> words) {
        List<String> capitalized = new ArrayList<>();
        for (int i = 0; i < words.size(); i++) {
            String currentWord = words.get(i);
            if (i == 0) {
                capitalized.add(capitalizeFirst(currentWord));
            } else {
                capitalized.add(currentWord.toLowerCase());
            }
        }
        return String.join(" ", capitalized);
    }

    private String capitalizeFirst(String word) {
        return word.substring(0, 1).toUpperCase()
               + word.substring(1).toLowerCase();
    }

    public List<String> findWordsInMixedCase(String text) {
        Matcher matcher = WORD_FINDER.matcher(text);
        List<String> words = new ArrayList<>();
        while (matcher.find()) {
            words.add(matcher.group(0));
        }
        return words;
    }
}
