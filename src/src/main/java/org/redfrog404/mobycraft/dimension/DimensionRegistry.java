package org.redfrog404.mobycraft.dimension;

import net.minecraftforge.common.DimensionManager;

public class DimensionRegistry {
	
	public static void mainRegistry(){
		registerDimensions();
	}
	
	public static final int magicLandID = 13;
	
	public static void registerDimensions(){
		DimensionManager.registerProviderType(magicLandID, WorldProviderMagicLand.class, false);
		DimensionManager.registerDimension(magicLandID, magicLandID);
	}

}