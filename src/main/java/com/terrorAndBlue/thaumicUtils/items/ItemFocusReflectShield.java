package com.terrorAndBlue.thaumicUtils.items;

import java.util.Iterator;
import java.util.List;

import com.terrorAndBlue.thaumicUtils.ThaumicUtils;
import com.terrorAndBlue.thaumicUtils.handlers.HandlerUtils;
import com.terrorAndBlue.thaumicUtils.network.CreateReflectShieldVisualPacket;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.common.items.wands.ItemWandCasting;

public class ItemFocusReflectShield extends TerrorBaseFocus
{
	public static final FocusUpgradeType REVITALIZING_COUNTER = new FocusUpgradeType(FocusUpgradeType.types.length, new ResourceLocation(ThaumicUtils.MODID+":textures/foci/upgrades/revitalizing_counter.png"),
			"focus.upgrade.revitalizingcounter.name","focus.upgrade.revitalizingcounter.text",
			new AspectList().add(Aspect.HEAL,1).add(Aspect.ORDER,1));
	
	public static final FocusUpgradeType RECKLESS_COUNTER = new FocusUpgradeType(FocusUpgradeType.types.length, new ResourceLocation(ThaumicUtils.MODID+":textures/foci/upgrades/reckless_counter.png"),
			"focus.upgrade.recklesscounter.name","focus.upgrade.recklesscounter.text",
			new AspectList().add(Aspect.BEAST,1).add(Aspect.FIRE,1));
	
	public static final FocusUpgradeType MASTER_OF_THE_ART = new FocusUpgradeType(FocusUpgradeType.types.length, new ResourceLocation(ThaumicUtils.MODID+":textures/foci/upgrades/master_of_the_art.png"),
			"focus.upgrade.masteroftheart.name","focus.upgrade.masteroftheart.text",
			new AspectList().add(Aspect.MAGIC,1).add(Aspect.MIND,1).add(Aspect.MAN,1).add(Aspect.SENSES,1));
	
	public static final FocusUpgradeType SUPERCHARGED_GLYPHS = new FocusUpgradeType(FocusUpgradeType.types.length, new ResourceLocation(ThaumicUtils.MODID+":textures/foci/upgrades/supercharged_glyphs.png"),
			"focus.upgrade.superchargedglyphs.name","focus.upgrade.superchargedglyphs.text",
			new AspectList().add(Aspect.ARMOR,1).add(Aspect.MIND,1).add(Aspect.AIR,1).add(Aspect.LIGHT,1));
	
	public static final FocusUpgradeType ASTRAL_AFFINITY = new FocusUpgradeType(FocusUpgradeType.types.length, new ResourceLocation(ThaumicUtils.MODID+":textures/foci/upgrades/astral_affinity.png"),
			"focus.upgrade.astralaffinity.name", "focus.upgrade.astralaffinity.text",
			new AspectList().add(Aspect.ORDER,1).add(Aspect.MIND,1).add(Aspect.SOUL,1).add(Aspect.DARKNESS,1).add(Aspect.ENTROPY,1));
	
	/**
	 * An array representing the 4 variants of Astral Affinity.
	 * <br>Each element corresponds to a different upgrade path for a given rank-3 upgrade.
	 * <br>The upgrades are Potency, Frugal, Revitalizing Counter, Reckless Counter.
	 */
	/*public static final FocusUpgradeType[] ASTRAL_AFFINITY = new FocusUpgradeType[]
	{
			new FocusUpgradeType(FocusUpgradeType.types.length, new ResourceLocation(ThaumicUtils.MODID+":textures/foci/upgrades/astral_affinity0.png"), "focus.upgrade.astralaffinity0.name", "focus.upgrade.astralaffinity0.text", new AspectList().add(Aspect.ORDER,1).add(Aspect.MIND,1).add(Aspect.SOUL,1).add(Aspect.DARKNESS,1).add(Aspect.ENTROPY,1)),
			new FocusUpgradeType(FocusUpgradeType.types.length, new ResourceLocation(ThaumicUtils.MODID+":textures/foci/upgrades/astral_affinity1.png"), "focus.upgrade.astralaffinity1.name", "focus.upgrade.astralaffinity1.text", new AspectList().add(Aspect.ORDER,1).add(Aspect.MIND,1).add(Aspect.SOUL,1).add(Aspect.DARKNESS,1).add(Aspect.ENTROPY,1)),
			new FocusUpgradeType(FocusUpgradeType.types.length, new ResourceLocation(ThaumicUtils.MODID+":textures/foci/upgrades/astral_affinity2.png"), "focus.upgrade.astralaffinity2.name", "focus.upgrade.astralaffinity2.text", new AspectList().add(Aspect.ORDER,1).add(Aspect.MIND,1).add(Aspect.SOUL,1).add(Aspect.DARKNESS,1).add(Aspect.ENTROPY,1)),
			new FocusUpgradeType(FocusUpgradeType.types.length, new ResourceLocation(ThaumicUtils.MODID+":textures/foci/upgrades/astral_affinity3.png"), "focus.upgrade.astralaffinity3.name", "focus.upgrade.astralaffinity3.text", new AspectList().add(Aspect.ORDER,1).add(Aspect.MIND,1).add(Aspect.SOUL,1).add(Aspect.DARKNESS,1).add(Aspect.ENTROPY,1))
	};*/
	
	
	public static final AspectList VIS_USAGE = new AspectList().add(Aspect.ORDER, 10).add(Aspect.FIRE, 10).add(Aspect.WATER, 10);
	public static final AspectList VIS_USAGE_FREE = new AspectList().add(Aspect.ORDER, 0).add(Aspect.FIRE, 0).add(Aspect.WATER, 0);
	
