package org.redfrog404.mobycraft.commands.common;

import static org.redfrog404.mobycraft.commands.common.MainCommand.arg1;
import static org.redfrog404.mobycraft.commands.common.MainCommand.args;
import static org.redfrog404.mobycraft.commands.common.MainCommand.boxContainers;
import static org.redfrog404.mobycraft.commands.common.MainCommand.builder;
import static org.redfrog404.mobycraft.commands.common.MainCommand.sender;
import static org.redfrog404.mobycraft.utils.MessageSender.sendErrorMessage;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Property;

import org.redfrog404.mobycraft.api.MobycraftBuildContainerCommands;
import org.redfrog404.mobycraft.api.MobycraftConfigurationCommands;
import org.redfrog404.mobycraft.api.MobycraftContainerListCommands;
import org.redfrog404.mobycraft.main.Mobycraft;
import org.redfrog404.mobycraft.structure.BoxContainer;
import org.redfrog404.mobycraft.utils.Utils;

import com.github.dockerjava.api.model.Container;

import javax.inject.Inject;

public class BuildContainerCommands implements MobycraftBuildContainerCommands {

	private final MobycraftContainerListCommands listCommands;
	private final MobycraftConfigurationCommands configurationCommands;

	@Inject
	public BuildContainerCommands(MobycraftContainerListCommands listCommands,
								  MobycraftConfigurationCommands configurationCommands) {
		this.listCommands = listCommands;
		this.configurationCommands = configurationCommands;
	}

	BlockPos minPos = new BlockPos(0, 0, 0);
	BlockPos maxPos = new BlockPos(0, 0, 0);

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
		listCommands.refresh();
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

		Container container = listCommands.getFromAllWithName("/" + arg1);

		if (container == null) {
			sendErrorMessage("No container exists with the name \"" + arg1
					+ "\"");
		}

		BoxContainer boxContainer = listCommands.getBoxContainerWithID(container.getId());

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
		listCommands.refreshContainerIDMap();

		Property startPosProperty = configurationCommands
				.getConfigProperties().getStartPosProperty();

		if (startPosProperty.isDefault()) {
			startPosProperty.setValue(sender.getPosition().getX() + ", "
					+ sender.getPosition().getY() + ", "
					+ sender.getPosition().getZ());
			Mobycraft.config.save();
		}

		List<Container> containers = listCommands.getAll();
		List<BoxContainer> newContainers = containerPanel(containers, configurationCommands.getStartPos(),
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
		newContainersToBuild = containerPanel(listCommands
				.getAll(), configurationCommands.getStartPos(),
				sender.getEntityWorld());
		newContainersToBuild = newContainersToBuild.subList(start,
				newContainersToBuild.size());
		buildContainersFromList(newContainersToBuild);

		boxContainers = newContainers;

		for (BoxContainer container : boxContainers) {
			if (!container.getState()) {
				setContainerAppearance(container, false);
			}
		}

		BlockPos startPos = configurationCommands.getStartPos();

		int minX = startPos.getX() - 2;
		int minY = startPos.getY();
		int minZ = startPos.getZ() - 10;
		int maxX;
		int maxY;
		int maxZ = startPos.getZ() + 10;

		int containerHeight = 0;

		int size = boxContainers.size();
		if (size < 10) {
			containerHeight = size;
		} else {
			containerHeight = 10;
		}

		maxY = startPos.getY() - 1 + (6 * containerHeight);

		int containerLength = ((size - (size % 10)) / 10) + 1;
		maxX = (minX - 1) + (containerLength * 6);

		minPos = new BlockPos(minX, minY, minZ);
		maxPos = new BlockPos(maxX, maxY, maxZ);
		
		Mobycraft.getMainCommand().count = 0;
	}

	private List<BoxContainer> containerColumn(List<Container> containers,
			int index, BlockPos pos, World world) {

		List<BoxContainer> boxContainers = new ArrayList<BoxContainer>();

		int endIndex = 10;

		if (containers.size() - (index * 10) < 10) {
			endIndex = containers.size() - index * 10;
		}

		for (int i = index * 10; i < (index * 10) + endIndex; i++) {
			Container container = containers.get(i);
			boxContainers.add(new BoxContainer(pos, container.getId(),
					container.getNames()[0], container.getImage(), world));
			pos = pos.add(0, 6, 0);
		}

		return boxContainers;
	}

	public List<BoxContainer> containerPanel(List<Container> containers,
			BlockPos pos, World world) {
		List<BoxContainer> boxContainers = new ArrayList<BoxContainer>();

		int lastIndex = (containers.size() - (containers.size() % 10)) / 10;
		for (int i = 0; i <= lastIndex; i++) {
			boxContainers.addAll(containerColumn(containers, i, pos, world));
			pos = pos.add(6, 0, 0);
		}

		List<String> stoppedContainerIDs = new ArrayList<String>();

		for (Container container : listCommands.getStopped()) {
			stoppedContainerIDs.add(container.getId());
		}

		for (BoxContainer container : boxContainers) {
			if (stoppedContainerIDs.contains(container.getID())) {
				container.setState(false);
			}
		}

		return boxContainers;
	}

	public BlockPos getAverageContainerPosition() {
		double x = 0;
		double y = 0;
		int z = configurationCommands.getStartPos().getZ();

		for (BoxContainer container : boxContainers) {
			BlockPos containerPos = container.getPosition();
			x += containerPos.getX();
			y += containerPos.getY();
		}

		x /= boxContainers.size();
		y /= boxContainers.size();

		return new BlockPos(Math.floor(x), Math.floor(y), z);
	}

	public BlockPos getMinPos() {
		return minPos;
	}

	public BlockPos getMaxPos() {
		return maxPos;
	}
}
