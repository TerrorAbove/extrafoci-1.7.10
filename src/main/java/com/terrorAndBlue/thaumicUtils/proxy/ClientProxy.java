package com.terrorAndBlue.thaumicUtils.proxy;

import com.terrorAndBlue.thaumicUtils.client.ReflectShieldVisualRender;
import com.terrorAndBlue.thaumicUtils.entity.EntityReflectShieldVisual;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

public class ClientProxy extends CommonProxy
{
	public void preInit()
	{
		RenderingRegistry.registerEntityRenderingHandler(EntityReflectShieldVisual.class, new ReflectShieldVisualRender());
	}
	
	public void init()
	{
		
	}
	
	public void postInit()
	{
		
	}
	
	@Override
	public EntityPlayer getPlayerEntity(MessageContext ctx)
	{
		// Note that if you simply return 'Minecraft.getMinecraft().thePlayer',
		// your packets will not work because you will be getting a client
		// player even when you are on the server! Sounds absurd, but it's true.

		// Solution is to double-check side before returning the player:
		return (ctx.side.isClient() ? Minecraft.getMinecraft().thePlayer : super.getPlayerEntity(ctx));
	}
}
