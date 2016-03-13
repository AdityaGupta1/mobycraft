package org.redfrog404.mobycraft;

import java.io.File;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

import org.redfrog404.mobycraft.dimension.DimensionRegistry;
import org.redfrog404.mobycraft.dimension.TeleporterMagicLand;
import org.redfrog404.mobycraft.entity.EntityDockerWhale;
import org.redfrog404.mobycraft.entity.RenderDockerWhale;

@Mod(modid = Moby.MODID, version = Moby.VERSION)
public final class Moby {
	public static final String MODID = "moby";
	public static final String VERSION = "1.0";

	public static Configuration config;

	public static Block docker_block;

	ItemModelMesher mesher;

	public static final StructureBuilder builder = new StructureBuilder();

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

		RenderManager render = Minecraft.getMinecraft().getRenderManager();

		registerModEntity(EntityDockerWhale.class, new RenderDockerWhale(),
				"docker_whale", EntityRegistry.findGlobalUniqueEntityId(),
				0x24B8EB, 0x008BB8);

		MinecraftForge.EVENT_BUS.register(this);

		DimensionRegistry.mainRegistry();
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

	public void registerModEntity(Class parEntityClass, Render render,
			String parEntityName, int entityId, int foregroundColor,
			int backgroundColor) {
		EntityRegistry.registerGlobalEntityID(parEntityClass, parEntityName,
				entityId, foregroundColor, backgroundColor);
		EntityRegistry.registerModEntity(parEntityClass, parEntityName,
				entityId, this, 80, 1, false);
		RenderingRegistry
				.registerEntityRenderingHandler(parEntityClass, render);
	}

	@SubscribeEvent
	public void teleportToMagicLand(LivingJumpEvent event) {
		if (!(event.entity instanceof EntityPlayerMP)) {
			return;
		}

		EntityPlayerMP player = (EntityPlayerMP) event.entity;

		player.mcServer
				.getConfigurationManager()
				.transferPlayerToDimension(
						player,
						DimensionRegistry.magicLandID,
						new TeleporterMagicLand(
								player.mcServer
										.worldServerForDimension(DimensionRegistry.magicLandID)));

		player.setLocationAndAngles(0, 70, 0, 0, 0);
		player.worldObj.setBlockState(new BlockPos(0, 70, 0),
				Blocks.diamond_block.getDefaultState());
	}
}
