package me.paulf.wings.server.item;

import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import baubles.api.IBauble;
import baubles.api.cap.IBaublesItemHandler;
import baubles.api.render.IRenderBauble;
import me.paulf.wings.WingsMod;
import me.paulf.wings.server.capability.Flight;
import me.paulf.wings.server.capability.FlightCapability;
import me.paulf.wings.server.sound.WingsSounds;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

public final class ItemWings extends Item implements IBauble, IRenderBauble {
	private ItemWings() {}

	public ItemStack createStack(StandardWing type) {
		return new ItemStack(this, 1, type.getMeta());
	}

	@Override
	public BaubleType getBaubleType(ItemStack stack) {
		return BaubleType.BODY;
	}

	@Override
	public void onWornTick(ItemStack stack, EntityLivingBase entity) {
		FlightCapability.ifPlayer(entity, (player, flight) -> flight.onWornUpdate(player));
	}

	@Override
	public void onEquipped(ItemStack itemstack, EntityLivingBase player) {
		player.playSound(WingsSounds.ITEM_ARMOR_EQIIP_WINGS, 1.0F, 1.0F);
	}

	@Override
	public void onUnequipped(ItemStack stack, EntityLivingBase entity) {
		FlightCapability.ifPlayer(entity, e -> !e.world.isRemote, (player, flight) ->
			flight.setIsFlying(false, Flight.PlayerSet.ofAll())
		);
	}

	@Override
	public void onPlayerBaubleRender(ItemStack stack, EntityPlayer player, RenderType type, float delta) {
		WingsMod.instance().renderWings(stack, player, type, delta);
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		//noinspection deprecation
		return I18n.translateToLocalFormatted(
			super.getItemStackDisplayName(stack),
			I18n.translateToLocal(StandardWing.fromMeta(stack).getTranslationKey())
		);
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		if (isInCreativeTab(tab)) {
			StandardWing.stream().forEach(t -> items.add(createStack(t)));
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		IBaublesItemHandler handler = BaublesApi.getBaublesHandler(player);
		for (int slot : getBaubleType(stack).getValidSlots()) {
			if (handler.getStackInSlot(slot).isEmpty() && handler.isItemValidForSlot(slot, stack, player)) {
				ItemStack copy = stack.copy();
				handler.setStackInSlot(slot, copy);
				onEquipped(copy, player);
				stack.setCount(0);
				return new ActionResult<>(EnumActionResult.SUCCESS, stack);
			}
		}
		return new ActionResult<>(EnumActionResult.FAIL, stack);
	}

	public static ItemWings create() {
		ItemWings item = new ItemWings();
		item.setMaxStackSize(1);
		item.setHasSubtypes(true);
		return item;
	}

	static ItemWings nil() {
		return new ItemWings();
	}
}