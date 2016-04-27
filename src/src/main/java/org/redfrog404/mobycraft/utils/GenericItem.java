package org.redfrog404.mobycraft.utils;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class GenericItem extends Item {

	public GenericItem(String name, CreativeTabs tab) {
		super();
		this.setUnlocalizedName(name);
		this.setCreativeTab(tab);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer playerIn,
			List<String> tooltip, boolean advanced) {
		if (!this.getUnlocalizedName().equals("container_wand")) {
			return;
		}

		tooltip.add(EnumChatFormatting.DARK_RED
				+ "Right click on a container's \"Name:\" sign to remove the container.");
	}
}