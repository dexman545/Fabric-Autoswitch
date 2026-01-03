package dex.autoswitch.platform;

import java.util.ServiceLoader;

import dex.autoswitch.Constants;
import dex.autoswitch.platform.services.IPlatformHelper;

public class Services {
    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);

    public static <T> T load(Class<T> clazz) {
        // Prefer test platforms
        T loadedService = null;
        for (T loaded : ServiceLoader.load(clazz, Services.class.getClassLoader())) {
            if (loadedService != null) {
                Constants.LOG.warn("Found duplicate platform service {}", loaded.getClass());
            } else {
                loadedService = loaded;

                if (loadedService.getClass().getName().contains("Test")) {
                    break;
                }
            }
        }

        if (loadedService == null) {
            throw new NullPointerException("Failed to load service for " + clazz.getName());
        }

        Constants.LOG.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}
