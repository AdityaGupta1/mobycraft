package org.redfrog404.mobycraft.commands;

import static org.redfrog404.mobycraft.commands.MainCommand.arg1;
import static org.redfrog404.mobycraft.commands.MainCommand.args;
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

import org.apache.commons.lang.math.NumberUtils;
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
		List<Container> containers = getDockerClient().listContainersCmd()
				.withShowAll(true).exec();
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

		Map<Double, BoxContainer> usagesMap = new HashMap<Double, BoxContainer>();
		List<Double> sortedUsages = new ArrayList<Double>();
		Map<Double, Integer> andOthers = new HashMap<Double, Integer>();
		for (BoxContainer container : boxContainers) {
			double usage = 0D;
			if (arg1.equalsIgnoreCase("cpu")) {
				usage = container.getCpuUsage();
			} else {
				usage = container.getMemoryUsage();
			}

			if (usagesMap.containsKey(usage)) {
				if (andOthers.containsKey(usage)) {
					andOthers.put(usage, andOthers.get(usage) + 1);
				} else {
					andOthers.put(usage, 1);
				}
			} else {
				usagesMap.put(usage, container);
			}
		}

		sortedUsages.addAll(usagesMap.keySet());
		sortedUsages = Lists.reverse(asSortedList(sortedUsages));

		int maxNumber = 0;
		int maxContainers = 5;

		if (usagesMap.size() >= maxContainers) {
			maxNumber = maxContainers;
		} else {
			maxNumber = usagesMap.size();
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

		List<Double> finishedUsages = new ArrayList<Double>();

		for (double usage : sortedUsages.subList(0, maxNumber)) {
			if (finishedUsages.contains(usage) && andOthers.containsKey(usage)) {
				continue;
			}
			String message = EnumChatFormatting.GOLD + "" + number + ". "
					+ EnumChatFormatting.DARK_AQUA
					+ usagesMap.get(usage).getName();
			if (andOthers.containsKey(usage)) {
				if (andOthers.get(usage) == 1) {
					message = message + " and " + andOthers.get(usage)
							+ " other";
				} else {
					message = message + " and " + andOthers.get(usage)
							+ " others";
				}
			}
			message = message + EnumChatFormatting.GOLD + " - "
					+ EnumChatFormatting.DARK_AQUA
					+ usagesMap.get(usage).getImage() + EnumChatFormatting.GOLD
					+ " - " + EnumChatFormatting.AQUA + formatter.format(usage)
					+ "%";
			sendMessage(message);
			number++;
			finishedUsages.add(usage);
		}

		int notIncluded = getContainers().size();
		notIncluded -= maxNumber;

		for (Double usage : finishedUsages) {
			if (andOthers.containsKey(usage)) {
				notIncluded -= andOthers.get(usage);
			}
		}
		System.out.println(notIncluded);
		switch (notIncluded) {
		case 0:
			break;
		case 1:
			sendMessage(EnumChatFormatting.DARK_AQUA
					+ "1 container not included in this heat map");
		default:
			sendMessage(EnumChatFormatting.DARK_AQUA + "" + notIncluded
					+ " containers not included in this heat map");
		}
		refresh();
	}
}