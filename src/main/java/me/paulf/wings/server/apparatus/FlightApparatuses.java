package me.paulf.wings.server.apparatus;

import me.paulf.wings.WingsMod;
import me.paulf.wings.server.config.WingsConfig;
import me.paulf.wings.util.CapabilityHolder;
import me.paulf.wings.util.CapabilityProviders;
import me.paulf.wings.util.HandlerSlot;
import me.paulf.wings.util.Util;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;
import java.util.Arrays;

public final class FlightApparatuses {
	private FlightApparatuses() {}

	private static final CapabilityHolder<ItemStack, FlightApparatus, WingedState> HOLDER = CapabilityHolder.create(WingedAbsentState::new, WingedPresentState::new);

	public static boolean has(final ItemStack stack) {
		return HOLDER.state().has(stack, null);
	}

	public static LazyOptional<FlightApparatus> get(final ItemStack stack) {
		return HOLDER.state().get(stack, null);
	}

	public static <T extends FlightApparatus> CapabilityProviders.NonSerializingSingleBuilder<T> providerBuilder(final T instance) {
		return HOLDER.state().providerBuilder(instance);
	}

	public static ItemStack find(final LivingEntity player) {
		return HOLDER.state().find(player);
	}

	@CapabilityInject(FlightApparatus.class)
	static void inject(final Capability<FlightApparatus> capability) {
		HOLDER.inject(capability);
	}

	private interface WingedState extends CapabilityHolder.State<ItemStack, FlightApparatus> {
		ItemStack find(LivingEntity player);
	}

	private static final class WingedAbsentState extends CapabilityHolder.AbsentState<ItemStack, FlightApparatus> implements WingedState {
		@Override
		public ItemStack find(final LivingEntity player) {
			return ItemStack.EMPTY;
		}
	}

	private static final class WingedPresentState extends CapabilityHolder.PresentState<ItemStack, FlightApparatus> implements WingedState {
		private WingedPresentState(final Capability<FlightApparatus> capability) {
			super(capability);
		}

		@Override
		public ItemStack find(final LivingEntity player) {
			for (final HandlerSlot slot : WingsMod.instance().getWingsAccessor().enumerate(player)) {
				final ItemStack stack = slot.get();
				if (this.has(stack, null)) {
					return stack;
				}
				if (!stack.isEmpty() && Arrays.asList(WingsConfig.wearObstructions).contains(Util.getName(stack.getItem()).toString())) {
					break;
				}
			}
			return ItemStack.EMPTY;
		}
	}
}
