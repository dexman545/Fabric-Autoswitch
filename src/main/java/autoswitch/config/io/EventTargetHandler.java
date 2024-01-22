package autoswitch.config.io;

import java.util.regex.Pattern;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;

public class EventTargetHandler {
    private static final Pattern groupPattern = Pattern.compile("(\\w+@\\w+:\\w+)");

    private static boolean isTaggedMatch(String str) {
        return groupPattern.matcher(str).matches();
    }

    public static Object getTargetableEvent(String str) {
        if (isTaggedMatch(str)) {
            var eventType = EventType.getType(str);
            if (eventType != null) {
                return eventType.makeTarget(str);
            }
        }

        return null;
    }

    public enum EventType {
        STAT_CHANGE("stat") {
            @Override
            public Object makeTarget(String event) {
                var key = event.split("@")[1];

                var split = key.split(":");

                if (split.length != 2) return null;

                return getStat(new ResourceLocation(split[0].replace(".", ":")),
                               new ResourceLocation(split[1].replace(".", ":")));
            }

            //todo use FRE? use RegistryHolders?
            public <T> Stat<?> getStat(ResourceLocation type, ResourceLocation name) {
                var maybeStatType = BuiltInRegistries.STAT_TYPE.getOptional(type);
                if (maybeStatType.isPresent()) {
                    var statType = (StatType<T>) maybeStatType.get();
                    var maybeObject = statType.getRegistry().getOptional(name);
                    if (maybeObject.isPresent()) {
                        return statType.get(maybeObject.get());
                    }
                }

                return null;
            }
        };

        private final String pattern;

        EventType(String pattern) {
            this.pattern = pattern;
        }

        public abstract Object makeTarget(String event);

        public static EventType getType(String possibleEvent) {
            for (EventType eventType : values()) {
                if (possibleEvent.startsWith(eventType.pattern)) return eventType;
            }

            return null;
        }
    }
}
