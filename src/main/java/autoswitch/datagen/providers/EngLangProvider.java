package autoswitch.datagen.providers;

import java.nio.file.Path;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;

public class EngLangProvider extends FabricLanguageProvider {
    public EngLangProvider(FabricDataOutput output) {
        super(output, "en_us");
    }

    @Override
    public void generateTranslations(TranslationBuilder translationBuilder) {
        //translationBuilder.add("", "");

        // Load an existing language file.
        try {
            Path existingFilePath =
                    dataOutput.getModContainer().findPath("assets/aswitch/lang/en_us.json").get();
            translationBuilder.add(existingFilePath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to add existing language file!", e);
        }
    }
}
