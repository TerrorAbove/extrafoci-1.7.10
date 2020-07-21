package com.terrorAndBlue.thaumicUtils.network;

import java.util.HashMap;
import java.util.Map;

import com.terrorAndBlue.thaumicUtils.ThaumicUtils;
import com.terrorAndBlue.thaumicUtils.entity.EntityReflectShieldVisual;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;

public class CreateReflectShieldVisualPacket implements IMessage
{
	public static final Map<EntityPlayer, EntityReflectShieldVisual> ACTIVE_SHIELDS = new HashMap<EntityPlayer, EntityReflectShieldVisual>();
	
	private int playerID;
	private boolean stoppedUsing;
	private boolean hurtEffect;
	
	public static class Handler implements IMessageHandler<CreateReflectShieldVisualPacket, IMessage>
	{
		@Override
		public IMessage onMessage(CreateReflectShieldVisualPacket message, MessageContext ctx)
		{
			World w = ThaumicUtils.proxy.getPlayerEntity(ctx).worldObj;

			if(w != null)
			{
				Entity ent = w.getEntityByID(message.playerID);
				if(ent instanceof EntityPlayer)
				{
					EntityPlayer pl = (EntityPlayer)ent;

					if(message.hurtEffect)
					{
						if(ACTIVE_SHIELDS.containsKey(pl))
						{
							EntityReflectShieldVisual prev = ACTIVE_SHIELDS.get(pl);
							prev.setHurt(8);//"shield hit" effect
						}
						return null;
					}

					if(message.stoppedUsing)
					{
						if(ACTIVE_SHIELDS.containsKey(pl))
						{
							EntityReflectShieldVisual prev = ACTIVE_SHIELDS.get(pl);
							prev.setDead();

							ACTIVE_SHIELDS.remove(pl);
						}
					}
					else
					{
						if(ACTIVE_SHIELDS.containsKey(pl))
						{
							EntityReflectShieldVisual prev = ACTIVE_SHIELDS.get(pl);

							if(prev.isEntityAlive())
							{
								prev.refresh();
							}
							else
							{
								EntityReflectShieldVisual entity = new EntityReflectShieldVisual(w);
								entity.setFollowing(pl);
								w.spawnEntityInWorld(entity);
								ACTIVE_SHIELDS.put(pl, entity);
							}
						}
						else
						{
							EntityReflectShieldVisual entity = new EntityReflectShieldVisual(w);
							entity.setFollowing(pl);
							w.spawnEntityInWorld(entity);
							ACTIVE_SHIELDS.put(pl, entity);
						}
					}
				}
			}
			return null;
		}
	}
	
	public CreateReflectShieldVisualPacket() {}
	
	public CreateReflectShieldVisualPacket(EntityPlayer player, boolean stoppedUsing, boolean hurtEffect)
	{
		this.playerID = player.getEntityId();
		this.stoppedUsing = stoppedUsing;
		this.hurtEffect = hurtEffect;
	}
	
	@Override
	public void fromBytes(ByteBuf buf)
	{
		this.playerID = buf.readInt();
		this.stoppedUsing = buf.readBoolean();
		this.hurtEffect = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(playerID);
		buf.writeBoolean(stoppedUsing);
		buf.writeBoolean(hurtEffect);
	}
}
