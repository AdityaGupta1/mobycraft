package org.redfrog404.mobycraft.commands;

import static org.redfrog404.mobycraft.commands.ContainerListCommands.getBoxContainerWithID;
import static org.redfrog404.mobycraft.commands.ContainerListCommands.refresh;
import static org.redfrog404.mobycraft.commands.DockerCommands.boxContainers;
import static org.redfrog404.mobycraft.commands.DockerCommands.builder;
import static org.redfrog404.mobycraft.commands.DockerCommands.containerIDMap;
import static org.redfrog404.mobycraft.commands.DockerCommands.sender;

import java.util.List;

import org.redfrog404.mobycraft.utils.BoxContainer;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

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
				setContainerAppearance(boxContainer.getID(), false);
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
	
	public static void setContainerAppearance(String containerID, boolean state) {
		
		System.out.println(containerIDMap);
		System.out.println(getBoxContainerWithID(containerID));

		if (getBoxContainerWithID(containerID) == null) {
			return;
		}

		BoxContainer boxContainer = getBoxContainerWithID(containerID);

		Block containerBlock;
		Block prevContainerBlock;

		if (state) {
			containerBlock = Blocks.iron_block;
			prevContainerBlock = Blocks.redstone_block;

		} else {
			containerBlock = Blocks.redstone_block;
			prevContainerBlock = Blocks.iron_block;
		}

		builder.replace(boxContainer.getWorld(), boxContainer.getPosition()
				.add(2, 0, 1), boxContainer.getPosition().add(-2, 0, 7),
				prevContainerBlock, containerBlock);

		builder.replace(boxContainer.getWorld(), boxContainer.getPosition()
				.add(2, 4, 1), boxContainer.getPosition().add(-2, 4, 7),
				prevContainerBlock, containerBlock);
	}

}
