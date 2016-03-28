package org.redfrog404.mobycraft.utils;

import static org.redfrog404.mobycraft.commands.ContainerListCommands.getWithName;
import static org.redfrog404.mobycraft.commands.MainCommand.dockerClient;
import static org.redfrog404.mobycraft.commands.MainCommand.getDockerClient;
import static org.redfrog404.mobycraft.utils.SendMessagesToCommandSender.sendMessage;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.github.dockerjava.api.model.Container;

public class GenericItem extends Item {

	public GenericItem(String name) {
		super();
		this.setUnlocalizedName(name);
		this.setCreativeTab(CreativeTabs.tabMisc);
	}

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