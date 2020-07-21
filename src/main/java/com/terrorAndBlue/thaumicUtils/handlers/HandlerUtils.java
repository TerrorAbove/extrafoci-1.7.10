package com.terrorAndBlue.thaumicUtils.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.terrorAndBlue.thaumicUtils.ThaumicUtils;
import com.terrorAndBlue.thaumicUtils.items.ItemFocusReflectShield;
import com.terrorAndBlue.thaumicUtils.network.CreateReflectShieldVisualPacket;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.common.items.wands.ItemWandCasting;

public class HandlerUtils
{
	public static List<EntityPlayer> needWandChecked = new ArrayList<EntityPlayer>();
	public static Map<EntityLivingBase, Long> stunnedList = new HashMap<EntityLivingBase, Long>();
	public static Map<EntityLivingBase, AstralDebuff> debuffList = new HashMap<EntityLivingBase, AstralDebuff>();
	
	public static ItemStack findGlyphWand(EntityPlayer player)
	{
		for(int i = 0; i < player.inventory.mainInventory.length; i++)
		{
			ItemStack curr = player.inventory.mainInventory[i];
			
			if(curr != null && curr.getItem() instanceof ItemWandCasting)
			{
				ItemWandCasting wand = (ItemWandCasting)curr.getItem();
				
				if(wand.getFocus(curr) == ThaumicUtils.wandFocusReflect)
				{
					ItemStack focus = wand.getFocusItem(curr);
					if(((ItemFocusReflectShield)ThaumicUtils.wandFocusReflect).isUpgradedWith(focus, ItemFocusReflectShield.SUPERCHARGED_GLYPHS))
					{
						if(focus.getTagCompound() != null)
						{
							if(focus.getTagCompound().getLong("activeUntil") > System.currentTimeMillis())
							{
								return curr;
							}
							else
							{
								needWandChecked.remove(player);
								ThaumicUtils.wrapper.sendToAll(new CreateReflectShieldVisualPacket(player, true, false));
							}
						}
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @param player the player casting reflect
	 * @param source the source of damage
	 * @return true if the player can counter the damage source. Now accounts for player rotation.
	 */
	public static boolean canCounterDamageSource(EntityPlayer player, DamageSource source)
	{
		Entity other = source.getSourceOfDamage();
		
		if(source.isDamageAbsolute() || source.isUnblockable() || source.canHarmInCreative())
			return false;
		
		if(source == DamageSource.anvil ||
				source == DamageSource.cactus ||
				source == DamageSource.drown ||
				source == DamageSource.fall ||
				source == DamageSource.fallingBlock ||
				source == DamageSource.inFire ||
				source == DamageSource.inWall ||
				source == DamageSource.lava ||
				source == DamageSource.onFire ||
				source == DamageSource.outOfWorld ||
				source == DamageSource.starve ||
				source == DamageSource.wither)
			return false;
		
		if(player == source.getSourceOfDamage())
			return false;
		
		if(other == null)
			return false;
		
		if(other instanceof EntityArrow)
			other = ((EntityArrow)other).shootingEntity;
		else if(other instanceof EntityFireball)
			other = ((EntityFireball)other).shootingEntity;
		else if(other instanceof EntityThrowable)
			other = ((EntityThrowable)other).getThrower();
		
		if(other == null)
			return false;
		
		return isPlayerFacingEntity(player, other);
	}
	
	public static boolean isPlayerFacingEntity(EntityPlayer player, Entity other)
	{
		if(!player.canEntityBeSeen(other))//checks if the enemy went behind a wall
			return false;
		
		Vec3 diff = Vec3.createVectorHelper(other.posX-player.posX, other.posY-player.posY, other.posZ-player.posZ).normalize();
		Vec3 look = player.getLookVec();
		
		double length0_xz = Math.sqrt(diff.xCoord * diff.xCoord + diff.zCoord * diff.zCoord);
		double length1_xz = Math.sqrt(look.xCoord * look.xCoord + look.zCoord * look.zCoord);
		
		int a = 180 + (int)(180 * Math.atan2(diff.zCoord, diff.xCoord) / Math.PI);
		int b = 180 + (int)(180 * Math.atan2(look.zCoord, look.xCoord) / Math.PI);
		
		int c = (int)(90 * Math.atan2(diff.yCoord, length0_xz) / Math.PI);
		int d = (int)(90 * Math.atan2(look.yCoord, length1_xz) / Math.PI);
		
		return Math.abs(a-b) < 90 && Math.abs(c-d) < 20;//checks yaw and pitch
	}
	
	public static void doReflectionEffect(LivingHurtEvent event, EntityPlayer player, ItemStack wandStack, ItemWandCasting caster)
	{
		if(event.source != null && canCounterDamageSource(player, event.source))
		{
			ItemStack focusStack = caster.getFocusItem(wandStack);
			ItemFocusReflectShield item = (ItemFocusReflectShield)focusStack.getItem();
			
			final float ORIGINAL = event.ammount;
			
			if(item.isUpgradedWith(focusStack, ItemFocusReflectShield.MASTER_OF_THE_ART))
			{
				int visAmount = (int)(20 * ORIGINAL);//this is equivalent to 1 actual vis point drained per 5 damage.
				//float frugal_multiplier = 1F - 0.1F * item.getUpgradeLevel(focusStack, FocusUpgradeType.frugal);
				//visAmount = (int)(visAmount * frugal_multiplier);
				
				AspectList list = new AspectList().add(Aspect.ORDER, visAmount).add(Aspect.FIRE, visAmount).add(Aspect.WATER, visAmount);
				
				if(!caster.consumeAllVis(wandStack, player, list, true, false))//frugal should be auto-applied here
				{
					if(wandStack == player.getHeldItem() && player.isUsingItem())
					{
						player.stopUsingItem();
						caster.clearObjectInUse(wandStack);
						caster.animation = null;
					}
					
					caster.consumeVis(wandStack, player, Aspect.ORDER, Math.min(visAmount, caster.getVis(wandStack, Aspect.ORDER)), false);
					caster.consumeVis(wandStack, player, Aspect.FIRE, Math.min(visAmount, caster.getVis(wandStack, Aspect.FIRE)), false);
					caster.consumeVis(wandStack, player, Aspect.WATER, Math.min(visAmount, caster.getVis(wandStack, Aspect.WATER)), false);
				}
			}
			
			boolean moonlight = player.worldObj.skylightSubtracted >= 7 && player.worldObj.canBlockSeeTheSky((int)player.posX, (int)player.posY+1, (int)player.posZ);
			
			boolean astral = item.isUpgradedWith(focusStack, ItemFocusReflectShield.ASTRAL_AFFINITY);
			
			float damageFactorAOE = 0.5F;
			
			float reflection_multiplier = item.isUpgradedWith(focusStack, ItemFocusReflectShield.RECKLESS_COUNTER) ? 3.0F : 0.5F;
			float potency_bonus = 0.2F * item.getUpgradeLevel(focusStack, FocusUpgradeType.potency);
			
			if(item.isUpgradedWith(focusStack, ItemFocusReflectShield.MASTER_OF_THE_ART))
				reflection_multiplier *= 2F;
			
			reflection_multiplier *= 1 + potency_bonus;
			
			float user_damage_multiplier = item.isUpgradedWith(focusStack, ItemFocusReflectShield.RECKLESS_COUNTER) ? 0.5F : 0F;
			float potency_penalty = 0.2F * item.getUpgradeLevel(focusStack, FocusUpgradeType.potency);
			
			user_damage_multiplier *= 1 + potency_penalty;
			
			event.ammount *= user_damage_multiplier;
			
			if(event.ammount == 0)
				player.hurtTime = 0;
			
			player.getFoodStats().addStats((int) event.source.getHungerDamage(), 1.0F);
			
			if(!player.worldObj.isRemote)//send clients visual update for "shield took a hit" effect
				ThaumicUtils.wrapper.sendToAll(new CreateReflectShieldVisualPacket(player, false, true));
			
			if(item.isUpgradedWith(focusStack, ItemFocusReflectShield.SUPERCHARGED_GLYPHS))
			{
				List list = player.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, AxisAlignedBB.getBoundingBox(player.posX-16, player.posY-8, player.posZ-16, player.posX+16, player.posY+8, player.posZ+16));
				
				for(Object o : list)
				{
					if(o instanceof EntityLivingBase)
					{
						EntityLivingBase elb = (EntityLivingBase)o;
						
						float dist = elb.getDistanceToEntity(player);
						
						//TODO check if original target was player
						if(elb != player && (!(elb instanceof EntityPlayer) || player.canAttackPlayer((EntityPlayer)elb))
								&& (elb instanceof EntityPlayer || elb.getAITarget() == player))
						{
							if(dist > 8 && dist <= 16)
							{
								HandlerUtils.stunnedList.put(elb, System.currentTimeMillis() + 3000);
							}
							else if(dist <= 8)
							{
								elb.knockBack(player, 0, (8 - dist) * (player.posX - elb.posX), (8 - dist) * (player.posZ - elb.posZ));
							}
						}
					}
				}
			}
			
			if(event.source.getSourceOfDamage() instanceof EntityLivingBase)
			{
				EntityLivingBase elb = (EntityLivingBase)event.source.getSourceOfDamage();
				
				final float REFLECTION_DAMAGE = ORIGINAL * reflection_multiplier;
				
				elb.attackEntityFrom(new DamageSource("reflection"), REFLECTION_DAMAGE);
				
				float healing_increase = 0F;
				
				if(astral && moonlight)//aoe centered on player, checks if targets are in front of player
				{
					AstralDebuff debuff = new AstralDebuff();
					if(debuffList.containsKey(elb))
					{
						debuff = debuffList.get(elb);
					}
					debuffList.put(elb, debuff.addStack());
					
					List list = player.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, AxisAlignedBB.getBoundingBox(player.posX-2, player.posY-1, player.posZ-2, player.posX+2, player.posY+1, player.posZ+2));
					
					for(Object o : list)
					{
						if(o instanceof EntityLivingBase)
						{
							EntityLivingBase elb2 = (EntityLivingBase)o;
							
							if(elb2 != elb && elb2 != player && elb2.getDistanceToEntity(player) <= 2
									&& (!(elb2 instanceof EntityPlayer) || (elb instanceof EntityPlayer && player.canAttackPlayer((EntityPlayer)elb2)))
									&& isPlayerFacingEntity(player, elb2)
									&& elb2.getAITarget() == player)
							{
								float damage = damageFactorAOE * REFLECTION_DAMAGE;
								elb2.attackEntityFrom(new DamageSource("reflection"), damage);
								healing_increase += elb2.getHealth() > 0 ? damage/2F : damage;
								
								debuff = new AstralDebuff();
								if(debuffList.containsKey(elb2))
								{
									debuff = debuffList.get(elb2);
								}
								debuffList.put(elb2, debuff.addStack());
							}
						}
					}
				}
				
				if(item.isUpgradedWith(focusStack, ItemFocusReflectShield.REVITALIZING_COUNTER))
				{
					player.heal(healing_increase + (elb.getHealth() > 0 ? REFLECTION_DAMAGE/2F : REFLECTION_DAMAGE));
				}
			}
			else if(event.source.getSourceOfDamage() instanceof EntityArrow)
			{
				EntityArrow arrow = (EntityArrow)event.source.getSourceOfDamage();

				if(arrow.shootingEntity instanceof EntityLivingBase)
				{
					EntityLivingBase elb = (EntityLivingBase)arrow.shootingEntity;
					
					arrow.setDead();
					player.setArrowCountInEntity(Math.max(0, player.getArrowCountInEntity() - 1));

					final float REFLECTION_DAMAGE = ORIGINAL * reflection_multiplier;
					
					elb.attackEntityFrom(new DamageSource("reflection"), REFLECTION_DAMAGE);
					
					float healing_increase = 0F;
					
					if(astral && moonlight)//aoe centered on target
					{
						AstralDebuff debuff = new AstralDebuff();
						if(debuffList.containsKey(elb))
						{
							debuff = debuffList.get(elb);
						}
						debuffList.put(elb, debuff.addStack());
						
						List list = player.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, AxisAlignedBB.getBoundingBox(elb.posX-3, elb.posY-1, elb.posZ-3, elb.posX+3, elb.posY+1, elb.posZ+3));
						
						for(Object o : list)
						{
							if(o instanceof EntityLivingBase)
							{
								EntityLivingBase elb2 = (EntityLivingBase)o;
								
								if(elb2 != elb && elb2 != player && elb2.getDistanceToEntity(elb) <= 3
										&& (!(elb2 instanceof EntityPlayer) || (elb instanceof EntityPlayer && player.canAttackPlayer((EntityPlayer)elb2)))
										&& isPlayerFacingEntity(player, elb2)
										&& elb2.getAITarget() == player)
								{
									float damage = damageFactorAOE * REFLECTION_DAMAGE;
									elb2.attackEntityFrom(new DamageSource("reflection"), damage);
									healing_increase += elb2.getHealth() > 0 ? damage/2F : damage;
									
									debuff = new AstralDebuff();
									if(debuffList.containsKey(elb2))
									{
										debuff = debuffList.get(elb2);
									}
									debuffList.put(elb2, debuff.addStack());
								}
							}
						}
					}
					
					if(item.isUpgradedWith(focusStack, ItemFocusReflectShield.REVITALIZING_COUNTER))
					{
						player.heal(healing_increase + (elb.getHealth() > 0 ? REFLECTION_DAMAGE/2F : REFLECTION_DAMAGE));
					}
				}
			}
			else if(event.source.getSourceOfDamage() instanceof EntityFireball)
			{
				EntityFireball fireball = (EntityFireball)event.source.getSourceOfDamage();
				
				if(fireball.shootingEntity instanceof EntityLivingBase)
				{
					EntityLivingBase elb = (EntityLivingBase)fireball.shootingEntity;
					fireball.setDead();
					
					player.extinguish();

					final float REFLECTION_DAMAGE = ORIGINAL * reflection_multiplier;
					
					elb.attackEntityFrom(new DamageSource("reflection"), REFLECTION_DAMAGE);
					
					float healing_increase = 0F;
					
					if(astral && moonlight)//aoe centered on target
					{
						AstralDebuff debuff = new AstralDebuff();
						if(debuffList.containsKey(elb))
						{
							debuff = debuffList.get(elb);
						}
						debuffList.put(elb, debuff.addStack());
						
						List list = player.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, AxisAlignedBB.getBoundingBox(elb.posX-3, elb.posY-1, elb.posZ-3, elb.posX+3, elb.posY+1, elb.posZ+3));
						
						for(Object o : list)
						{
							if(o instanceof EntityLivingBase)
							{
								EntityLivingBase elb2 = (EntityLivingBase)o;
								
								if(elb2 != elb && elb2 != player && elb2.getDistanceToEntity(elb) <= 3
										&& (!(elb2 instanceof EntityPlayer) || (elb instanceof EntityPlayer && player.canAttackPlayer((EntityPlayer)elb2)))
										&& isPlayerFacingEntity(player, elb2)
										&& elb2.getAITarget() == player)
								{
									float damage = damageFactorAOE * REFLECTION_DAMAGE;
									elb2.attackEntityFrom(new DamageSource("reflection"), damage);
									healing_increase += elb2.getHealth() > 0 ? damage/2F : damage;
									
									debuff = new AstralDebuff();
									if(debuffList.containsKey(elb2))
									{
										debuff = debuffList.get(elb2);
									}
									debuffList.put(elb2, debuff.addStack());
								}
							}
						}
					}
					
					if(item.isUpgradedWith(focusStack, ItemFocusReflectShield.REVITALIZING_COUNTER))
					{
						player.heal(healing_increase + (elb.getHealth() > 0 ? REFLECTION_DAMAGE/2F : REFLECTION_DAMAGE));
					}
				}
			}
			else if(event.source.getSourceOfDamage() instanceof EntityThrowable)
			{
				EntityThrowable thrown = (EntityThrowable)event.source.getSourceOfDamage();
				
				//TODO fix, witch reflection not working
				
				if(thrown.getThrower() instanceof EntityLivingBase)
				{
					EntityLivingBase elb = (EntityLivingBase)thrown.getThrower();

					final float REFLECTION_DAMAGE = ORIGINAL * reflection_multiplier;
					
					elb.attackEntityFrom(new DamageSource("reflection"), REFLECTION_DAMAGE);
					
					float healing_increase = 0F;
					
					if(astral && moonlight)//aoe centered on target
					{
						AstralDebuff debuff = new AstralDebuff();
						if(debuffList.containsKey(elb))
						{
							debuff = debuffList.get(elb);
						}
						debuffList.put(elb, debuff.addStack());
						
						List list = player.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, AxisAlignedBB.getBoundingBox(elb.posX-3, elb.posY-1, elb.posZ-3, elb.posX+3, elb.posY+1, elb.posZ+3));
						
						for(Object o : list)
						{
							if(o instanceof EntityLivingBase)
							{
								EntityLivingBase elb2 = (EntityLivingBase)o;
								
								if(elb2 != elb && elb2 != player && elb2.getDistanceToEntity(elb) <= 3
										&& (!(elb2 instanceof EntityPlayer) || (elb instanceof EntityPlayer && player.canAttackPlayer((EntityPlayer)elb2)))
										&& isPlayerFacingEntity(player, elb2)
										&& elb2.getAITarget() == player)
								{
									float damage = damageFactorAOE * REFLECTION_DAMAGE;
									elb2.attackEntityFrom(new DamageSource("reflection"), damage);
									healing_increase += elb2.getHealth() > 0 ? damage/2F : damage;
									
									debuff = new AstralDebuff();
									if(debuffList.containsKey(elb2))
									{
										debuff = debuffList.get(elb2);
									}
									debuffList.put(elb2, debuff.addStack());
								}
							}
						}
					}
					
					if(item.isUpgradedWith(focusStack, ItemFocusReflectShield.REVITALIZING_COUNTER))
					{
						player.heal(healing_increase + (elb.getHealth() > 0 ? REFLECTION_DAMAGE/2F : REFLECTION_DAMAGE));
					}
					
					if(thrown instanceof EntityPotion)
					{
						EntityPotion potion = (EntityPotion)thrown;

						List list = Items.potionitem.getEffects(potion.getPotionDamage());

						if (list != null && !list.isEmpty())
						{
							Iterator iterator1 = list.iterator();

							while (iterator1.hasNext())
							{
								PotionEffect potioneffect = (PotionEffect)iterator1.next();
								int i = potioneffect.getPotionID();
								
								player.removePotionEffect(i);

								if (Potion.potionTypes[i].isInstant())
								{
									Potion.potionTypes[i].affectEntity(player, elb, potioneffect.getAmplifier(), 1.0);
								}
								else
								{
									int j = (int)((double)potioneffect.getDuration() + 0.5D);

									if (j > 20)
									{
										elb.addPotionEffect(new PotionEffect(i, j, potioneffect.getAmplifier()));
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	public static class AstralDebuff
	{
		private int strength;
		private long expiration;
		
		private AstralDebuff()
		{
			strength = 0;
			expiration = 0;
		}
		
		private AstralDebuff addStack()
		{
			if(strength < 8)
				strength++;
			
			expiration = System.currentTimeMillis() + 5000;
			
			return this;
		}
		
		public float getMultiplier()
		{
			return 1 + 0.5F * strength;
		}
		
		public long getExpiration()
		{
			return expiration;
		}
	}
}
