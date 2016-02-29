package org.redfrog404.mobycraft;

import java.io.File;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod(modid = Moby.MODID, version = Moby.VERSION)
public class Moby
{
    public static final String MODID = "moby";
    public static final String VERSION = "1.0";
    
    public static File configFile;
    public static Configuration config;
    
    public static Block logo_block;
    
    ItemModelMesher mesher;
    
    public static Builder builder = new Builder();
    
    @EventHandler
    public void registerDockerCommands(FMLServerStartingEvent event)
    {
      event.registerServerCommand(new DockerCommands());
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
    	mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
        
        logo_block = new GenericBlock("logo_block", Material.iron, 5.0F, 10.0F,
  				"pickaxe", 1, Block.soundTypeMetal);
        registerBlock(logo_block, "logo_block");
    }
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event){
    	configFile = new File(event.getModConfigurationDirectory().toString() + "/mobycraft/mobycraft.txt");
    	config = new Configuration(configFile);
    	config.load();
    	config.getString("docker-cert-path", "files", "File path", "The location of your Docker stuff");
    	config.save();
    }
    
    private void registerBlock(Block block, String name) {
		GameRegistry.registerBlock(block, name);
		mesher.register(Item.getItemFromBlock(block), 0,
				new ModelResourceLocation("moby:" + name, "inventory"));
	}
}
