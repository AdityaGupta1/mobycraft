package org.redfrog404.mobycraft.main;

import java.io.File;

import com.google.inject.Guice;
import com.google.inject.Injector;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

import org.redfrog404.mobycraft.commands.common.MainCommand;
import org.redfrog404.mobycraft.dimension.DimensionRegistry;
import org.redfrog404.mobycraft.entity.EntityChaosMonkey;
import org.redfrog404.mobycraft.entity.EntityMoby;
import org.redfrog404.mobycraft.entity.RenderChaosMonkey;
import org.redfrog404.mobycraft.entity.RenderMoby;
import org.redfrog404.mobycraft.structure.GenericBlock;
import org.redfrog404.mobycraft.structure.GenericItem;

@Mod(modid = Mobycraft.MODID, version = Mobycraft.VERSION)
public final class Mobycraft {
	public static final String MODID = "moby";
	public static final String VERSION = "1.0-beta";

	public static Configuration config;

	public static Block docker_block;
	public static Item container_wand;
	public static Item container_essence;
	private static Injector injector;

	ItemModelMesher mesher;

	private static MainCommand commands;

	@EventHandler
	public void registerDockerCommands(FMLServerStartingEvent event) {
		event.registerServerCommand(commands);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		injector = Guice.createInjector(new MobycraftModule());

		mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();

		docker_block = new GenericBlock("docker_block", Material.iron, 5.0F,
				10.0F, "pickaxe", 1, Block.soundTypeMetal);
		registerBlock(docker_block, "docker_block");

		container_wand = new GenericItem("container_wand",
				CreativeTabs.tabTools).setMaxStackSize(1);
		registerItem(container_wand, "container_wand");

		container_essence = new GenericItem("container_essence",
				CreativeTabs.tabMaterials);
		registerItem(container_essence, "container_essence");

		RenderManager render = Minecraft.getMinecraft().getRenderManager();

		registerModEntity(EntityMoby.class, new RenderMoby(), "moby",
				EntityRegistry.findGlobalUniqueEntityId(), 0x24B8EB, 0x008BB8);
		registerModEntity(EntityChaosMonkey.class, new RenderChaosMonkey(),
				"chaos_monkey", EntityRegistry.findGlobalUniqueEntityId(),
				0x8E6400, 0xEAFF00);

		DimensionRegistry.mainRegistry();

		commands = injector.getInstance(MainCommand.class);
		commands.loadConfig();

		MinecraftForge.EVENT_BUS.register(commands);
		FMLCommonHandler.instance().bus().register(commands);
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		File configFile = new File(event.getModConfigurationDirectory()
				.toString() + "/mobycraft/mobycraft.txt");

		config = new Configuration(configFile);
		config.load();
		config.getString(
				"docker-cert-path",
				"files",
				"File path",
				"The directory path of your Docker certificate (set using /docker path <path>); only used if DOCKER_CERT_PATH environment variable is not set");
		config.getString(
				"docker-host",
				"files",
				"Docker host IP",
				"The IP of your Docker host (set using /docker host <host>); only used if DOCKER_HOST environment variable is not set");
		config.getString(
				"start-pos",
				"container-building",
				"0, 0, 0",
				"The position - x, y, z - to start building containers at (set using /docker start_pos)");
		config.getString(
				"poll-rate",
				"container-building",
				"2",
				"The rate in seconds at which the containers will update (set using /docker poll_rate <rate in seconds>)");
		config.save();
	}

	private void registerItem(Item item, String name) {
		GameRegistry.registerItem(item, name);
		mesher.register(item, 0, new ModelResourceLocation("moby:" + name,
				"inventory"));
	}

	private void registerBlock(Block block, String name) {
		GameRegistry.registerBlock(block, name);
		mesher.register(Item.getItemFromBlock(block), 0,
				new ModelResourceLocation("moby:" + name, "inventory"));
	}

	public void registerModEntity(Class entityClass, Render render,
			String entityName, int entityId, int foregroundColor,
			int backgroundColor) {
		EntityRegistry.registerGlobalEntityID(entityClass, entityName,
				entityId, foregroundColor, backgroundColor);
		EntityRegistry.registerModEntity(entityClass, entityName, entityId,
				this, 80, 1, false);
		RenderingRegistry.registerEntityRenderingHandler(entityClass, render);
	}

	public static MainCommand getMainCommand() {
		return commands;
	}

	public static Injector getInjector() {
		return injector;
	}
}