	private static final AspectList ACTIVATE_MIN_VIS = new AspectList().add(Aspect.ORDER, 100).add(Aspect.FIRE, 100).add(Aspect.WATER, 100);
	
	public ItemFocusReflectShield()
	{
		super();
		this.setUnlocalizedName("wandFocusReflect");
		this.setCreativeTab(CreativeTabs.tabCombat);//TODO TEMPORARY
	}
	
	@Override
	public void registerIcons(IIconRegister reg)
	{
		this.icon = reg.registerIcon(ThaumicUtils.MODID+":wand_focus_reflect");
	}
	
	@Override
	public ItemStack onFocusRightClick(ItemStack stack, World world, EntityPlayer player, MovingObjectPosition mop)
	{
		ItemWandCasting wand = (ItemWandCasting) stack.getItem();
		
		if(!wand.consumeAllVis(stack, player, ACTIVATE_MIN_VIS, false, false))
		{
			if(player.isUsingItem())
			{
				System.out.println("Reflect Focus force-stopped.");
				player.stopUsingItem();
				wand.clearObjectInUse(player.getHeldItem());
				wand.animation = null;
			}
			return stack;
		}
		
		player.setItemInUse(stack, Integer.MAX_VALUE);
		return stack;
	}
	
	@Override
	public void onUsingFocusTick(ItemStack stack, EntityPlayer p, int ticks)
	{
		int ticks_used = Integer.MAX_VALUE - ticks;
		
		ItemWandCasting wand = (ItemWandCasting) stack.getItem();
		
		AspectList visUsage = VIS_USAGE;
		
		if(!p.worldObj.isRemote && isUpgradedWith(wand.getFocusItem(stack), ASTRAL_AFFINITY))
		{
			boolean moonlight = p.worldObj.skylightSubtracted >= 7 && p.worldObj.canBlockSeeTheSky((int)p.posX, (int)p.posY+1, (int)p.posZ);
			
			if(moonlight)
			{
				//int newVisAmount = Math.max(0, (int)(visUsage.getAmount(Aspect.ORDER) * 0.05F * (20 - ticks_used/20)));
				visUsage = new AspectList().add(Aspect.ORDER, 5).add(Aspect.FIRE, 5).add(Aspect.WATER, 5);
				if(wand.getFocusItem(stack).getTagCompound() != null)
				{
					wand.getFocusItem(stack).getTagCompound().setInteger("newVisAmount", 5);
				}
			}
		}

		ItemStack focusStack = wand.getFocusItem(stack);
		if(!wand.consumeAllVis(stack, p, visUsage, !(focusStack.hasTagCompound() && focusStack.getTagCompound().getBoolean("freeChannel")), false))
		{
			if(p.isUsingItem())
			{
				p.stopUsingItem();
				wand.clearObjectInUse(stack);
				wand.animation = null;
			}
			return;
		}
		
		//inhibit non-vertical movement
		p.motionX = 0;
		p.motionZ = 0;
		
		if(p.motionY > 0)//inhibit upward motion, including jumps and when swimming
			p.motionY = 0;
		
		//visual effect
		if(ticks_used % 10 == 0)//every half second focus is in use, send packets for visual
			ThaumicUtils.wrapper.sendToAll(new CreateReflectShieldVisualPacket(p, false, false));
	}
	
