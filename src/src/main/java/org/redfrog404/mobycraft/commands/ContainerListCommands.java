package org.redfrog404.mobycraft.commands;

import static org.redfrog404.mobycraft.commands.MainCommand.arg1;
import static org.redfrog404.mobycraft.commands.MainCommand.boxContainers;
import static org.redfrog404.mobycraft.commands.MainCommand.builder;
import static org.redfrog404.mobycraft.commands.MainCommand.checkIfArgIsNull;
import static org.redfrog404.mobycraft.commands.MainCommand.containerIDMap;
import static org.redfrog404.mobycraft.commands.MainCommand.getDockerClient;
import static org.redfrog404.mobycraft.commands.MainCommand.getStartPosition;
import static org.redfrog404.mobycraft.commands.MainCommand.sender;
import static org.redfrog404.mobycraft.utils.MessageSender.sendErrorMessage;
import static org.redfrog404.mobycraft.utils.MessageSender.sendFeedbackMessage;
import static org.redfrog404.mobycraft.utils.MessageSender.sendMessage;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.util.EnumChatFormatting;

import org.redfrog404.mobycraft.utils.BoxContainer;

import com.github.dockerjava.api.model.Container;
import com.google.common.collect.Lists;

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

	public static void refreshRunning() {
		List<Container> containers = getContainers();
		boxContainers = builder.containerPanel(containers, getStartPosition(),
				sender.getEntityWorld());
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
		refreshContainerIDMap();
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

	public static void heatMap() {
		if (arg1 == null) {
			sendErrorMessage("Type of heat map not specified! \"cpu\" and \"memory\" are accepted as types.");
			return;
		}

		if (!arg1.equalsIgnoreCase("cpu") && !arg1.equalsIgnoreCase("memory")) {
			sendErrorMessage("\""
					+ arg1
					+ "\" is not a valid argument! Only \"cpu\" and \"memory\" are accepted.");
			return;
		}

		sendFeedbackMessage("Loading...");

		refreshRunning();
		Map<Double, BoxContainer> finalUsagesMap = new HashMap<Double, BoxContainer>();

		Map<Double, BoxContainer> usagesMap = new HashMap<Double, BoxContainer>();
		for (BoxContainer container : boxContainers) {
			if (arg1.equalsIgnoreCase("cpu")) {
				usagesMap.put(container.getCpuUsage(), container);
			} else {
				usagesMap.put(container.getMemoryUsage(), container);
			}
		}

		List<Double> sortedUsages = new ArrayList<Double>();

		for (BoxContainer container : boxContainers) {
			if (arg1.equalsIgnoreCase("cpu")) {
				sortedUsages.add(container.getCpuUsage());
			} else {
				sortedUsages.add(container.getMemoryUsage());
			}
		}

		sortedUsages = Lists.reverse(asSortedList(sortedUsages));

		int maxNumber = 0;

		if (boxContainers.size() >= 5) {
			maxNumber = 5;
		} else {
			maxNumber = boxContainers.size();
		}
		for (int i = 0; i < maxNumber; i++) {
			finalUsagesMap.put(sortedUsages.get(i),
					usagesMap.get(sortedUsages.get(i)));
		}

		if (arg1.equalsIgnoreCase("cpu")) {
			sendMessage(EnumChatFormatting.BLUE + "" + EnumChatFormatting.BOLD
					+ "== CPU Usage Heat Map");
		} else {
			sendMessage(EnumChatFormatting.BLUE + "" + EnumChatFormatting.BOLD
					+ "== Memory Usage Heat Map");
		}

		int number = 1;
		NumberFormat formatter = new DecimalFormat("#0.00");

		for (double usage : sortedUsages.subList(0, maxNumber)) {
			sendMessage(EnumChatFormatting.GOLD + "" + number + ". "
					+ EnumChatFormatting.DARK_AQUA
					+ finalUsagesMap.get(usage).getName()
					+ EnumChatFormatting.GOLD + " - "
					+ EnumChatFormatting.DARK_AQUA
					+ finalUsagesMap.get(usage).getImage()
					+ EnumChatFormatting.GOLD + " - " + EnumChatFormatting.AQUA
					+ formatter.format(usage) + "%");
			number++;
		}
		refresh();
	}
}