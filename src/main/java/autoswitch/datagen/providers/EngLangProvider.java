package autoswitch.datagen.providers;

import java.nio.file.Path;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;

public class EngLangProvider extends FabricLanguageProvider {
    public EngLangProvider(FabricDataGenerator dataGenerator) {
        super(dataGenerator, "en_us");
    }

    @Override
    public void generateTranslations(TranslationBuilder translationBuilder) {
        //translationBuilder.add("", "");

        // Load an existing language file.
        try {
            Path existingFilePath =
                    dataGenerator.getModContainer().findPath("assets/aswitch/lang/en_us.json").get();
            translationBuilder.add(existingFilePath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to add existing language file!", e);
        }
    }
}
