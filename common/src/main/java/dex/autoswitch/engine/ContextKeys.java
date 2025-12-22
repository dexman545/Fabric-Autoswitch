package dex.autoswitch.engine;

import dex.autoswitch.engine.data.ContextKey;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

public class ContextKeys {
    public static final ContextKey<BlockPos> BLOCK_POS = ContextKey.create("blockPos", BlockPos.class);
    public static final ContextKey<Player> PLAYER = ContextKey.create("player", Player.class);

    private ContextKeys() {}
}
