package me.paulf.wings.client.flight;

import me.paulf.wings.WingsMod;
import me.paulf.wings.server.flight.AttachFlightCapabilityEvent;
import me.paulf.wings.util.CapabilityHolder;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = WingsMod.ID, value = Dist.CLIENT)
public final class FlightViews {
    private FlightViews() {
    }

    private static final CapabilityHolder<LivingEntity, FlightView, CapabilityHolder.State<LivingEntity, FlightView>> HOLDER = CapabilityHolder.create();
    private static final Capability<FlightView> FLIGHT_VIEW = CapabilityManager.get(new CapabilityToken<>() {});

    public static boolean has(LivingEntity player) {
        return HOLDER.state().has(player, null);
    }

    public static LazyOptional<FlightView> get(LivingEntity player) {
        return HOLDER.state().get(player, null);
    }

//    @CapabilityInject(FlightView.class)
//    static void inject(Capability<FlightView> capability) {
//        HOLDER.inject(capability);
//    }

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(FlightView.class);
        HOLDER.inject(FLIGHT_VIEW);
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachFlightCapabilityEvent event) {
        Entity entity = event.getObject();
        if (entity instanceof AbstractClientPlayer) {
            event.addCapability(
                new ResourceLocation(WingsMod.ID, "flight_view"),
                HOLDER.state().providerBuilder(new FlightViewDefault((Player) entity, event.getInstance())).build()
            );
        }
    }
}