	@Override
	public void onPlayerStoppedUsingFocus(ItemStack wandstack, World world, EntityPlayer player, int count)
	{
		if(wandstack != null && wandstack.getItem() instanceof ItemWandCasting && world != null && !world.isRemote)
		{
			ItemWandCasting wand = (ItemWandCasting)wandstack.getItem();
			if(wand.getFocus(wandstack) == this)
			{
				if(isUpgradedWith(wand.getFocusItem(wandstack), SUPERCHARGED_GLYPHS))
				{
					ItemStack focus = wand.getFocusItem(wandstack);
					if(focus != null && focus.getTagCompound() != null)
					{
						if(player.isSneaking())
						{
							focus.getTagCompound().setLong("activeUntil", 0);
							HandlerUtils.needWandChecked.remove(player);
							ThaumicUtils.wrapper.sendToAll(new CreateReflectShieldVisualPacket(player, true, false));
						}
						else
						{
							focus.getTagCompound().setLong("activeUntil", System.currentTimeMillis() + 10000);
							
							if(!HandlerUtils.needWandChecked.contains(player))
								HandlerUtils.needWandChecked.add(player);
						}
					}
				}
				else if(isUpgradedWith(wand.getFocusItem(wandstack), ASTRAL_AFFINITY))
				{
					ItemStack focusStack = wand.getFocusItem(wandstack);
					
					if(focusStack.getTagCompound() != null && focusStack.getTagCompound().hasKey("newVisAmount"))
					{
						focusStack.getTagCompound().removeTag("newVisAmount");
					}
				}
				
				ThaumicUtils.wrapper.sendToAll(new CreateReflectShieldVisualPacket(player, true, false));
			}
		}
	}
	
	@Override
	public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack stack, int rank)
	{
		switch(rank)
		{
		case 1:
		case 2:
		case 4:
			return new FocusUpgradeType[] { FocusUpgradeType.potency, FocusUpgradeType.frugal };
		
		case 3:
			return new FocusUpgradeType[] { FocusUpgradeType.potency, FocusUpgradeType.frugal, REVITALIZING_COUNTER, RECKLESS_COUNTER };
			
		case 5:
			return new FocusUpgradeType[] { FocusUpgradeType.potency, FocusUpgradeType.frugal, MASTER_OF_THE_ART, SUPERCHARGED_GLYPHS, ASTRAL_AFFINITY };
		}
		return null;//TODO remove all traces of the old mod with multiple astral affinity upgrades from in-game
	}
	
	@Override
	public boolean applyUpgrade(ItemStack focusStack, FocusUpgradeType type, int rank)
	{
		boolean ret = super.applyUpgrade(focusStack, type, rank);
		
		if(ret && type.id == ItemFocusReflectShield.MASTER_OF_THE_ART.id)
		{
			focusStack.getTagCompound().setBoolean("freeChannel", true);
		}
		
		return ret;
	}

	@Override
	public String getSortingHelper(ItemStack paramItemStack)
	{
		return "REFLECT";
	}

	@Override
	public boolean isVisCostPerTick(ItemStack stack)
	{
		return true;
	}

	@Override
	public int getFocusColor(ItemStack stack)
	{
		return 0xD7BFEE;
	}
	
	public boolean isFreeToChannel(ItemStack stack)
	{
		return isUpgradedWith(stack, MASTER_OF_THE_ART);
	}

	@Override
	public AspectList getVisCost(ItemStack stack)
	{
		if(stack.getTagCompound() != null && stack.getTagCompound().hasKey("newVisAmount"))
		{
			int newAmount = stack.getTagCompound().getInteger("newVisAmount");
			return new AspectList().add(Aspect.ORDER, newAmount).add(Aspect.FIRE, newAmount).add(Aspect.WATER, newAmount);
		}
		
		return isFreeToChannel(stack) ? VIS_USAGE_FREE : VIS_USAGE;
		
		//frugal is already applied later, so below is not necessary.
		
		/*float modifier = getUpgradeLevel(stack, FocusUpgradeType.frugal)*0.10f;
		AspectList cost = visUsage.copy();
		for(Aspect a : visUsage.getAspects())
		{
			cost.remove(a, (int) Math.round(cost.getAmount(a) * modifier));
		}
		return cost;*/
	}
}
