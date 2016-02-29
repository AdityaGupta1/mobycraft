package org.redfrog404.mobycraft;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class Builder {

	public void fill(World world, BlockPos start, BlockPos end, Block material) {
		int x1 = start.getX();
		int x2 = end.getX();
		int y1 = start.getY();
		int y2 = end.getY();
		int z1 = start.getZ();
		int z2 = end.getZ();

		int[] ints = new int[2];

		if (x2 < x1) {
			ints = switchNumbers(x1, x2);
			x1 = ints[0];
			x2 = ints[1];
		}

		if (y2 < y1) {
			ints = switchNumbers(y1, y2);
			y1 = ints[0];
			y2 = ints[1];
		}

		if (z2 < z1) {
			ints = switchNumbers(z1, z2);
			z1 = ints[0];
			z2 = ints[1];
		}

		for (int x = x1; x < x2 + 1; x++) {
			for (int y = y1; y < y2 + 1; y++) {
				for (int z = z1; z < z2 + 1; z++) {
					world.setBlockState(new BlockPos(x, y, z),
							material.getDefaultState());
				}
			}
		}
	}

	public void room(World world, BlockPos start, BlockPos end, Block material) {
		fill(world, start, end, material);

		int x1 = -(start.getX() - end.getX())
				/ Math.abs(start.getX() - end.getX());
		int x2 = -(end.getX() - start.getX())
				/ Math.abs(end.getX() - start.getX());
		int y1 = -(start.getY() - end.getY())
				/ Math.abs(start.getY() - end.getY());
		int y2 = -(end.getY() - start.getY())
				/ Math.abs(end.getY() - start.getY());
		int z1 = -(start.getZ() - end.getZ())
				/ Math.abs(start.getZ() - end.getZ());
		int z2 = -(end.getZ() - start.getZ())
				/ Math.abs(end.getZ() - start.getZ());

		fill(world, start.add(x1, y1, z1), end.add(x2, y2, z2), Blocks.air);
	}

	public void container(World world, BlockPos start, Block material) {
		start = start.add(-2, 0, 1);
		room(world, start, start.add(4, 4, 4), Blocks.iron_block);

		world.setBlockState(start.add(2, 3, 0),
				Moby.logo_block.getDefaultState());
		fill(world, start.add(2, 2, 0), start.add(2, 1, 0), Blocks.air);

		IBlockState glowstone = Blocks.glowstone.getDefaultState();
		world.setBlockState(start.add(3, 0, 1), glowstone);
		world.setBlockState(start.add(1, 0, 1), glowstone);
		world.setBlockState(start.add(3, 0, 3), glowstone);
		world.setBlockState(start.add(1, 0, 3), glowstone);
		world.setBlockState(start.add(3, 4, 1), glowstone);
		world.setBlockState(start.add(1, 4, 1), glowstone);
		world.setBlockState(start.add(3, 4, 3), glowstone);
		world.setBlockState(start.add(1, 4, 3), glowstone);

		world.setBlockState(start.add(2, 2, 3), Blocks.lever.getDefaultState());
		IBlockState wallSign = Blocks.wall_sign.getDefaultState();
		world.setBlockState(start.add(1, 2, 3), wallSign);
		world.setBlockState(start.add(2, 3, 3), wallSign);
		world.setBlockState(start.add(3, 2, 3), wallSign);
		TileEntitySign sign = ((TileEntitySign) world.getTileEntity(start.add(
				2, 3, 3)));
		sign.signText[1] = new ChatComponentText(EnumChatFormatting.GREEN + ""
				+ EnumChatFormatting.BOLD + "Start" + EnumChatFormatting.BLACK
				+ "" + EnumChatFormatting.BOLD + " / "
				+ EnumChatFormatting.RED + "" + EnumChatFormatting.BOLD
				+ "Stop");
		sign.signText[2] = new ChatComponentText(EnumChatFormatting.BLACK + ""
				+ EnumChatFormatting.BOLD + "Container");
		((TileEntitySign) world.getTileEntity(start.add(3, 2, 3))).signText[1] = new ChatComponentText(
				EnumChatFormatting.GREEN + "" + EnumChatFormatting.BOLD
						+ "Start = Up");
		((TileEntitySign) world.getTileEntity(start.add(1, 2, 3))).signText[1] = new ChatComponentText(
				EnumChatFormatting.RED + "" + EnumChatFormatting.BOLD
						+ "Stop = Down");

	}

	public int[] switchNumbers(int num1, int num2) {
		int temp = num2;
		num2 = num1;
		num1 = temp;
		int[] ints = new int[2];
		ints[0] = num1;
		ints[1] = num2;
		return ints;
	}

}
