package org.redfrog404.mobycraft.commands;

import static org.redfrog404.mobycraft.commands.ContainerListCommands.getBoxContainerWithID;
import static org.redfrog404.mobycraft.commands.ContainerListCommands.refresh;
import static org.redfrog404.mobycraft.commands.MainCommand.boxContainers;
import static org.redfrog404.mobycraft.commands.MainCommand.builder;
import static org.redfrog404.mobycraft.commands.MainCommand.containerIDMap;
import static org.redfrog404.mobycraft.commands.MainCommand.sender;

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
	
	public static void setContainerAppearance(BoxContainer container, boolean state) {		
		Block containerBlock;
		Block prevContainerBlock;

		if (state) {
			containerBlock = Blocks.iron_block;
			prevContainerBlock = Blocks.redstone_block;

		} else {
			containerBlock = Blocks.redstone_block;
			prevContainerBlock = Blocks.iron_block;
		}

		builder.replace(container.getWorld(), container.getPosition()
				.add(2, 0, 1), container.getPosition().add(-2, 0, 7),
				prevContainerBlock, containerBlock);

		builder.replace(container.getWorld(), container.getPosition()
				.add(2, 4, 1), container.getPosition().add(-2, 4, 7),
				prevContainerBlock, containerBlock);
	}

}
