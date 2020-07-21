package com.terrorAndBlue.thaumicUtils.client;

import org.lwjgl.opengl.GL11;

import com.terrorAndBlue.thaumicUtils.ThaumicUtils;
import com.terrorAndBlue.thaumicUtils.entity.EntityReflectShieldVisual;
import com.terrorAndBlue.thaumicUtils.network.CreateReflectShieldVisualPacket;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import thaumcraft.codechicken.lib.math.MathHelper;

public class ReflectShieldVisualRender extends Render
{
	public static final ResourceLocation SHIELD_TEXTURE = new ResourceLocation(ThaumicUtils.MODID+":textures/rendering/reflect_large_circle.png");
	
	private static final int SPREAD_ANGLE = 30;
	
	public ReflectShieldVisualRender()
	{
		super();
	}
	
	@Override
	public void doRender(Entity entity, double x, double y, double z, float f0, float rand)
	{
		if(entity != CreateReflectShieldVisualPacket.ACTIVE_SHIELDS.get(Minecraft.getMinecraft().thePlayer))
		{
			y += Minecraft.getMinecraft().thePlayer.getDefaultEyeHeight();
		}
		GL11.glPushMatrix();
		GL11.glTranslated(x-0.5, y-0.5, z-0.5);
		if(((EntityReflectShieldVisual)entity).renderHurtVisual())
			GL11.glColor4d(1, 0.5, 0.5, 1);
		else
			GL11.glColor4d(1, 1, 1, 0.5);
		GL11.glDepthMask(false);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glDisable(GL11.GL_CULL_FACE);
		bindTexture(SHIELD_TEXTURE);
		Tessellator tes = Tessellator.instance;
		
		//rotate to account for where the player is looking
		float yaw = entity.rotationYaw;
		float pitch = entity.rotationPitch;
		GL11.glTranslated(0.5, 0.5, 0.5);
		GL11.glRotated(-yaw, 0, 1, 0);
		GL11.glRotated(pitch, 1, 0, 0);
		GL11.glTranslated(-0.5, -0.5, -0.5);
		
		GL11.glTranslated(0.5, 0.5, 0.5);
		GL11.glRotated(3*SPREAD_ANGLE, 0, 1, 0);
		GL11.glTranslated(-0.5, -0.5, -0.5);
		
		for(int i = 0; i < 5; i++)
		{
			GL11.glTranslated(0.5, 0.5, 0.5);
			GL11.glRotated(-SPREAD_ANGLE, 0, 1, 0);
			GL11.glTranslated(-0.5, -0.5, -0.5);
			
			GL11.glTranslated(0.5, 0.5, 0.5);
			GL11.glRotated(3*SPREAD_ANGLE, 1, 0, 0);
			GL11.glTranslated(-0.5, -0.5, -0.5);
			
			for(int j = 0; j < 5; j++)
			{
				GL11.glTranslated(0.5, 0.5, 0.5);
				GL11.glRotated(-SPREAD_ANGLE, 1, 0, 0);
				GL11.glTranslated(-0.5, -0.5, -0.5);
				
				tes.startDrawingQuads();
				tes.setNormal(0, 1, 0);
				
				double scaleFactor = (i == 2 && j == 2) ? 0.75 : (i%4 > 0 && j%4 > 0) ? 0.5 : 0.25;
				double min = 0.5 - scaleFactor/2.0;
				double max = 0.5 + scaleFactor/2.0;

				//  X      Y      Z      U       V
				tes.addVertexWithUV(min, max, 2, 0.0, 1.0); // Bottom left
				tes.addVertexWithUV(max, max, 2, 1.0, 1.0); // Bottom right
				tes.addVertexWithUV(max, min, 2, 1.0, 0.0); // Top right
				tes.addVertexWithUV(min, min, 2, 0.0, 0.0); // Top left
				tes.draw();
				
				tes.startDrawingQuads();
				tes.setNormal(0, 1, 0);

				//  X      Y      Z      U       V
				tes.addVertexWithUV(min, min, 1.99, 0, 1); // Bottom Left
				tes.addVertexWithUV(max, min, 1.99, 1, 1); // Bottom Right
				tes.addVertexWithUV(max, max, 1.99, 1, 0); // Top Right
				tes.addVertexWithUV(min, max, 1.99, 0, 0); // Top Left
				tes.draw();
			}
			
			GL11.glTranslated(0.5, 0.5, 0.5);
			GL11.glRotated(2*SPREAD_ANGLE, 1, 0, 0);
			GL11.glTranslated(-0.5, -0.5, -0.5);
		}
		
		GL11.glColor4d(1.0, 1.0, 1.0, 1.0);
		GL11.glDepthMask(true);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glPopMatrix();
	}
	
	@Override
	public ResourceLocation getEntityTexture(Entity entity)
	{
		return SHIELD_TEXTURE;
	}
}
