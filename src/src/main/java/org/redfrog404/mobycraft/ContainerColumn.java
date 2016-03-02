package org.redfrog404.mobycraft;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.BlockPos;

/*
 * Contains 10 containers arranged in a vertical column
 */
public class ContainerColumn {

	public List<BoxContainer> containers = new ArrayList<BoxContainer>();

	public BlockPos position;

	public ContainerColumn(ArrayList<BoxContainer> boxContainers, int index,
			BlockPos position) {

		for (int i = index * 10; i < (index + 1) * 10; i++) {
			containers.add(boxContainers.get(i));
		}

		this.position = position;

	}
	
	public void build () {
		
	}

}
