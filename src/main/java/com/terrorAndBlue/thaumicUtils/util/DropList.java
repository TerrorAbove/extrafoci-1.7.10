package com.terrorAndBlue.thaumicUtils.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class DropList
{
	public List<Drop> drops;
	
	public DropList(String data)
	{
		ArrayList<Drop> list = new ArrayList<Drop>();
		
		String[] drops = data.split(",");
		
		for(String drop : drops)
		{
			String itemName = "";
			int amount = 1;
			boolean special = false;
			double[] chances = new double[] {1, 1, 1};
			
			if(drop.contains("*"))
			{
				special = true;
				drop = drop.replace("*", "");
			}
			
			int k = drop.indexOf("[");
			int l = drop.indexOf("]");
			
			if(k >= 0 && l >= 0)
			{
				String amountStr = drop.substring(k+1, l);
				
				try
				{
					amount = Integer.parseInt(amountStr);
				}
				catch(NumberFormatException nfe) {}
				
				drop = drop.substring(0, k) + drop.substring(l+1, drop.length());
			}
			
			int m = drop.indexOf("(");
			int n = drop.indexOf(")");
			
			if(m >= 0 && n >= 0)
			{
				String chanceStr = drop.substring(m+1, n);
				String[] ch = chanceStr.split(" ");
				
				try
				{
					for(int i = 0; i < Math.min(chances.length, ch.length); i++)
						chances[i] = Double.parseDouble(ch[i]) / 100.0;
					
					if(ch.length == 1)
						chances[2] = chances[1] = chances[0];
					else if(ch.length == 2)
						chances[2] = chances[1];
				}
				catch(NumberFormatException nfe) {}
				
				drop = drop.substring(0, m) + drop.substring(n+1, drop.length());
			}
			
			itemName = drop.trim();
			String[] spl = itemName.split(":");
			
			String modName = spl[0];
			if(spl.length == 1)
				modName = "minecraft";
			
			Item item = (Item)(Item.itemRegistry.containsKey(itemName) ? Item.itemRegistry.getObject(itemName) : GameRegistry.findItem(modName, spl[spl.length-1]));
			
			if(item != null)
			{
				list.add(new Drop(new ItemStack(item, amount), special, chances));
			}
		}
		
		list.sort(null);
		
		this.drops = list;
	}
	
	public String toString()
	{
		String ret = "";
		
		for(int i = 0; i < drops.size(); i++)
		{
			Drop drop = drops.get(i);
			
			String name = Item.itemRegistry.getNameForObject(drop.item.getItem());
			
			if(name == null)
				name = drop.item.getItem().getUnlocalizedName();
			
			if(name == null)
				continue;
			
			ret += name + (drop.standalone ? "*" : "") + (drop.item.stackSize > 1 ? "["+drop.item.stackSize+"]" : "") + "(" + HelperMethods.formatChanceStr(100*drop.chances[0]) + " " + HelperMethods.formatChanceStr(100*drop.chances[1]) + " " + HelperMethods.formatChanceStr(100*drop.chances[2]) + ")";
			
			if(i < drops.size()-1)
				ret += ", ";
		}
		
		return ret;
	}
	
	public String[] toNiceStrings()
	{
		String[] ret = new String[drops.size()];
		
		for(int i = 0; i < ret.length; i++)
		{
			ret[i] = drops.get(i).toString();
		}
		
		return ret;
	}
	
	/**
	 * Note: this class has a natural ordering that is inconsistent with equals
	 */
	public static class Drop implements Comparable<Drop>
	{
		private ItemStack item;//item info and base quantity (actual quantity may be higher)
		private boolean standalone;//if true, getting this drop will prevent other drops from the same steal cast
		private double[] chances;//up to 3 of these, one for each possible level of fortune/looting, [0] being base chance
		
		private Drop(ItemStack item, boolean standalone, double... chances)
		{
			this.item = item;
			this.standalone = standalone;
			this.chances = chances;
		}
		
		public boolean roll(int fortune)//rolls the dice for a given fortune level and returns true if it's a win
		{
			return Math.random() < chances[fortune];
		}
		
		public ItemStack getItem()
		{
			return item;
		}
		
		public boolean isSpecial()
		{
			return this.standalone;
		}

		@Override
		public int compareTo(Drop o)
		{
			if(this.standalone && !o.standalone)
				return -1;
			if(o.standalone && !this.standalone)
				return 1;
			
			if(this.chances[2] < o.chances[2])//compare highest-level looting drop rate
				return -1;
			if(this.chances[2] > o.chances[2])//compare highest-level looting drop rate
				return 1;
			
			//no other checks necessary
			
			return 0;
		}
		
		@Override
		public String toString()
		{
			String name = Item.itemRegistry.getNameForObject(item.getItem());
			
			if(name == null)
				name = item.getItem().getUnlocalizedName();
			
			if(name == null)
				return "null";
			
			if(name.startsWith("minecraft:"))
				name = name.substring(10);
			
			if(name.length() > 1 && Character.isAlphabetic(name.charAt(0)))
				name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
			
			return name + (item.stackSize > 1 ? " x"+item.stackSize : "") + (standalone ? " (unique) :" : " :") + " Drop chance with luck 0/1/2 is " + HelperMethods.formatChanceStr(100*chances[0]) + "/" + HelperMethods.formatChanceStr(100*chances[1]) + "/" + HelperMethods.formatChanceStr(100*chances[2]) + "%";
		}
	}
}
