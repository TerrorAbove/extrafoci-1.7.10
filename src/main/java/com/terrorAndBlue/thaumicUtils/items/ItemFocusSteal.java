package com.terrorAndBlue.thaumicUtils.items;

import java.util.HashMap;

import com.terrorAndBlue.thaumicUtils.ThaumicUtils;
import com.terrorAndBlue.thaumicUtils.network.StealPacket;
import com.terrorAndBlue.thaumicUtils.util.DropList;
import com.terrorAndBlue.thaumicUtils.util.DropList.Drop;
import com.terrorAndBlue.thaumicUtils.util.HelperMethods;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.common.items.wands.ItemWandCasting;

public class ItemFocusSteal extends TerrorBaseFocus
{
	public static final int MAX_DURATION_TICKS = 100;//stealing from the same target increases the time it takes: each additional steal takes one second longer
	public static final AspectList VIS_USAGE_DISPLAY = new AspectList().add(Aspect.ENTROPY, 10);
	public static final AspectList VIS_USAGE_ACTUAL = new AspectList().add(Aspect.ENTROPY, 200);
	
	public ItemFocusSteal()
	{
		super();
		this.setUnlocalizedName("wandFocusSteal");
		this.setCreativeTab(CreativeTabs.tabCombat);//TODO TEMPORARY
	}
	
	@Override
	public void registerIcons(IIconRegister reg)
	{
		this.icon = reg.registerIcon(ThaumicUtils.MODID+":wand_focus_steal");
	}
	
	@Override
	public ItemStack onFocusRightClick(ItemStack stack, World world, EntityPlayer player, MovingObjectPosition mop)
	{
		player.setItemInUse(stack, MAX_DURATION_TICKS);
		return stack;
	}
	
	@Override
	public void onUsingFocusTick(ItemStack wandstack, EntityPlayer player, int count)
	{
		ItemWandCasting wand = (ItemWandCasting)wandstack.getItem();
		
		if(player.worldObj.isRemote)//client side
		{
			EntityLivingBase target = HelperMethods.getClientTarget(10);
			
			int entityID = -1;
			
			if(target == null)
			{
				player.stopUsingItem();
				wand.clearObjectInUse(wandstack);
				wand.animation = null;
			}
			else
			{
				entityID = target.getEntityId();
			}
			
			if(count % 20 == 0 || count == 1)
				ThaumicUtils.wrapper.sendToServer(new StealPacket(entityID, count));
		}

		ItemStack focusStack = wand.getFocusItem(wandstack);

		if(focusStack != null)
		{
			NBTTagCompound c = focusStack.stackTagCompound;

			if(c != null)
			{
				if(c.hasKey("lastStealTarget"))
				{
					int id = c.getInteger("lastStealTarget");
					Entity ent = player.worldObj.getEntityByID(id);
					
					if(ent instanceof EntityPlayer)
					{
						//TODO
					}
					else if(ent instanceof EntityLiving)
					{
						EntityLiving el = (EntityLiving)ent;
						
						NBTTagCompound tc = el.getEntityData();
						
						if(tc != null)
						{
							long time = tc.getLong("stealTargetTime");
							
							if(System.currentTimeMillis() - time < StealPacket.COOLDOWN_TIME*1000L)
								return;
						}
						
						el.setAttackTarget(null);//prevent targets from attacking anything while steal is being channeled
						
						//stop movement and set rotation variables to old values
						el.motionX = 0;
						el.motionY = 0;
						el.motionZ = 0;
						if(!player.worldObj.isRemote)
						{
							el.posX = el.prevPosX;
							el.posY = el.prevPosY;
							el.posZ = el.prevPosZ;
							
							if(tc != null && el instanceof EntityEnderman)
							{
								tc.setLong("stealTargetTeleBlocked", System.currentTimeMillis());
							}
						}
						el.rotationYawHead = el.prevRotationYawHead;
						el.rotationPitch = el.prevRotationPitch;
						el.rotationYaw = el.prevRotationYaw;
					}
				}
			}
		}
	}
	
	@Override
	public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack stack, int rank)
	{
		switch(rank)
		{
		case 2:
		case 4:
			return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.treasure};
		case 1:
		case 3:
			return new FocusUpgradeType[]{FocusUpgradeType.frugal};
		case 5:
			//TODO rank 5, add custom upgrade
			//ideas: slow target, deal half-heart per sec, close-range but faster, long-range but slower, instant catch loot
		}
		return null;
	}
	
	@Override
	public String getSortingHelper(ItemStack paramItemStack)
	{
		return "STEAL";
	}

	@Override
	public boolean isVisCostPerTick(ItemStack stack)
	{
		return true;
	}
	
	@Override
	public AspectList getVisCost(ItemStack stack)
	{
		return VIS_USAGE_DISPLAY;
	}
}
