package com.terrorAndBlue.thaumicUtils.network;

import java.util.HashMap;

import com.terrorAndBlue.thaumicUtils.ThaumicUtils;
import com.terrorAndBlue.thaumicUtils.items.ItemFocusSteal;
import com.terrorAndBlue.thaumicUtils.util.DropList;
import com.terrorAndBlue.thaumicUtils.util.DropList.Drop;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.common.items.wands.ItemWandCasting;

public class StealPacket implements IMessage
{
	public static final int COOLDOWN_TIME = 10;//10 seconds
	public static final int MAX_STEALS = 3;//the max times a given target can be stolen from
	
	public static class Handler implements IMessageHandler<StealPacket, IMessage>
	{
		@Override
		public IMessage onMessage(StealPacket message, MessageContext ctx)
		{
			EntityPlayer player = ThaumicUtils.proxy.getPlayerEntity(ctx);
			
			ItemWandCasting wand = null;
			ItemFocusBasic itemFocus = null;
			ItemStack held = player.getCurrentEquippedItem();
			if(held != null)
			{
				Item item = held.getItem();
				if(item instanceof ItemWandCasting)
				{
					wand = (ItemWandCasting)item;
					itemFocus = wand.getFocus(held);
				}
			}
			
			if(wand == null)
				return null;
			
			if(message.entityID == -1)
			{
				player.stopUsingItem();
				wand.clearObjectInUse(held);
				wand.animation = null;
			}
			else
			{
				Entity ent = player.worldObj.getEntityByID(message.entityID);
				
				if(ent instanceof EntityLivingBase)
				{
					EntityLivingBase target = (EntityLivingBase)ent;
					
					if(target.isDead)
						return null;
					
					NBTTagCompound tc = target.getEntityData();
					
					int steals = tc == null ? MAX_STEALS : tc.getInteger("stealTargetNum");
					long time = tc == null ? 0 : tc.getLong("stealTargetTime");
					
					if(steals >= MAX_STEALS)
					{
						player.stopUsingItem();
						wand.clearObjectInUse(held);
						wand.animation = null;
						return null;
					}
					
					int count = message.itemUseCount - 20*(MAX_STEALS - steals - 1);//steal takes 3/4/5 seconds
					
					if(System.currentTimeMillis() - time < COOLDOWN_TIME*1000L)
						return null;
					
					if(!wand.consumeAllVis(held, player, ItemFocusSteal.VIS_USAGE_ACTUAL, true, false))
						return null;
					
					//target.attackEntityFrom(DamageSource.magic, 1);
					//target.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 20, 3));
					
					//TODO send packet to all nearby clients for rendering visual effect for 1 second
					
					ItemStack focusStack = wand.getFocusItem(held);
					NBTTagCompound c = focusStack.stackTagCompound;
					
					if(c == null)
						c = new NBTTagCompound();
					
					c.setInteger("lastStealTarget", target.getEntityId());
					focusStack.setTagCompound(c);
					
					if(count <= 1 && player.getDistanceToEntity(target) <= 10)
					{
						DropList list = ThaumicUtils.DROP_TABLE.get(EntityList.getEntityString(target));
						
						if(list != null)
						{
							for(Drop drop : list.drops)
							{
								int fortune = itemFocus.getUpgradeLevel(wand.getFocusItem(held), FocusUpgradeType.treasure);
								
								if(drop.roll(fortune))
								{
									EntityItem item = new EntityItem(target.worldObj, target.posX, target.posY+0.5, target.posZ, drop.getItem().copy());
									item.posX += (player.posX-target.posX)/5;
									item.posZ += (player.posZ-target.posZ)/5;
									item.addVelocity((player.posX-target.posX)/20, 0.2+(player.posY-target.posY)/20, (player.posZ-target.posZ)/20);
									item.delayBeforeCanPickup = 0;
									item.lifespan = 40;//2 seconds
									
									target.worldObj.spawnEntityInWorld(item);
									
									if(drop.isSpecial())
										break;
								}
							}
							player.inventoryContainer.detectAndSendChanges();
						}
						
						if(tc != null)
						{
							tc.setLong("stealTargetTime", System.currentTimeMillis());
							tc.setInteger("stealTargetNum", tc.getInteger("stealTargetNum")+1);
						}
						
						player.stopUsingItem();
						wand.clearObjectInUse(held);
						wand.animation = null;
					}
				}
			}
			
			return null;
		}
	}
	
	private int entityID;
	private int itemUseCount;

	public StealPacket() {}
	
	public StealPacket(int entityID, int itemUseCount)
	{
		this.entityID = entityID;
		this.itemUseCount = itemUseCount;
	}
	
	@Override
	public void fromBytes(ByteBuf buf)
	{
		entityID = buf.readInt();
		itemUseCount = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(entityID);
		buf.writeInt(itemUseCount);
	}
}