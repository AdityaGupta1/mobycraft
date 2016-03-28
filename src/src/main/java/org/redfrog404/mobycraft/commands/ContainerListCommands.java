package org.redfrog404.mobycraft.commands;

import static org.redfrog404.mobycraft.commands.MainCommand.boxContainers;
import static org.redfrog404.mobycraft.commands.MainCommand.builder;
import static org.redfrog404.mobycraft.commands.MainCommand.containerIDMap;
import static org.redfrog404.mobycraft.commands.MainCommand.getDockerClient;
import static org.redfrog404.mobycraft.commands.MainCommand.getStartPosition;
import static org.redfrog404.mobycraft.commands.MainCommand.sender;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.redfrog404.mobycraft.utils.BoxContainer;

import com.github.dockerjava.api.model.Container;

public class ContainerListCommands {
	
	static <T extends Comparable<? super T>> List<T> asSortedList(
			Collection<T> c) {
		List<T> list = new ArrayList<T>(c);
		java.util.Collections.sort(list);
		return list;
	}
	
	public static void refresh() {
		List<Container> containers = getAll();
		boxContainers = builder.containerPanel(containers, getStartPosition(),
				sender.getEntityWorld());
		List<String> stoppedContainerNames = new ArrayList<String>();
		for (Container container : getStopped()) {
			stoppedContainerNames.add(container.getNames()[0]);
		}
		for (BoxContainer boxContainer : boxContainers) {
			if (stoppedContainerNames.contains(boxContainer.getName())) {
				boxContainer.setState(!boxContainer.getState());
			}
		}
		refreshContainerIDMap();
	}

	public static void refreshContainerIDMap() {
		containerIDMap.clear();
		for (BoxContainer boxContainer : boxContainers) {
			containerIDMap.put(boxContainer.getID(), boxContainer);	
		}
	}

	public static List<Container> getContainers() {
		return getDockerClient().listContainersCmd().exec();
	}

	public static List<Container> getStopped() {
		List<Container> containers = new ArrayList<Container>();
		for (Container container : getDockerClient().listContainersCmd()
				.withShowAll(true).exec()) {
			if (container.getStatus().toLowerCase().contains("exited")) {
				containers.add(container);
			}
		}

		return containers;
	}
	
	public static Container getWithName(String name) {
		for (Container container : getContainers()) {
			if (container.getNames()[0].equals(name)) {
				return container;
			}
		}
		return null;
	}

	public static Container getFromAllWithName(String name) {
		List<Container> containers = getAll();
		for (Container container : containers) {
			if (container.getNames()[0].equals(name)) {
				return container;
			}
		}
		return null;
	}

	public static List<Container> getAll() {
		List<Container> containers = getContainers();
		containers.addAll(getStopped());
		return containers;
	}

	/*
	 * Gets the BoxContainer from the containerIDMap with id <id>
	 */
	public static BoxContainer getBoxContainerWithID(String id) {
		if (!containerIDMap.containsKey(id)) {
			return null;
		}
		return containerIDMap.get(id);
	}
	

	public static boolean isStopped(String containerName) {
		if (getFromAllWithName(containerName) == null) {
			return false;
		}

		Container container = getFromAllWithName(containerName);

		if (container.getStatus().toLowerCase().contains("exited")) {
			return true;
		}

		return false;
	}

}
