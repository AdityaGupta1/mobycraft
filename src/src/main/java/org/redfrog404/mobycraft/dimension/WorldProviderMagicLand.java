package org.redfrog404.mobycraft.dimension;

import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManagerHell;
import net.minecraft.world.chunk.IChunkProvider;

public class WorldProviderMagicLand extends WorldProvider {
	
	public void registerWorldChunkManager(){
		this.worldChunkMgr = new WorldChunkManagerHell(BiomeGenBase.ocean, 0F);
		this.dimensionId = DimensionRegistry.magicLandID;
	}
	
	public IChunkProvider createChunkGeneration() {
		return null;
	}

	@Override
	public String getDimensionName() {
		return "magic_land";
	}

	@Override
	public String getInternalNameSuffix() {
		return "dimension";
	}

}