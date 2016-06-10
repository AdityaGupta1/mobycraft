package org.redfrog404.mobycraft.structure;

import static org.redfrog404.mobycraft.utils.MessageSender.sendMessage;

import java.util.ArrayList;
import java.util.List;

import org.redfrog404.mobycraft.api.MobycraftContainerListCommands;
import org.redfrog404.mobycraft.main.Mobycraft;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Vec3i;
import net.minecraft.world.World;

public class StructureBuilder {

	private static void fill(World world, BlockPos start, BlockPos end,
			Block material) {
		int startX = start.getX();
		int endX = end.getX();
		int startY = start.getY();
		int endY = end.getY();
		int startZ = start.getZ();
		int endZ = end.getZ();

		int[] intSwitchArray = new int[2];

		if (endX < startX) {
			intSwitchArray = switchNumbers(startX, endX);
			startX = intSwitchArray[0];
			endX = intSwitchArray[1];
		}

		if (endY < startY) {
			intSwitchArray = switchNumbers(startY, endY);
			startY = intSwitchArray[0];
			endY = intSwitchArray[1];
		}

		if (endZ < startZ) {
			intSwitchArray = switchNumbers(startZ, endZ);
			startZ = intSwitchArray[0];
			endZ = intSwitchArray[1];
		}

		for (int x = startX; x < endX + 1; x++) {
			for (int y = startY; y < endY + 1; y++) {
				for (int z = startZ; z < endZ + 1; z++) {
					world.setBlockState(new BlockPos(x, y, z),
							material.getDefaultState());
				}
			}
		}
	}

	public static void room(World world, BlockPos start, BlockPos end,
			Block material) {
		fill(world, start, end, material);

		int airStartX = -(start.getX() - end.getX())
				/ Math.abs(start.getX() - end.getX());
		int airEndX = -(end.getX() - start.getX())
				/ Math.abs(end.getX() - start.getX());
		int airStartY = -(start.getY() - end.getY())
				/ Math.abs(start.getY() - end.getY());
		int airEndY = -(end.getY() - start.getY())
				/ Math.abs(end.getY() - start.getY());
		int airStartZ = -(start.getZ() - end.getZ())
				/ Math.abs(start.getZ() - end.getZ());
		int airEndZ = -(end.getZ() - start.getZ())
				/ Math.abs(end.getZ() - start.getZ());

		fill(world, start.add(airStartX, airStartY, airStartZ),
				end.add(airEndX, airEndY, airEndZ), Blocks.air);
	}

