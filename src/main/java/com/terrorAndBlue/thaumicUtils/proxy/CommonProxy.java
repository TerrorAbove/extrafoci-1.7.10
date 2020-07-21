package com.terrorAndBlue.thaumicUtils.proxy;

import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.entity.player.EntityPlayer;

public class CommonProxy
{
	public void preInit()
	{
		
	}
	
	public void init()
	{
		
	}
	
	public void postInit()
	{
		
	}
	
	public EntityPlayer getPlayerEntity(MessageContext ctx)
	{
		return ctx.getServerHandler().playerEntity;
	}
}
