package bone_meal_sugar_cane;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldEvents;

public class BoneMealSugarCane implements ModInitializer {
    /** Sugar cane naturally stops growing at 3 blocks tall. */
    private static final int MAX_HEIGHT = 3;

    @Override
    public void onInitialize() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            ItemStack stack = player.getStackInHand(hand);
            BlockPos clicked = hitResult.getBlockPos();

            if (player.isSpectator()
                    || !stack.isOf(Items.BONE_MEAL)
                    || !world.getBlockState(clicked).isOf(Blocks.SUGAR_CANE)) {
                return ActionResult.PASS;
            }

            // Measure the whole stack, from its base to its top.
            BlockPos top = clicked;
            while (world.getBlockState(top.up()).isOf(Blocks.SUGAR_CANE)) {
                top = top.up();
            }
            int height = 1;
            BlockPos base = top;
            while (world.getBlockState(base.down()).isOf(Blocks.SUGAR_CANE)) {
                base = base.down();
                height++;
            }

            BlockPos above = top.up();
            if (height >= MAX_HEIGHT || !world.getBlockState(above).isAir()) {
                return ActionResult.FAIL;
            }

            if (world.isClient) {
                return ActionResult.SUCCESS; // server does the real work
            }

            world.setBlockState(above, Blocks.SUGAR_CANE.getDefaultState());
            world.syncWorldEvent(WorldEvents.BONE_MEAL_USED, above, 0);
            if (!player.getAbilities().creativeMode) {
                stack.decrement(1);
            }
            return ActionResult.SUCCESS;
        });
    }
}
