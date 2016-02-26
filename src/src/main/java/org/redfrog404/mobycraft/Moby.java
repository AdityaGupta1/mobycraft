package org.redfrog404.mobycraft;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = Moby.MODID, version = Moby.VERSION)
public class Moby
{
    public static final String MODID = "mobycraft";
    public static final String VERSION = "1.0";
    
    @EventHandler
    public void init(FMLServerStartingEvent event)
    {
      event.registerServerCommand(new DockerCommands());
    }
}
