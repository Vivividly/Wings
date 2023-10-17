package me.paulf.wings.server.dreamcatcher;

import me.paulf.wings.WingsMod;
import me.paulf.wings.client.flight.FlightView;
import me.paulf.wings.util.CapabilityHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = WingsMod.ID)
public final class InSomniableCapability {
    private InSomniableCapability() {
    }

    private static final CapabilityHolder<Player, InSomniable, CapabilityHolder.State<Player, InSomniable>> HOLDER = CapabilityHolder.create();
    private static final Capability<InSomniable> INSOMNIABLE = CapabilityManager.get(new CapabilityToken<>() {});

    public static LazyOptional<InSomniable> getInSomniable(Player player) {
        return HOLDER.state().get(player, null);
    }

//    @CapabilityInject(InSomniable.class)
//    static void injectInSomniable(Capability<InSomniable> capability) {
//        INSOMNIABLE.inject(capability);
//    }

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(InSomniable.class);
        HOLDER.inject(INSOMNIABLE);
    }

    @SubscribeEvent
    public static void onAttachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
        Entity entity = event.getObject();
        if (entity instanceof Player) {
            event.addCapability(
                new ResourceLocation(WingsMod.ID, "insomniable"),
                HOLDER.state().providerBuilder(new InSomniable())
                    .serializedBy(new InSomniable.Serializer())
                    .build()
            );
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        getInSomniable(event.getOriginal())
            .ifPresent(oldInstance -> getInSomniable(event.getEntity())
                .ifPresent(newInstance -> newInstance.clone(oldInstance))
            );
    }
}
