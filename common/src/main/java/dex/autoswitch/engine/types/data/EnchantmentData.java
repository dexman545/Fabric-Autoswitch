package dex.autoswitch.engine.types.data;

import dex.autoswitch.config.data.tree.Data;
import dex.autoswitch.config.data.tree.ExpressionTree;
import dex.autoswitch.engine.Matcher;
import dex.autoswitch.engine.data.Match;
import dex.autoswitch.engine.data.SelectionContext;
import dex.autoswitch.engine.data.extensible.DataType;

public class EnchantmentData extends DataType<ExpressionTree> {
    public static final EnchantmentData INSTANCE = new EnchantmentData();

    private EnchantmentData() {
        super("enchantments", ExpressionTree.class);
    }

    @Override
    public Match matches(int baseLevel, SelectionContext context, Object selectable, Data data) {
        if (data instanceof Matcher matcher) {
            return matcher.matches(baseLevel, context, selectable);
        }

        return new Match(false);
    }
}
