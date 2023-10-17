package me.paulf.wings.client;

import com.mojang.blaze3d.vertex.PoseStack;
import me.paulf.wings.WingsMod;
import me.paulf.wings.client.audio.WingsSound;
import me.paulf.wings.client.flight.FlightView;
import me.paulf.wings.client.flight.FlightViews;
import me.paulf.wings.server.asm.AnimatePlayerModelEvent;
import me.paulf.wings.server.asm.ApplyPlayerRotationsEvent;
import me.paulf.wings.server.asm.EmptyOffHandPresentEvent;
import me.paulf.wings.server.asm.GetCameraEyeHeightEvent;
import me.paulf.wings.server.flight.Flights;
import me.paulf.wings.util.Maath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import com.mojang.math.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = WingsMod.ID)
public final class ClientEventHandler {
    private ClientEventHandler() {
    }

    @SubscribeEvent
    public static void onAnimatePlayerModel(AnimatePlayerModelEvent event) {
        Player player = event.getEntity();
        Flights.get(player).ifPresent(flight -> {
            float delta = event.getTicksExisted() - player.tickCount;
            float amt = flight.getFlyingAmount(delta);
            if (amt == 0.0F) return;
            PlayerModel<?> model = event.getModel();
            float pitch = event.getPitch();
            model.head.xRot = Maath.toRadians(Maath.lerp(pitch, pitch / 4.0F - 90.0F, amt));
            model.leftArm.xRot = Maath.lerp(model.leftArm.xRot, -3.2F, amt);
            model.rightArm.xRot = Maath.lerp(model.rightArm.xRot, -3.2F, amt);
            model.leftLeg.xRot = Maath.lerp(model.leftLeg.xRot, 0.0F, amt);
            model.rightLeg.xRot = Maath.lerp(model.rightLeg.xRot, 0.0F, amt);
            model.hat.copyFrom(model.head);
        });
    }

    @SubscribeEvent
    public static void onApplyRotations(ApplyPlayerRotationsEvent event) {
        Flights.ifPlayer(event.getEntity(), (player, flight) -> {
            PoseStack matrixStack = event.getMatrixStack();
            float delta = event.getDelta();
            float amt = flight.getFlyingAmount(delta);
            if (amt > 0.0F) {
                float roll = Maath.lerpDegrees(
                    player.yBodyRotO - player.yRotO,
                    player.yBodyRot - player.getYRot(),
                    delta
                );
                float pitch = -Maath.lerpDegrees(player.xRotO, player.getXRot(), delta) - 90.0F;
                matrixStack.mulPose(Vector3f.ZP.rotationDegrees(Maath.lerpDegrees(0.0F, roll, amt)));
                matrixStack.mulPose(Vector3f.XP.rotationDegrees(Maath.lerpDegrees(0.0F, pitch, amt)));
                matrixStack.translate(0.0D, -1.2D * Maath.easeInOut(amt), 0.0D);
            }
        });
    }

    @SubscribeEvent
    public static void onGetCameraEyeHeight(GetCameraEyeHeightEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof LocalPlayer) {
            FlightViews.get((LocalPlayer) entity).ifPresent(flight ->
                flight.tickEyeHeight(event.getValue(), event::setValue)
            );
        }
    }

    @SubscribeEvent
    public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
        Flights.ifPlayer(Minecraft.getInstance().cameraEntity, (player, flight) -> {
            float delta = (float) event.getPartialTick();
            float amt = flight.getFlyingAmount(delta);
            if (amt > 0.0F) {
                float roll = Maath.lerpDegrees(
                    player.yBodyRotO - player.yRotO,
                    player.yBodyRot - player.getYRot(),
                    delta
                );
                event.setRoll(Maath.lerpDegrees(0.0F, -roll * 0.25F, amt));
            }
        });
    }

    @SubscribeEvent
    public static void onEmptyOffHandPresentEvent(EmptyOffHandPresentEvent event) {
        Flights.get(event.getPlayer()).ifPresent(flight -> {
            if (flight.isFlying()) {
                event.setResult(Event.Result.ALLOW);
            }
        });
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        Flights.ifPlayer(event.getEntity(), Player::isLocalPlayer, (player, flight) ->
            Minecraft.getInstance().getSoundManager().play(new WingsSound(player, flight))
        );
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player entity = event.player;
        if (event.phase == TickEvent.Phase.END && entity instanceof AbstractClientPlayer) {
            AbstractClientPlayer player = (AbstractClientPlayer) entity;
            FlightViews.get(player).ifPresent(FlightView::tick);
        }
    }
}
