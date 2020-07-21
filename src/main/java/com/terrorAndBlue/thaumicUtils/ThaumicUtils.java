package com.terrorAndBlue.thaumicUtils;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.RecipeSorter.Category;
import thaumcraft.api.ItemApi;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.api.crafting.InfusionRecipe;
import thaumcraft.api.crafting.ShapelessArcaneRecipe;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchCategoryList;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.api.research.ResearchPage;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.items.ItemCrystalEssence;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import com.terrorAndBlue.thaumicUtils.command.ExtraFociDebugCommand;
import com.terrorAndBlue.thaumicUtils.entity.EntityReflectShieldVisual;
import com.terrorAndBlue.thaumicUtils.handlers.HandlerUtils;
import com.terrorAndBlue.thaumicUtils.handlers.LivingEventHandler;
import com.terrorAndBlue.thaumicUtils.handlers.WorldEventHandler;
import com.terrorAndBlue.thaumicUtils.items.ItemFocusReflectShield;
import com.terrorAndBlue.thaumicUtils.items.ItemFocusSteal;
import com.terrorAndBlue.thaumicUtils.network.CreateReflectShieldVisualPacket;
import com.terrorAndBlue.thaumicUtils.network.StealPacket;
import com.terrorAndBlue.thaumicUtils.proxy.CommonProxy;
import com.terrorAndBlue.thaumicUtils.research.ResearchItemMain;
import com.terrorAndBlue.thaumicUtils.util.CrystalEssenceRecipe;
import com.terrorAndBlue.thaumicUtils.util.DropList;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = ThaumicUtils.MODID, version = ThaumicUtils.VERSION,
	name = ThaumicUtils.NAME)
public class ThaumicUtils
{
	public static final String RESEARCH_TAB = "THAUMIC_UTILS";
	
	public static final String MODID = "ThaumicUtils";
	public static final String NAME = "Thaumic Utilities";
	public static final String VERSION = "1.0";
	
	private static final String[] DROP_TABLE_DEFAULTS = new String[]
	{
		"Zombie="+Item.itemRegistry.getNameForObject(Items.apple)+"*[2](50), "+Item.itemRegistry.getNameForObject(Items.carrot),
	};
	
	public static final HashMap<String, DropList> DROP_TABLE = new HashMap<String, DropList>();

	public static Item wandFocusReflect;
	public static Item wandFocusSteal;

	public static ItemCrystalEssence crystalEss;
	
	private static CrystalEssenceRecipe[] crysEssRecipes;
	private static InfusionRecipe reflectFociRecipe;

