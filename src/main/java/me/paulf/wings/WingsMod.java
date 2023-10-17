package me.paulf.wings;

import com.mojang.serialization.Lifecycle;
import me.paulf.wings.client.ClientProxy;
import me.paulf.wings.server.ServerProxy;
import me.paulf.wings.server.apparatus.FlightApparatus;
import me.paulf.wings.server.apparatus.SimpleFlightApparatus;
import me.paulf.wings.server.config.WingsItemsConfig;
import me.paulf.wings.server.effect.WingsEffects;
import me.paulf.wings.server.flight.Flight;
import me.paulf.wings.server.item.WingsItems;
import me.paulf.wings.server.sound.WingsSounds;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

@Mod(WingsMod.ID)
public final class WingsMod {
    public static final String ID = "wings";

    private static WingsMod INSTANCE;

//    public static final Registry<FlightApparatus> WINGS = new DefaultedRegistry<>(Names.NONE.toString(), ResourceKey.createRegistryKey(new ResourceLocation(ID, "wings")), Lifecycle.experimental());
    public static final DeferredRegister<FlightApparatus> WINGS = DeferredRegister.create(new ResourceLocation(ID, "wings"), ID);
    public static final Supplier<IForgeRegistry<FlightApparatus>> WINGS_REGISTRY = WINGS.makeRegistry(RegistryBuilder::new);

    public static final RegistryObject<FlightApparatus> NONE_WINGS = WINGS.register(Names.NONE.getPath(), () -> FlightApparatus.NONE);
    public static final RegistryObject<FlightApparatus> ANGEL_WINGS = WINGS.register(Names.ANGEL.getPath(), () -> new SimpleFlightApparatus(WingsItemsConfig.ANGEL));
	public static final RegistryObject<FlightApparatus> PARROT_WINGS = WINGS.register(Names.PARROT.getPath(), () -> new SimpleFlightApparatus(WingsItemsConfig.PARROT));
    public static final RegistryObject<FlightApparatus> BAT_WINGS = WINGS.register(Names.BAT.getPath(), () -> new SimpleFlightApparatus(WingsItemsConfig.BAT));
    public static final RegistryObject<FlightApparatus> BLUE_BUTTERFLY_WINGS = WINGS.register(Names.BLUE_BUTTERFLY.getPath(), () -> new SimpleFlightApparatus(WingsItemsConfig.BLUE_BUTTERFLY));
    public static final RegistryObject<FlightApparatus> DRAGON_WINGS = WINGS.register(Names.DRAGON.getPath(), () -> new SimpleFlightApparatus(WingsItemsConfig.DRAGON));
    public static final RegistryObject<FlightApparatus> EVIL_WINGS = WINGS.register(Names.EVIL.getPath(), () -> new SimpleFlightApparatus(WingsItemsConfig.EVIL));
    public static final RegistryObject<FlightApparatus> FAIRY_WINGS = WINGS.register(Names.FAIRY.getPath(), () -> new SimpleFlightApparatus(WingsItemsConfig.FAIRY));
    public static final RegistryObject<FlightApparatus> MONARCH_BUTTERFLY_WINGS = WINGS.register(Names.MONARCH_BUTTERFLY.getPath(), () -> new SimpleFlightApparatus(WingsItemsConfig.MONARCH_BUTTERFLY));
    public static final RegistryObject<FlightApparatus> SLIME_WINGS = WINGS.register(Names.SLIME.getPath(), () -> new SimpleFlightApparatus(WingsItemsConfig.SLIME));
    public static final RegistryObject<FlightApparatus> FIRE_WINGS = WINGS.register(Names.FIRE.getPath(), () -> new SimpleFlightApparatus(WingsItemsConfig.FIRE));

    private Proxy proxy;

    public WingsMod() {
        if (INSTANCE != null) throw new IllegalStateException("Already constructed!");
        INSTANCE = this;
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        WingsItems.REG.register(bus);
        WingsSounds.REG.register(bus);
        WingsEffects.REG.register(bus);
        WINGS.register(bus);
        this.proxy = DistExecutor.safeRunForDist(() -> ProxyInit::createClient, () -> ProxyInit::createServer);
        this.proxy.init(bus);
    }

    static class ProxyInit {
        static Proxy createClient() {
            return new ClientProxy();
        }

        static Proxy createServer() {
            return new ServerProxy();
        }
    }

    public void addFlightListeners(Player player, Flight instance) {
        this.requireProxy().addFlightListeners(player, instance);
    }

    public static WingsMod instance() {
        return INSTANCE;
    }

    private Proxy requireProxy() {
        if (this.proxy == null) {
            throw new IllegalStateException("Proxy not initialized");
        }
        return this.proxy;
    }

    public static final class Names {
        private Names() {
        }

        public static final ResourceLocation
            NONE = create("none"),
            ANGEL = create("angel_wings"),
            PARROT = create("parrot_wings"),
            SLIME = create("slime_wings"),
            BLUE_BUTTERFLY = create("blue_butterfly_wings"),
            MONARCH_BUTTERFLY = create("monarch_butterfly_wings"),
            FIRE = create("fire_wings"),
            BAT = create("bat_wings"),
            FAIRY = create("fairy_wings"),
            EVIL = create("evil_wings"),
            DRAGON = create("dragon_wings");

        private static ResourceLocation create(String path) {
            return new ResourceLocation(ID, path);
        }
    }
}
