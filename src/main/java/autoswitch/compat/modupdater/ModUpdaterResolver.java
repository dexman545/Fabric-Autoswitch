package autoswitch.compat.modupdater;

import com.thebrokenrail.modupdater.api.entrypoint.ModUpdaterEntryPoint;
import net.fabricmc.loader.api.FabricLoader;

import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModUpdaterResolver  implements ModUpdaterEntryPoint {
    @Override
    public boolean isVersionCompatible(String version) {
        AtomicReference<String> mcVersion = new AtomicReference<>();

        FabricLoader.getInstance().getModContainer("minecraft").ifPresent(modContainer ->
                mcVersion.set(modContainer.getMetadata().getVersion().getFriendlyString())
        );

        // cg1 = major, cg2 = minor, cg3 = patch, cg4 = prerelease and cg5 = buildmetadata
        Pattern versionPattern = Pattern.compile("^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$");

        Matcher matcherMC = versionPattern.matcher(mcVersion.get());
        Matcher matcherAS = versionPattern.matcher(version);

        int as = 0;
        int mc = 0;
        if (matcherAS.matches() && matcherMC.matches()) {
            mc = Integer.parseInt(matcherMC.group(2));
            as = Integer.parseInt(matcherAS.group(1));
        }


        //TODO keep this up to date
        return ((mc == 15 || mc == 14) && (as == 1)) || ((mc >= 16) && (as == 2));
    }
}
