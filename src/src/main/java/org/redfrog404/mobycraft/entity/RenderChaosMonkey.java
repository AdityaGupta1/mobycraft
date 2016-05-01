package org.redfrog404.mobycraft.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderChaosMonkey extends RenderLiving
{
    private static final ResourceLocation monkeyTextures = new ResourceLocation("moby:textures/entity/chaos_monkey.png");

    public RenderChaosMonkey()
    {
        super(Minecraft.getMinecraft().getRenderManager(),
				new ModelChaosMonkey(), 1.0F);
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    protected ResourceLocation getEntityTexture(EntityChaosMonkey entity)
    {
        return monkeyTextures;
    }
    
    protected ResourceLocation getEntityTexture(Entity entity) {
		return this.getEntityTexture((EntityChaosMonkey) entity);
	}
}