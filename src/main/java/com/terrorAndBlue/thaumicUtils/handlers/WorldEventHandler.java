package com.terrorAndBlue.thaumicUtils.handlers;

import com.terrorAndBlue.thaumicUtils.command.ExtraFociDebugCommand;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.world.WorldEvent;

public class WorldEventHandler
{
	@SubscribeEvent
	public void handleWorldUnloading(WorldEvent.Unload event)
	{
		if(ExtraFociDebugCommand.outFrame != null)
		{
			ExtraFociDebugCommand.outFrame.dispose();
			ExtraFociDebugCommand.outFrame = null;
			System.setOut(ExtraFociDebugCommand.DEFAULT_OUT);
		}
	}
}