	public static void container(BoxContainer container, MobycraftContainerListCommands listCommands) {
		BlockPos start = container.getPosition();
		World world = container.getWorld();
		String containerName = container.getName();
		String containerImage = container.getImage();
		String containerID = container.getID();
		double memoryUsage = container.getMemoryUsage(listCommands);
		double cpuUsage = container.getCpuUsage(listCommands);

		// Iron room
		start = start.add(-2, 0, 1);
		room(world, start, start.add(4, 4, 4), Blocks.iron_block);

		// Logo block and entrance
		world.setBlockState(start.add(2, 3, 0),
				Mobycraft.docker_block.getDefaultState());
		fill(world, start.add(2, 2, 0), start.add(2, 1, 0), Blocks.air);

		// Start/Stop Container sign and its command block
		world.setBlockState(start.add(3, 2, 3),
				Blocks.stone_button.getDefaultState());
		IBlockState wallSign = Blocks.wall_sign.getDefaultState();
		world.setBlockState(start.add(3, 3, 3), wallSign);
		TileEntitySign sign = ((TileEntitySign) world.getTileEntity(start.add(
				3, 3, 3)));
		sign.signText[1] = new ChatComponentText(EnumChatFormatting.GREEN + ""
				+ EnumChatFormatting.BOLD + "Start" + EnumChatFormatting.BLACK
				+ "" + EnumChatFormatting.BOLD + " / " + EnumChatFormatting.RED
				+ "" + EnumChatFormatting.BOLD + "Stop");
		sign.signText[2] = new ChatComponentText(EnumChatFormatting.BLACK + ""
				+ EnumChatFormatting.BOLD + "Container");
		room(world, start.add(0, 0, 6), start.add(4, 4, 4), Blocks.iron_block);
		world.setBlockState(start.add(3, 2, 5),
				Blocks.command_block.getDefaultState());
		TileEntityCommandBlock commandBlock = (TileEntityCommandBlock) world
				.getTileEntity(start.add(3, 2, 5));
		commandBlock.getCommandBlockLogic().setCommand(
				"/docker switch_state " + containerID);

		// Detailed Information sign and its command block
		world.setBlockState(start.add(1, 2, 3),
				Blocks.stone_button.getDefaultState());
		world.setBlockState(start.add(1, 3, 3), wallSign);
		sign = ((TileEntitySign) world.getTileEntity(start.add(1, 3, 3)));
		sign.signText[1] = new ChatComponentText(EnumChatFormatting.BLACK + ""
				+ EnumChatFormatting.BOLD + "Detailed");
		sign.signText[2] = new ChatComponentText(EnumChatFormatting.BLACK + ""
				+ EnumChatFormatting.BOLD + "Information");
		world.setBlockState(start.add(1, 2, 5),
				Blocks.command_block.getDefaultState());
		commandBlock = (TileEntityCommandBlock) world.getTileEntity(start.add(
				1, 2, 5));
		commandBlock.getCommandBlockLogic().setCommand(
				"/docker show_detailed_info " + containerID);

		// Name sign
		world.setBlockState(start.add(4, 1, -1), wallSign);
		sign = ((TileEntitySign) world.getTileEntity(start.add(4, 1, -1)));
		sign.signText[0] = new ChatComponentText(EnumChatFormatting.BOLD
				+ "Name:");
		wrapSignText(containerName, sign);

		// Image sign
		world.setBlockState(start.add(3, 1, -1), wallSign);
		sign = ((TileEntitySign) world.getTileEntity(start.add(3, 1, -1)));
		sign.signText[0] = new ChatComponentText(EnumChatFormatting.BOLD
				+ "Image:");
		wrapSignText(containerImage, sign);

		// Memory sign
		world.setBlockState(start.add(1, 2, -1), wallSign);
		sign = ((TileEntitySign) world.getTileEntity(start.add(1, 2, -1)));
		sign.signText[1] = new ChatComponentText(EnumChatFormatting.BOLD
				+ "Memory Usage");

		// CPU sign
		world.setBlockState(start.add(1, 1, -1), wallSign);
		sign = ((TileEntitySign) world.getTileEntity(start.add(1, 1, -1)));
		sign.signText[1] = new ChatComponentText(EnumChatFormatting.BOLD
				+ "CPU Usage");
		
		List<Block> usageScale = new ArrayList<Block>();
		usageScale.add(Blocks.packed_ice);
		usageScale.add(Blocks.stone);
		usageScale.add(Blocks.sand);
		usageScale.add(Blocks.netherrack);
		usageScale.add(Blocks.bedrock);
		
		sendMessage(memoryUsage);
		sendMessage(cpuUsage);
		
		if (memoryUsage >= 40) {
			world.setBlockState(start.add(0, 2, 0), usageScale.get(4).getDefaultState());
		} else if (memoryUsage >= 30) {
			world.setBlockState(start.add(0, 2, 0), usageScale.get(3).getDefaultState());
		} else if (memoryUsage >= 20) {
			world.setBlockState(start.add(0, 2, 0), usageScale.get(2).getDefaultState());
		} else if (memoryUsage >= 10) {
			world.setBlockState(start.add(0, 2, 0), usageScale.get(1).getDefaultState());
		} else {
			world.setBlockState(start.add(0, 2, 0), usageScale.get(0).getDefaultState());
		}
		
		if (cpuUsage >= 40) {
			world.setBlockState(start.add(0, 1, 0), usageScale.get(4).getDefaultState());
		} else if (cpuUsage >= 30) {
			world.setBlockState(start.add(0, 1, 0), usageScale.get(3).getDefaultState());
		} else if (cpuUsage >= 20) {
			world.setBlockState(start.add(0, 1, 0), usageScale.get(2).getDefaultState());
		} else if (cpuUsage >= 10) {
			world.setBlockState(start.add(0, 1, 0), usageScale.get(1).getDefaultState());
		} else {
			world.setBlockState(start.add(0, 1, 0), usageScale.get(0).getDefaultState());
		}

		// Glowstone lighting
		IBlockState glowstone = Blocks.glowstone.getDefaultState();
		Vec3i[] addVectors = { new Vec3i(3, 0, 1), new Vec3i(1, 0, 1),
				new Vec3i(3, 0, 3), new Vec3i(1, 0, 3), new Vec3i(3, 0, 5),
				new Vec3i(1, 0, 5), new Vec3i(3, 4, 1), new Vec3i(1, 4, 1),
				new Vec3i(3, 4, 3), new Vec3i(1, 4, 3), new Vec3i(3, 4, 5),
				new Vec3i(1, 4, 5) };
		for (Vec3i vector : addVectors) {
			world.setBlockState(start.add(vector), glowstone);
		}
	}

	private static void wrapSignText(String containerProperty,
			TileEntitySign sign) {
		if (containerProperty.length() < 14) {
			sign.signText[1] = new ChatComponentText(containerProperty);
		} else if (containerProperty.length() < 27) {
			sign.signText[1] = new ChatComponentText(
					containerProperty.substring(0, 13));
			sign.signText[2] = new ChatComponentText(
					containerProperty.substring(13, containerProperty.length()));
		} else {
			sign.signText[1] = new ChatComponentText(
					containerProperty.substring(0, 13));
			sign.signText[1] = new ChatComponentText(
					containerProperty.substring(13, 26));
			sign.signText[2] = new ChatComponentText(
					containerProperty.substring(26, containerProperty.length()));
		}
	}

	private static int[] switchNumbers(int num1, int num2) {
		int[] ints = { num2, num1 };
		return ints;
	}

	public static void replace(World world, BlockPos start, BlockPos end,
			Block blockToReplace, Block blockToReplaceWith) {
		int startX = start.getX();
		int endX = end.getX();
		int startY = start.getY();
		int endY = end.getY();
		int startZ = start.getZ();
		int endZ = end.getZ();

		int[] intSwitchArray = new int[2];

		if (endX < startX) {
			intSwitchArray = switchNumbers(startX, endX);
			startX = intSwitchArray[0];
			endX = intSwitchArray[1];
		}

		if (endY < startY) {
			intSwitchArray = switchNumbers(startY, endY);
			startY = intSwitchArray[0];
			endY = intSwitchArray[1];
		}

		if (endZ < startZ) {
			intSwitchArray = switchNumbers(startZ, endZ);
			startZ = intSwitchArray[0];
			endZ = intSwitchArray[1];
		}

		for (int x = startX; x < endX + 1; x++) {
			for (int y = startY; y < endY + 1; y++) {
				for (int z = startZ; z < endZ + 1; z++) {
					if (world.getBlockState(new BlockPos(x, y, z)) == blockToReplace
							.getDefaultState()) {
						world.setBlockState(new BlockPos(x, y, z),
								blockToReplaceWith.getDefaultState());

					}
				}
			}
		}
	}

	public static void airContainer(World world, BlockPos start) {
		start = start.add(-2, 0, 0);
		fill(world, start, start.add(4, 4, 7), Blocks.air);
	}
}