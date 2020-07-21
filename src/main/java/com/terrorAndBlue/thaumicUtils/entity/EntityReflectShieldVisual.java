package com.terrorAndBlue.thaumicUtils.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class EntityReflectShieldVisual extends Entity
{
	private static final int MAX_TICKS_ALIVE = 10;//half second
	
	private EntityPlayer following;
	private int timesRefreshed;
	private int hurtVisual;
	
	public EntityReflectShieldVisual(World world)
	{
		super(world);
		this.setSize(1.0F, 2.0F);
		this.timesRefreshed = 0;
		this.hurtVisual = 0;
	}
	
	public void setFollowing(EntityPlayer player)
	{
		this.following = player;
	}
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		if(following != null)
		{
			this.motionX = following.motionX;
			this.motionY = following.motionY;
			this.motionZ = following.motionZ;
			
			setLocationAndAngles(following.posX, following.posY, following.posZ, following.rotationYaw, following.rotationPitch);
		}
		
		if(ticksExisted > MAX_TICKS_ALIVE * (2 + timesRefreshed))
			setDead();
		
		if(hurtVisual > 0)
			hurtVisual--;
	}
	
	public void refresh()
	{
		timesRefreshed++;
	}
	
	public void setHurt(int ticks)
	{
		hurtVisual = ticks;
	}
	
	public boolean renderHurtVisual()
	{
		return hurtVisual > 0;
	}

	@Override
	protected void entityInit() {}

	@Override
	protected void readEntityFromNBT(NBTTagCompound tag)
	{
		/*if(tag.hasKey("playerID"))
		{
			Entity ent = worldObj.getEntityByID(tag.getInteger("playerID"));
			
			if(ent instanceof EntityPlayer)
			{
				following = (EntityPlayer)ent;
			}
		}*/
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound tag)
	{
		/*if(following != null)
			tag.setInteger("playerID", following.getEntityId());*/
	}
}