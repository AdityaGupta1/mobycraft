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
public final class Moby {
	public static final String MODID = "moby";
	public static final String VERSION = "1.0";

	public static Configuration config;

	public static Block docker_block;

	ItemModelMesher mesher;

	public static StructureBuilder builder = new StructureBuilder();

	@EventHandler
	public void registerDockerCommands(FMLServerStartingEvent event) {
		event.registerServerCommand(new DockerCommands());
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();

		docker_block = new GenericBlock("docker_block", Material.iron, 5.0F,
				10.0F, "pickaxe", 1, Block.soundTypeMetal);
		registerBlock(docker_block, "docker_block");
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		File configFile = new File(event.getModConfigurationDirectory()
				.toString() + "/mobycraft/mobycraft.txt");
		config = new Configuration(configFile);
		config.load();
		config.getString("docker-cert-path", "files", "File path",
				"The directory path of your Docker certificate");
		config.save();
	}

	private void registerBlock(Block block, String name) {
		GameRegistry.registerBlock(block, name);
		mesher.register(Item.getItemFromBlock(block), 0,
				new ModelResourceLocation("moby:" + name, "inventory"));
	}
}
