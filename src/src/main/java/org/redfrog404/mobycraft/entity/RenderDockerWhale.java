package org.redfrog404.mobycraft.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderDockerWhale extends RenderLiving
{
    private static final ResourceLocation whaleTextures = new ResourceLocation("moby:textures/entity/docker_whale.png");

    public RenderDockerWhale()
    {
        super(Minecraft.getMinecraft().getRenderManager(),
				new ModelDockerWhale(), 1.0F);
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    protected ResourceLocation getEntityTexture(EntityDockerWhale entity)
    {
        return whaleTextures;
    }
    
    protected ResourceLocation getEntityTexture(Entity entity) {
		return this.getEntityTexture((EntityDockerWhale) entity);
	}
}