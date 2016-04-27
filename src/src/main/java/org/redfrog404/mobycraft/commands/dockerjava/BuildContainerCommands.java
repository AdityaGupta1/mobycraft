package org.redfrog404.mobycraft.commands.dockerjava;

import static org.redfrog404.mobycraft.commands.dockerjava.MainCommand.arg1;
import static org.redfrog404.mobycraft.commands.dockerjava.MainCommand.args;
import static org.redfrog404.mobycraft.commands.dockerjava.MainCommand.boxContainers;
import static org.redfrog404.mobycraft.commands.dockerjava.MainCommand.builder;
import static org.redfrog404.mobycraft.commands.dockerjava.MainCommand.sender;
import static org.redfrog404.mobycraft.utils.MessageSender.sendErrorMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraftforge.common.config.Property;

import org.redfrog404.mobycraft.api.MobycraftBuildContainerCommands;
import org.redfrog404.mobycraft.api.MobycraftCommandsFactory;
import org.redfrog404.mobycraft.main.Mobycraft;
import org.redfrog404.mobycraft.utils.BoxContainer;
import org.redfrog404.mobycraft.utils.Utils;

import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Event;
import com.github.dockerjava.core.command.EventsResultCallback;

public class BuildContainerCommands implements MobycraftBuildContainerCommands {

	MobycraftCommandsFactory factory = MobycraftCommandsFactory.getInstance();

	@Override
	public void buildContainers() {
		buildContainersFromList(boxContainers);
	}

	@Override
	public void buildContainersFromList(List<BoxContainer> containers) {
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

	@Override
	public void refreshAndBuildContainers() {
		MobycraftCommandsFactory.getInstance().getListCommands().refresh();
		if (boxContainers.size() < 1) {
			return;
		}
		buildContainers();
	}

	@Override
	public void setContainerAppearance(BoxContainer container, boolean state) {
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

	@Override
	public void teleport() throws PlayerNotFoundException {
		if (!(sender instanceof EntityPlayer)) {
			return;
		}

		if (boxContainers.size() < 1) {
			refreshAndBuildContainers();
			if (boxContainers.size() < 1) {
				sendErrorMessage("No containers currently existing!");
			}
		}

		if (Utils.checkIfArgIsNull(args, 0)) {
			sendErrorMessage("Container name not specified! Command is used as /docker rm <name> .");
			return;
		}

		Container container = MobycraftCommandsFactory.getInstance()
				.getListCommands().getFromAllWithName("/" + arg1);

		if (container == null) {
			sendErrorMessage("No container exists with the name \"" + arg1
					+ "\"");
		}

		BoxContainer boxContainer = MobycraftCommandsFactory.getInstance()
				.getListCommands().getBoxContainerWithID(container.getId());

		BlockPos pos = boxContainer.getPosition();

		getCommandSenderAsPlayer(sender).playerNetServerHandler
				.setPlayerLocation(pos.getX() + 0.5, pos.getY() + 0.5,
						pos.getZ() - 0.5, 0, 0);
	}

	@Override
	public EntityPlayerMP getCommandSenderAsPlayer(ICommandSender sender)
			throws PlayerNotFoundException {
		if (sender instanceof EntityPlayerMP) {
			return (EntityPlayerMP) sender;
		} else {
			throw new PlayerNotFoundException(
					"You must specify which player you wish to perform this action on.",
					new Object[0]);
		}
	}

	@Override
	public void updateContainers(boolean checkForEqual) {
		factory.getListCommands().refreshContainerIDMap();

		Property startPosProperty = factory.getConfigurationCommands()
				.getConfigProperties().getStartPosProperty();

		if (startPosProperty.isDefault()) {
			startPosProperty.setValue(sender.getPosition().getX() + ", "
					+ sender.getPosition().getY() + ", "
					+ sender.getPosition().getZ());
			Mobycraft.config.save();
		}

		List<Container> containers = factory.getListCommands().getAll();
		List<BoxContainer> newContainers = builder.containerPanel(containers,
				factory.getConfigurationCommands().getStartPos(),
				sender.getEntityWorld());

		if (boxContainers.equals(newContainers) && checkForEqual) {
			return;
		}

		int start = 0;

		if (checkForEqual) {
			findDifferences: for (; start < boxContainers.size(); start++) {
				if (start == newContainers.size()) {
					start--;
					break findDifferences;
				}
				if (!boxContainers.get(start).equals(newContainers.get(start))) {
					break findDifferences;
				}
			}

			start -= start % 10;
			start--;

			if (start < 0) {
				start = 0;
			}
		}

		List<BoxContainer> containersToReplace = new ArrayList<BoxContainer>();
		containersToReplace = boxContainers
				.subList(start, boxContainers.size());

		for (BoxContainer container : containersToReplace) {
			builder.airContainer(container.getWorld(), container.getPosition());
		}

		List<BoxContainer> newContainersToBuild = new ArrayList<BoxContainer>();
		newContainersToBuild = builder.containerPanel(factory.getListCommands()
				.getAll(), factory.getConfigurationCommands().getStartPos(),
				sender.getEntityWorld());
		newContainersToBuild = newContainersToBuild.subList(start,
				newContainersToBuild.size());
		factory.getBuildCommands()
				.buildContainersFromList(newContainersToBuild);

		boxContainers = newContainers;

		List<String> stoppedContainerIDs = new ArrayList<String>();

		for (Container container : factory.getListCommands().getStopped()) {
			stoppedContainerIDs.add(container.getId());
		}

		for (BoxContainer container : boxContainers) {
			if (stoppedContainerIDs.contains(container.getID())) {
				factory.getBuildCommands().setContainerAppearance(container,
						false);
				container.setState(false);
			} else {
				container.setState(true);
			}
		}
	}
}
