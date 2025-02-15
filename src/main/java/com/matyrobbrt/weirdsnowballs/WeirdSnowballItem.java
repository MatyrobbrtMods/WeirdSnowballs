package com.matyrobbrt.weirdsnowballs;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SnowballItem;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

public class WeirdSnowballItem extends SnowballItem {
    private final MobEffectCategory category;
    private final Supplier<EntityType<WeirdSnowball>> type;
    public WeirdSnowballItem(Properties properties, MobEffectCategory category, Supplier<EntityType<WeirdSnowball>> type) {
        super(properties);
        this.category = category;
        this.type = type;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.SNOWBALL_THROW,
                SoundSource.NEUTRAL,
                0.5F,
                0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
        );
        if (!level.isClientSide) {
            Snowball snowball = new WeirdSnowball(type.get(), level, category);
            snowball.setOwner(player);
            snowball.setPos(player.getX(), player.getEyeY() - 0.1F, player.getZ());
            snowball.setItem(itemstack);
            snowball.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            level.addFreshEntity(snowball);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        itemstack.consume(1, player);
        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }

    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        Snowball snowball = new WeirdSnowball(type.get(), level, category);
        snowball.setPos(pos.x(), pos.y(), pos.z());
        snowball.setItem(stack);
        return snowball;
    }
}
