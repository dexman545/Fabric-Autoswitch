package dex.autoswitch.engine.types.data;

import dex.autoswitch.config.codecs.SelectableTypeMarker;
import dex.autoswitch.config.data.tree.ExpressionTree;
import dex.autoswitch.engine.data.Match;
import dex.autoswitch.engine.data.SelectionContext;
import dex.autoswitch.engine.data.extensible.DataType;
import io.leangen.geantyref.TypeToken;

public class EnchantmentData extends DataType<ExpressionTree> {
    public static final EnchantmentData INSTANCE = new EnchantmentData();

    private EnchantmentData() {
        super("enchantments", new TypeToken<@SelectableTypeMarker("enchantment") ExpressionTree>() {});
    }

    @Override
    public Match matches(int baseLevel, SelectionContext context, Object selectable, ExpressionTree data) {
        return data.matches(baseLevel, context, selectable);
    }
}
