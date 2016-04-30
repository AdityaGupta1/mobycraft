package org.redfrog404.mobycraft.structure;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;

public class GenericBlock extends Block {

	public GenericBlock(String name, Material material, float hardness, float resistance, String tool, int harvestLevel,
			SoundType sound) {
		super(material);
		this.setUnlocalizedName(name);
		this.setCreativeTab(CreativeTabs.tabBlock);
		this.setHardness(hardness);
		this.setResistance(resistance);
		this.setHarvestLevel(tool, harvestLevel);
		this.setStepSound(sound);
	}
}