package org.redfrog404.mobycraft.commands.dockerjava;

import static org.redfrog404.mobycraft.commands.dockerjava.MainCommand.arg1;
import static org.redfrog404.mobycraft.commands.dockerjava.MainCommand.boxContainers;
import static org.redfrog404.mobycraft.commands.dockerjava.MainCommand.builder;
import static org.redfrog404.mobycraft.commands.dockerjava.MainCommand.containerIDMap;
import static org.redfrog404.mobycraft.commands.dockerjava.MainCommand.getDockerClient;
import static org.redfrog404.mobycraft.commands.dockerjava.MainCommand.sender;
import static org.redfrog404.mobycraft.utils.MessageSender.sendErrorMessage;
import static org.redfrog404.mobycraft.utils.MessageSender.sendFeedbackMessage;
import static org.redfrog404.mobycraft.utils.MessageSender.sendMessage;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.util.EnumChatFormatting;

import org.redfrog404.mobycraft.api.MobycraftCommandsFactory;
import org.redfrog404.mobycraft.api.MobycraftContainerListCommands;
import org.redfrog404.mobycraft.structure.BoxContainer;
import org.redfrog404.mobycraft.utils.Utils;

import com.github.dockerjava.api.model.Container;
import com.google.common.collect.Lists;

public class ContainerListCommands implements MobycraftContainerListCommands {

	MobycraftCommandsFactory factory = MobycraftCommandsFactory.getInstance();
	
	public void refresh() {
		List<Container> containers = getAll();
		boxContainers = factory.getBuildCommands().containerPanel(containers, 
				MobycraftCommandsFactory.getInstance().getConfigurationCommands().getStartPos(),
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

	public void refreshRunning() {
		List<Container> containers = getStarted();
		boxContainers = factory.getBuildCommands().containerPanel(containers, 
				MobycraftCommandsFactory.getInstance().getConfigurationCommands().getStartPos(),
				sender.getEntityWorld());
		refreshContainerIDMap();
	}

	public void refreshContainerIDMap() {
		containerIDMap.clear();
		for (BoxContainer boxContainer : boxContainers) {
			containerIDMap.put(boxContainer.getID(), boxContainer);
		}
	}

	public List<Container> getStarted() {
		return getDockerClient().listContainersCmd().exec();
	}

	public List<Container> getStopped() {
		List<Container> containers = new ArrayList<Container>();
		for (Container container : getDockerClient().listContainersCmd()
				.withShowAll(true).exec()) {
			if (container.getStatus().toLowerCase().contains("exited")) {
				containers.add(container);
			}
		}

		return containers;
	}

	public Container getWithName(String name) {
		for (Container container : getStarted()) {
			if (container.getNames()[0].equals(name)) {
				return container;
			}
		}
		return null;
	}

	public Container getFromAllWithName(String name) {
		List<Container> containers = getAll();
		for (Container container : containers) {
			if (container.getNames()[0].equals(name)) {
				return container;
			}
		}
		return null;
	}

	public List<Container> getAll() {
		List<Container> containers = getDockerClient().listContainersCmd()
				.withShowAll(true).exec();
		return containers;
	}

	/*
	 * Gets the BoxContainer from the containerIDMap with id <id>
	 */
	public BoxContainer getBoxContainerWithID(String id) {
		refreshContainerIDMap();
		if (!containerIDMap.containsKey(id)) {
			return null;
		}
		return containerIDMap.get(id);
	}

	public boolean isStopped(String containerName) {
		if (getFromAllWithName(containerName) == null) {
			return false;
		}

		Container container = getFromAllWithName(containerName);

		if (container.getStatus().toLowerCase().contains("exited")) {
			return true;
		}

		return false;
	}

	public void heatMap() {
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
		sortedUsages = Lists.reverse(Utils.asSortedList(sortedUsages));

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

		int notIncluded = getStarted().size();
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
