package org.redfrog404.mobycraft;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Vec3i;
import net.minecraft.world.World;

import com.github.dockerjava.api.model.Container;

public class StructureBuilder {

	public void fill(World world, BlockPos start, BlockPos end, Block material) {
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

	public void room(World world, BlockPos start, BlockPos end, Block material) {
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

	public void container(World world, BlockPos start, Block material,
			String containerName, String containerImage) {
		start = start.add(-2, 0, 1);
		room(world, start, start.add(4, 4, 4), Blocks.iron_block);

		world.setBlockState(start.add(2, 3, 0),
				Moby.docker_block.getDefaultState());
		fill(world, start.add(2, 2, 0), start.add(2, 1, 0), Blocks.air);

		IBlockState glowstone = Blocks.glowstone.getDefaultState();
		Vec3i[] addVectors = { new Vec3i(3, 0, 1), new Vec3i(1, 0, 1),
				new Vec3i(3, 0, 3), new Vec3i(1, 0, 3), new Vec3i(3, 4, 1),
				new Vec3i(1, 4, 1), new Vec3i(3, 4, 3), new Vec3i(1, 4, 3) };
		for (Vec3i vector : addVectors) {
			world.setBlockState(start.add(vector), glowstone);
		}

		world.setBlockState(start.add(2, 2, 3), Blocks.lever.getDefaultState());
		IBlockState wallSign = Blocks.wall_sign.getDefaultState();
		world.setBlockState(start.add(1, 2, 3), wallSign);
		world.setBlockState(start.add(2, 3, 3), wallSign);
		world.setBlockState(start.add(3, 2, 3), wallSign);
		TileEntitySign sign = ((TileEntitySign) world.getTileEntity(start.add(
				2, 3, 3)));
		sign.signText[1] = new ChatComponentText(EnumChatFormatting.GREEN + ""
				+ EnumChatFormatting.BOLD + "Start" + EnumChatFormatting.BLACK
				+ "" + EnumChatFormatting.BOLD + " / " + EnumChatFormatting.RED
				+ "" + EnumChatFormatting.BOLD + "Stop");
		sign.signText[2] = new ChatComponentText(EnumChatFormatting.BLACK + ""
				+ EnumChatFormatting.BOLD + "Container");
		((TileEntitySign) world.getTileEntity(start.add(3, 2, 3))).signText[1] = new ChatComponentText(
				EnumChatFormatting.GREEN + "" + EnumChatFormatting.BOLD
						+ "Start = Up");
		((TileEntitySign) world.getTileEntity(start.add(1, 2, 3))).signText[1] = new ChatComponentText(
				EnumChatFormatting.RED + "" + EnumChatFormatting.BOLD
						+ "Stop = Down");

		world.setBlockState(start.add(4, 1, -1), wallSign);
		sign = ((TileEntitySign) world.getTileEntity(start.add(4, 1, -1)));
		sign.signText[0] = new ChatComponentText(EnumChatFormatting.BOLD
				+ "Name:");
		wrapSignText(containerName, sign);

		world.setBlockState(start.add(3, 1, -1), wallSign);
		sign = ((TileEntitySign) world.getTileEntity(start.add(3, 1, -1)));
		sign.signText[0] = new ChatComponentText(EnumChatFormatting.BOLD
				+ "Image:");
		wrapSignText(containerImage, sign);

	}

	private void wrapSignText(String containerProperty, TileEntitySign sign) {
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

	public int[] switchNumbers(int num1, int num2) {
		int[] ints = { num2, num1 };
		return ints;
	}

	public List<BoxContainer> containerColumn(List<Container> containers,
			int index, BlockPos pos) {

		List<BoxContainer> boxContainers = new ArrayList<BoxContainer>();

		int endIndex = 10;

		if (containers.size() - (index * 10) < 10) {
			endIndex = containers.size() - index * 10;
		}

		for (int i = index * 10; i < (index * 10) + endIndex; i++) {
			Container container = containers.get(i);
			boxContainers.add(new BoxContainer(pos, container.getId(),
					container.getNames()[0], container.getImage()));
			pos = pos.add(0, 6, 0);
		}

		return boxContainers;
	}

	public List<BoxContainer> containerPanel(List<Container> containers,
			BlockPos pos) {
		List<BoxContainer> boxContainers = new ArrayList<BoxContainer>();

		int lastIndex = (containers.size() - (containers.size() % 10)) / 10;
		for (int i = 0; i <= lastIndex; i++) {
			boxContainers.addAll(containerColumn(containers, i, pos));
			pos = pos.add(6, 0, 0);
		}

		return boxContainers;
	}

}
