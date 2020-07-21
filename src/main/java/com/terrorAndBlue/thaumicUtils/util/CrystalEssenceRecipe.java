package com.terrorAndBlue.thaumicUtils.util;

import java.util.List;

import com.terrorAndBlue.thaumicUtils.ThaumicUtils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.world.World;
import thaumcraft.api.ItemApi;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.api.crafting.ShapelessArcaneRecipe;
import thaumcraft.common.items.ItemCrystalEssence;

public class CrystalEssenceRecipe extends ShapelessArcaneRecipe
{
	public CrystalEssenceRecipe(String research, ItemStack item, AspectList aspects, Object... params)
	{
		super(research, item, aspects, params);
	}

	@Override
	public AspectList getAspects()
	{
		return aspects;
	}

	@Override
	public AspectList getAspects(IInventory inv)
	{
		return getAspects();
	}

	@Override
	public ItemStack getCraftingResult(IInventory inv)
	{
		return super.getCraftingResult(inv);
	}

	@Override
	public String getResearch()
	{
		return research;
	}

	@Override
	public boolean matches(IInventory inv, World world, EntityPlayer player)
	{
		return super.matches(inv, world, player);
	}
	
	
	public static AspectList getPrimals(AspectList list)
	{
		if(list == null)
			return null;
		
		boolean primal = true;
		for(Aspect asp : list.aspects.keySet())
			primal = primal && asp.isPrimal();
		
		if(primal)
			return list;
		
		AspectList newList = new AspectList();
		for(Aspect asp : list.aspects.keySet())
			if(asp.isPrimal())
				newList.add(asp, list.getAmount(asp));
			else
				newList.add(new AspectList().add(asp.getComponents()[0],1).add(asp.getComponents()[1],1));
		
		return getPrimals(newList);
	}
}
