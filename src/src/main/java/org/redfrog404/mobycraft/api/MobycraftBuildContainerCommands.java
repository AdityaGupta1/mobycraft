package org.redfrog404.mobycraft.api;

import java.util.ArrayList;
import java.util.List;

import org.redfrog404.mobycraft.commands.dockerjava.ConfigProperties;
import org.redfrog404.mobycraft.utils.BoxContainer;

import com.github.dockerjava.api.model.Container;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.BlockPos;

public interface MobycraftBuildContainerCommands {
	
	public void buildContainers();

	public void buildContainersFromList(List<BoxContainer> containers);

	public void refreshAndBuildContainers();

	public void setContainerAppearance(BoxContainer container,
			boolean state);

	public void teleport() throws PlayerNotFoundException;

	public EntityPlayerMP getCommandSenderAsPlayer(ICommandSender sender)
			throws PlayerNotFoundException;
	
	public void updateContainers(boolean checkForEqual);
}