	@SidedProxy(clientSide="com.terrorAndBlue.thaumicUtils.proxy.ClientProxy", serverSide="com.terrorAndBlue.thaumicUtils.proxy.CommonProxy")
	public static CommonProxy proxy;
	public static SimpleNetworkWrapper wrapper;
	
	
	public static String[] loadCfgFile()
	{
		Configuration config = new Configuration(new File("config/ThaumicUtils.cfg"));

		config.load();
		Property steal_drop_table_cfg = config.get("Server Configs", "Steal Focus Drop Table", DROP_TABLE_DEFAULTS, "Syntax: <Entity name>=drop1,drop2,...etc. Drops have the following format: item name [amount] (chance1 chance2 chance3). Chance 2 and 3 correspond to the chance with looting 1 and 2. You can add * after item name to make it a special drop. These will prevent any further drops on a successful roll.");
		String[] arr = steal_drop_table_cfg.getStringList();
		for(int i = 0; i < arr.length; i++)
		{
			String[] spl = arr[i].split("=");
			
			if(spl.length != 2)
				continue;
			
			String entityName = spl[0].trim();
			String dropsInfo = spl[1];
			
			DropList list = new DropList(dropsInfo);
			DROP_TABLE.put(entityName, list);
			arr[i] = entityName + "=" + list;
		}
		steal_drop_table_cfg.set(arr);
		config.save();
		return arr;
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		EntityRegistry.registerGlobalEntityID(EntityReflectShieldVisual.class, "entityReflectShieldVisual", EntityRegistry.findGlobalUniqueEntityId());
		wrapper = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
		wrapper.registerMessage(CreateReflectShieldVisualPacket.Handler.class, CreateReflectShieldVisualPacket.class, 0, Side.CLIENT);
		wrapper.registerMessage(StealPacket.Handler.class, StealPacket.class, 1, Side.SERVER);
		proxy.preInit();
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		wandFocusReflect = new ItemFocusReflectShield();
		wandFocusSteal = new ItemFocusSteal();

		GameRegistry.registerItem(wandFocusReflect, "wandFocusReflect");
		GameRegistry.registerItem(wandFocusSteal, "wandFocusSteal");
		//TODO wand focus imprison? Prevents movement with nice visual
		
		AspectList aspList = new AspectList().add(Aspect.MAGIC, 32).add(Aspect.WATER, 16).add(Aspect.FIRE, 16).add(Aspect.ORDER, 16);
		ItemStack[] infusionItems = new ItemStack[] {
						new ItemStack(Blocks.brick_block),
						new ItemStack(Items.glowstone_dust),
						new ItemStack(Blocks.cactus),
						new ItemStack(Items.glowstone_dust),
						new ItemStack(Blocks.brick_block),
						new ItemStack(Items.glowstone_dust),
						new ItemStack(Blocks.cactus),
						new ItemStack(Items.glowstone_dust)
		};
		
		//TODO config options for reflect?
		reflectFociRecipe = ThaumcraftApi.addInfusionCraftingRecipe("extra_foci_reflect", new ItemStack(wandFocusReflect), 2, aspList, new ItemStack(Items.diamond), infusionItems);
		
		MinecraftForge.EVENT_BUS.register(new LivingEventHandler());
		MinecraftForge.EVENT_BUS.register(new WorldEventHandler());
		//FMLCommonHandler.instance().bus().register(new PlayerTickHandler());

		ItemStack essence = ItemApi.getItem("itemCrystalEssence", 0);
		crystalEss = (ItemCrystalEssence)essence.getItem();

		if(essence != null)//TODO config file option to disable this?
		{
			List<Aspect> aspects = Aspect.getCompoundAspects();
			crysEssRecipes = new CrystalEssenceRecipe[aspects.size()];
			for(int i = 0; i < crysEssRecipes.length; i++)
			{
				Aspect[] comps = aspects.get(i).getComponents();
				
				if(comps.length >= 2)
				{
					Aspect cp1 = comps[0];
					Aspect cp2 = comps[1];
					
					if(cp1 != null && cp2 != null)
					{
						ItemStack essence_cp1 = essence.copy();
						crystalEss.setAspects(essence_cp1, new AspectList().add(cp1, 1));
						ItemStack essence_cp2 = essence.copy();
						crystalEss.setAspects(essence_cp2, new AspectList().add(cp2, 1));
						ItemStack essence_result = essence.copy();
						crystalEss.setAspects(essence_result, new AspectList().add(aspects.get(i), 1));
						
						crysEssRecipes[i] = new CrystalEssenceRecipe("extra_foci_crysEss_combining", essence_result, CrystalEssenceRecipe.getPrimals(new AspectList().add(aspects.get(i),1)), essence_cp1, essence_cp2);
						ThaumcraftApi.getCraftingRecipes().add(crysEssRecipes[i]);
					}
				}
			}
		}

		RecipeSorter.register("crystalEssenceRecipe", CrystalEssenceRecipe.class, Category.SHAPELESS, "");

		proxy.init();
	}

	@EventHandler
	public void serverLoad(FMLServerStartingEvent event)
	{
		loadCfgFile();
		event.registerServerCommand(new ExtraFociDebugCommand());
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		//start of research registration
		ResearchCategories.registerCategory(RESEARCH_TAB, new ResourceLocation(MODID+":textures/research/1.png"), new ResourceLocation(MODID+":textures/research/2.png"));

		ResearchItem main = new ResearchItemMain();
		main.registerResearchItem();

		ResearchItem reflectFocusResearch = new ResearchItem("extra_foci_reflect", RESEARCH_TAB, new AspectList().add(Aspect.ARMOR, 1).add(Aspect.WEAPON, 1).add(Aspect.AURA, 1).add(Aspect.EXCHANGE, 1), 2, 3, 2, new ItemStack(wandFocusReflect));
		reflectFocusResearch.setParents(main.key);
		ResearchPage[] reflectPages = new ResearchPage[2];
		reflectPages[0] = new ResearchPage("You don't know why you've never thought of this before... A shield to reflect incoming attacks in a frontal cone! Simply hold to create a barrier in the direction you're facing to block all damage and reflect some of it back to the attacker.");
		if(reflectFociRecipe != null)
			reflectPages[1] = new ResearchPage(reflectFociRecipe);
		else
			reflectPages[1] = new ResearchPage("This recipe has been disabled. If you're on a server, contact admin to find out why.");
		reflectFocusResearch.setPages(reflectPages);
		reflectFocusResearch.registerResearchItem();

		ResearchItem essCombineResearch = new ResearchItem("extra_foci_crysEss_combining", RESEARCH_TAB, new AspectList().add(Aspect.MAGIC, 5).add(Aspect.MIND, 10), 5, 6, 0, new ResourceLocation(MODID+":textures/research/essence_combining.png"));
		essCombineResearch.setParents(main.key);
		ResearchPage[] essCombinePages = new ResearchPage[2];
		essCombinePages[0] = new ResearchPage("You have discovered how to combine crystallized essence in an arcane crafting table. Imagine what else you could learn by applying common sense to your previous magical creations...");
		if(crysEssRecipes != null)
			essCombinePages[1] = new ResearchPage(crysEssRecipes);
		else
			essCombinePages[1] = new ResearchPage("This recipe has been disabled. If you're on a server, contact admin to find out why.");
		essCombineResearch.setPages(essCombinePages);
		essCombineResearch.setSecondary();
		essCombineResearch.registerResearchItem();

		proxy.postInit();
	}
}
