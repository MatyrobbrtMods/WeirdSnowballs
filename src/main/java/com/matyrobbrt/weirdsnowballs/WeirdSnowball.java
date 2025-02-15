package com.matyrobbrt.weirdsnowballs;

import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class WeirdSnowball extends Snowball {
    private static final int MIN_EFFECT_SECONDS = 1, MAX_EFFECT_SECONDS = 10,
            MIN_EFFECT_LEVEL = 0, MAX_EFFECT_LEVEL = 4;

    private final MobEffectCategory category;

    public WeirdSnowball(EntityType<? extends Snowball> entityType, Level level, MobEffectCategory category) {
        super(entityType, level);
        this.category = category;
    }

    @Override
    protected Item getDefaultItem() {
        return category == null ? Items.SNOWBALL : WeirdSnowballs.ITEMS_BY_CATEGORY.get(this.category).value();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (result.getEntity() instanceof LivingEntity living) {
            var possibilities = result.getEntity().level().registryAccess().lookupOrThrow(Registries.MOB_EFFECT)
                    .listElements()
                    .filter(r -> r.value().getCategory() == category)
                    .toList();

            var rand = result.getEntity().getRandom();
            var effect = Util.getRandom(possibilities, rand);

            var tickRate = Mth.ceil(result.getEntity().level().tickRateManager().tickrate());

            living.addEffect(new MobEffectInstance(
                    effect,
                    rand.nextIntBetweenInclusive(MIN_EFFECT_SECONDS * tickRate, MAX_EFFECT_SECONDS * tickRate),
                    rand.nextIntBetweenInclusive(MIN_EFFECT_LEVEL, MAX_EFFECT_LEVEL)
            ), this);
        }
    }
}
