package com.matyrobbrt.weirdsnowballs;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

@Mod(WeirdSnowballs.MOD_ID)
public class WeirdSnowballs {
    public static final String MOD_ID = "weirdsnowballs";
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, MOD_ID);
    public static final Map<MobEffectCategory, Holder<Item>> ITEMS_BY_CATEGORY;
    public static final Map<MobEffectCategory, DeferredHolder<EntityType<?>, EntityType<WeirdSnowball>>> ENTITIES_BY_CATEGORY;

    static {
        var map = new EnumMap<MobEffectCategory, Holder<Item>>(MobEffectCategory.class);
        var entityMap = new EnumMap<MobEffectCategory, DeferredHolder<EntityType<?>, EntityType<WeirdSnowball>>>(MobEffectCategory.class);

        for (MobEffectCategory value : MobEffectCategory.values()) {
            var type = ENTITY_TYPES.register(value.name().toLowerCase(Locale.ROOT) + "_snowball", key ->
                    EntityType.Builder.<WeirdSnowball>of((entityType, level) -> new WeirdSnowball(entityType, level, value), MobCategory.MISC).sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10)
                        .build(key.toString()));
            entityMap.put(value, type);
            map.put(value, ITEMS.register(value.name().toLowerCase(Locale.ROOT) + "_snowball", () ->
                    new WeirdSnowballItem(
                            new Item.Properties()
                                    .rarity(switch (value) {
                                        case HARMFUL -> Rarity.RARE;
                                        case NEUTRAL -> Rarity.UNCOMMON;
                                        case BENEFICIAL -> Rarity.EPIC;
                                    })
                                    .stacksTo(16),
                            value,
                            type
                    )));
        }

        ITEMS_BY_CATEGORY = Collections.unmodifiableMap(map);
        ENTITIES_BY_CATEGORY = Collections.unmodifiableMap(entityMap);
    }

    public WeirdSnowballs(IEventBus bus) {
        ITEMS.register(bus);
        ENTITY_TYPES.register(bus);

        bus.addListener((final FMLCommonSetupEvent event) -> event.enqueueWork(() -> {
            for (Holder<Item> value : ITEMS_BY_CATEGORY.values()) {
                DispenserBlock.registerProjectileBehavior(value.value());
            }
        }));

        if (!FMLEnvironment.production) {
            datagen(bus);
        }
    }

    private void datagen(IEventBus bus) {
        bus.addListener((final GatherDataEvent event) -> {
            event.getGenerator().addProvider(event.includeClient(), new ItemModelProvider(event.getGenerator().getPackOutput(), MOD_ID, event.getExistingFileHelper()) {
                @Override
                protected void registerModels() {
                    ITEMS_BY_CATEGORY.values().forEach(h -> basicItem(h.value()));
                }
            });

            event.getGenerator().addProvider(event.includeClient(), new LanguageProvider(event.getGenerator().getPackOutput(), MOD_ID, "en_us") {
                @Override
                protected void addTranslations() {
                    ITEMS_BY_CATEGORY.forEach((cat, item) -> add(item.value(), getCategory(cat) + " Effect Snowball"));
                    ENTITIES_BY_CATEGORY.forEach((cat, et) -> add(et.value(), getCategory(cat) + " Effect Snowball"));
                }

                private String getCategory(MobEffectCategory cat) {
                    return switch (cat) {
                        case HARMFUL -> "Harmful";
                        case BENEFICIAL -> "Beneficial";
                        case NEUTRAL -> "Neutral";
                    };
                }
            });

            event.getGenerator().addProvider(event.includeServer(), new RecipeProvider(event.getGenerator().getPackOutput(), event.getLookupProvider()) {
                @Override
                protected void buildRecipes(RecipeOutput recipeOutput) {
                    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ITEMS_BY_CATEGORY.get(MobEffectCategory.NEUTRAL).value(), 8)
                            .pattern("SSS").pattern("SWS").pattern("SSS")
                            .define('S', Items.SNOWBALL)
                            .define('W', Tags.Items.CROPS_NETHER_WART)
                            .unlockedBy("has_snowball", has(Items.SNOWBALL))
                            .save(recipeOutput);

                    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ITEMS_BY_CATEGORY.get(MobEffectCategory.BENEFICIAL).value(), 8)
                            .pattern("SSS").pattern("SCS").pattern("SSS")
                            .define('S', ITEMS_BY_CATEGORY.get(MobEffectCategory.NEUTRAL).value())
                            .define('C', Items.MAGMA_CREAM)
                            .unlockedBy("has_snowball", has(Items.SNOWBALL))
                            .save(recipeOutput);
                    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ITEMS_BY_CATEGORY.get(MobEffectCategory.HARMFUL).value(), 8)
                            .pattern("SSS").pattern("SES").pattern("SSS")
                            .define('S', ITEMS_BY_CATEGORY.get(MobEffectCategory.NEUTRAL).value())
                            .define('E', Items.FERMENTED_SPIDER_EYE)
                            .unlockedBy("has_snowball", has(Items.SNOWBALL))
                            .save(recipeOutput);
                }
            });
        });
    }
}
