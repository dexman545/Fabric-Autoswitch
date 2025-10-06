package dex.autoswitch.engine.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.ServiceLoader;

import dex.autoswitch.config.data.tree.Data;
import dex.autoswitch.engine.Matcher;
import dex.autoswitch.engine.data.extensible.DataType;
import dex.autoswitch.engine.data.extensible.SelectableType;
import dex.autoswitch.engine.data.extensible.SwitchRegistryService;
import org.jetbrains.annotations.Nullable;

/**
 * The various types should be registered via a {@link SwitchRegistryService}.
 */
public class SwitchRegistry {
    public static final SwitchRegistry INSTANCE = new SwitchRegistry();
    public final Matcher nonToolMatcher;
    private final HashMap<String, SelectableType<?, ?, ?>> SELECTABLE_TYPES = new HashMap<>();
    private final HashMap<String, DataType<?>> DATA_TYPES = new HashMap<>();

    private SwitchRegistry() {
        Matcher nonTool = null;

        var loader = ServiceLoader.load(SwitchRegistryService.class, SwitchRegistry.class.getClassLoader());
        for (SwitchRegistryService registryService : loader) {
            var selTypes = registryService.selectableTypes();
            for (SelectableType<?, ?, ?> selType : selTypes) {
                if (SELECTABLE_TYPES.put(selType.id(), selType) != null) {
                    throw new IllegalArgumentException("Tried to create SelectorType with existing id " + selType.id());
                }
            }

            var dataTypes = registryService.dataTypes();
            for (DataType<?> dataType : dataTypes) {
                if (DATA_TYPES.put(dataType.id(), dataType) != null) {
                    throw new IllegalArgumentException("Tried to create DataType with existing id " + dataType.id());
                }
            }

            var registeredTool = registryService.nonToolMatcher();
            if (nonTool == null) {
                nonTool = registeredTool;
            } else if (registeredTool != null) {
                throw new IllegalArgumentException("Tried to create second blank tool selector");
            }
        }

        if (nonTool == null) {
            nonTool = (i, j, k) -> new Match(false);
        }

        nonToolMatcher = nonTool;
    }

    public <T extends Data> DataType<T> getDataType(String id) {
        Objects.requireNonNull(id);
        id = id.toUpperCase(Locale.ENGLISH);
        var i = DATA_TYPES.get(id);
        if (i != null) {
            //noinspection unchecked
            return (DataType<T>) i;
        }

        throw new IllegalArgumentException("Tried to fetch DataType with invalid id " + id);
    }

    public SelectableType<?, ?, ?> getSelectableType(String id) {
        Objects.requireNonNull(id);
        id = id.toUpperCase(Locale.ENGLISH);
        var i = SELECTABLE_TYPES.get(id);
        if (i != null) {
            return i;
        }

        throw new IllegalArgumentException("Tried to fetch SelectorType with invalid id " + id);
    }

    public @Nullable SelectableType<?, ?, ?> getSelectableType(Object o) {
        for (SelectableType<?, ?, ?> selectableType : SELECTABLE_TYPES.values()) {
            if (selectableType.isOf(o)) {
                return selectableType;
            }
        }

        return null;
    }

    public Collection<SelectableType<?, ?, ?>> getSelectableTypes() {
        return SELECTABLE_TYPES.values();
    }
}
