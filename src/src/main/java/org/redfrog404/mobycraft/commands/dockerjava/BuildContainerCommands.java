package org.redfrog404.mobycraft.commands.dockerjava;

import static org.redfrog404.mobycraft.commands.dockerjava.ContainerListCommands.getBoxContainerWithID;
import static org.redfrog404.mobycraft.commands.dockerjava.ContainerListCommands.getFromAllWithName;
import static org.redfrog404.mobycraft.commands.dockerjava.ContainerListCommands.refresh;
import static org.redfrog404.mobycraft.commands.dockerjava.MainCommand.arg1;
import static org.redfrog404.mobycraft.commands.dockerjava.MainCommand.boxContainers;
import static org.redfrog404.mobycraft.commands.dockerjava.MainCommand.builder;
import static org.redfrog404.mobycraft.commands.dockerjava.MainCommand.checkIfArgIsNull;
import static org.redfrog404.mobycraft.commands.dockerjava.MainCommand.readConfigProperties;
import static org.redfrog404.mobycraft.commands.dockerjava.MainCommand.sender;
import static org.redfrog404.mobycraft.commands.dockerjava.MainCommand.startPosProperty;
import static org.redfrog404.mobycraft.commands.dockerjava.MainCommand.updateContainers;
import static org.redfrog404.mobycraft.utils.MessageSender.sendConfirmMessage;
import static org.redfrog404.mobycraft.utils.MessageSender.sendErrorMessage;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

import org.redfrog404.mobycraft.main.Mobycraft;
import org.redfrog404.mobycraft.utils.BoxContainer;

import com.github.dockerjava.api.model.Container;

public class BuildContainerCommands {

	public static void buildContainers() {
		buildContainersFromList(boxContainers);
	}

	public static void buildContainersFromList(List<BoxContainer> containers) {
		for (BoxContainer boxContainer : containers) {
			builder.container(sender.getEntityWorld(),
					boxContainer.getPosition(), Blocks.iron_block,
					boxContainer.getName(), boxContainer.getImage(),
					boxContainer.getID());
			if (!boxContainer.getState()) {
				setContainerAppearance(boxContainer, false);
			}
		}
	}

	public static void refreshAndBuildContainers() {
		refresh();
		if (boxContainers.size() < 1) {
			return;
		}
		buildContainers();
	}

	public static void setContainerAppearance(BoxContainer container,
			boolean state) {
		Block containerBlock;
		Block prevContainerBlock;

		if (state) {
			containerBlock = Blocks.iron_block;
			prevContainerBlock = Blocks.redstone_block;

		} else {
			containerBlock = Blocks.redstone_block;
			prevContainerBlock = Blocks.iron_block;
		}

		builder.replace(container.getWorld(),
				container.getPosition().add(2, 0, 1), container.getPosition()
						.add(-2, 0, 7), prevContainerBlock, containerBlock);

		builder.replace(container.getWorld(),
				container.getPosition().add(2, 4, 1), container.getPosition()
						.add(-2, 4, 7), prevContainerBlock, containerBlock);
	}

	public static void teleport() throws PlayerNotFoundException {
		if (!(sender instanceof EntityPlayer)) {
			return;
		}

		if (boxContainers.size() < 1) {
			refreshAndBuildContainers();
			if (boxContainers.size() < 1) {
				sendErrorMessage("No containers currently existing!");
			}
		}

		if (checkIfArgIsNull(0)) {
			sendErrorMessage("Container name not specified! Command is used as /docker rm <name> .");
			return;
		}

		Container container = getFromAllWithName("/" + arg1);

		if (container == null) {
			sendErrorMessage("No container exists with the name \"" + arg1
					+ "\"");
		}

		BoxContainer boxContainer = getBoxContainerWithID(container.getId());

		BlockPos pos = boxContainer.getPosition();

		getCommandSenderAsPlayer(sender).playerNetServerHandler
				.setPlayerLocation(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() - 0.5, 0, 0);
	}

	public static EntityPlayerMP getCommandSenderAsPlayer(ICommandSender sender)
			throws PlayerNotFoundException {
		if (sender instanceof EntityPlayerMP) {
			return (EntityPlayerMP) sender;
		} else {
			throw new PlayerNotFoundException(
					"You must specify which player you wish to perform this action on.",
					new Object[0]);
		}
	}
	
	public static void setStartPos(){
		BlockPos position = sender.getPosition();
		startPosProperty.setValue((int) Math.floor(position.getX()) + ", "
				+ (int) Math.floor(position.getY()) + ", " + (int) Math.floor(position.getZ()));
		Mobycraft.config.save();
		sendConfirmMessage("Set start position for building containers to ("
				+ startPosProperty.getString() + ").");
		readConfigProperties();
		updateContainers(false);
	}

}
