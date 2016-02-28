package org.redfrog404.mobycraft;

import java.io.File;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = Moby.MODID, version = Moby.VERSION)
public class Moby
{
    public static final String MODID = "mobycraft";
    public static final String VERSION = "1.0";
    
    public static File configFile;
    public static Configuration config;
    public static String category = "files";
    public static String key = "docker-cert-path";
    public static String defaultValue = "TODO";
    public static String comment = "The location of your Docker stuff";
    
    @EventHandler
    public void init(FMLServerStartingEvent event)
    {
      event.registerServerCommand(new DockerCommands());
    }
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event){
    	configFile = new File(event.getModConfigurationDirectory().toString() + "/mobycraft/mobycraft.txt");
    	config = new Configuration(configFile);
    	config.load();
    	config.getString(key, category, defaultValue, comment);
    	config.save();
    }
}
