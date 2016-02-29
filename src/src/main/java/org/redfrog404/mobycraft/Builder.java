package org.redfrog404.mobycraft;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
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
					world.setBlockState(new BlockPos(x, y, z), material.getDefaultState());
				}
			}
		}
	}

	public void room(World world, BlockPos start, BlockPos end, Block material) {
		fill(world, start, end, material);
		
		int x1 = -(start.getX() - end.getX()) / Math.abs(start.getX() - end.getX());
		int x2 = -(end.getX() - start.getX()) / Math.abs(end.getX() - start.getX());
		int y1 = -(start.getY() - end.getY()) / Math.abs(start.getY() - end.getY());
		int y2 = -(end.getY() - start.getY()) / Math.abs(end.getY() - start.getY());
		int z1 = -(start.getZ() - end.getZ()) / Math.abs(start.getZ() - end.getZ());
		int z2 = -(end.getZ() - start.getZ()) / Math.abs(end.getZ() - start.getZ());
		
		fill(world, start.add(x1, y1, z1), end.add(x2, y2, z2), Blocks.air);
	}
	
	public void container(World world, BlockPos start, Block material){
		room(world, start, start.add(4, 4, 4), Blocks.iron_block);
	}
	
	public int[] switchNumbers(int num1, int num2){
		int temp = num2;
		num2 = num1;
		num1 = temp;
		int[] ints = new int[2];
		ints[0] = num1;
		ints[1] = num2;
		return ints;
	}

}
