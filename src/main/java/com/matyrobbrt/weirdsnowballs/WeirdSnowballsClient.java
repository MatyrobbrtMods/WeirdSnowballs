package com.matyrobbrt.weirdsnowballs;

import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@Mod(value = WeirdSnowballs.MOD_ID, dist = Dist.CLIENT)
public class WeirdSnowballsClient {
    public WeirdSnowballsClient(IEventBus bus) {
        bus.addListener((final EntityRenderersEvent.RegisterRenderers event) -> WeirdSnowballs.ENTITIES_BY_CATEGORY.forEach((cat, holder) -> event.registerEntityRenderer(holder.value(), ThrownItemRenderer::new)));
    }
}
