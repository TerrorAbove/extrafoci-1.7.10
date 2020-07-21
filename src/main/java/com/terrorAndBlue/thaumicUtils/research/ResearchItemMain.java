package com.terrorAndBlue.thaumicUtils.research;

import com.terrorAndBlue.thaumicUtils.ThaumicUtils;

import net.minecraft.util.ResourceLocation;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.api.research.ResearchPage;

public class ResearchItemMain extends ResearchItem
{
	public ResearchItemMain()
	{
		super("extra_foci_info", ThaumicUtils.RESEARCH_TAB, new AspectList(), 4, 4, 0, new ResourceLocation(ThaumicUtils.MODID+":textures/research/main_node_icon.png"));
		setAutoUnlock();
		setPages(new ResearchPage[]{
				new ResearchPage("Thus far you have seen many foci for your wands and staves. Recently you had a revelation that many more could be discovered, if one only looked in the right places..."),
				new ResearchPage("You also had an idea to try to combine crystallized essence shards in an arcane workbench. It's about time someone found a use for these. You're sure you will find many more common-sense applications of magic in the future.")
		});
	}
}
