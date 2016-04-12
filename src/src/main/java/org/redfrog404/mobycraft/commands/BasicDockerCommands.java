package org.redfrog404.mobycraft.commands;

import static org.redfrog404.mobycraft.commands.ContainerListCommands.asSortedList;
import static org.redfrog404.mobycraft.commands.ContainerListCommands.getAll;
import static org.redfrog404.mobycraft.commands.ContainerListCommands.getBoxContainerWithID;
import static org.redfrog404.mobycraft.commands.ContainerListCommands.getContainers;
import static org.redfrog404.mobycraft.commands.ContainerListCommands.getFromAllWithName;
import static org.redfrog404.mobycraft.commands.ContainerListCommands.isStopped;
import static org.redfrog404.mobycraft.commands.ContainerListCommands.refreshContainerIDMap;
import static org.redfrog404.mobycraft.commands.MainCommand.arg1;
import static org.redfrog404.mobycraft.commands.MainCommand.args;
import static org.redfrog404.mobycraft.commands.MainCommand.checkIfArgIsNull;
import static org.redfrog404.mobycraft.commands.MainCommand.commandNumbers;
import static org.redfrog404.mobycraft.commands.MainCommand.containerIDMap;
import static org.redfrog404.mobycraft.commands.MainCommand.certPathProperty;
import static org.redfrog404.mobycraft.commands.MainCommand.dockerHostProperty;
import static org.redfrog404.mobycraft.commands.MainCommand.getDockerClient;
import static org.redfrog404.mobycraft.commands.MainCommand.helpMessages;
import static org.redfrog404.mobycraft.commands.MainCommand.pollRateProperty;
import static org.redfrog404.mobycraft.commands.MainCommand.sendHelpMessage;
import static org.redfrog404.mobycraft.commands.MainCommand.sender;
import static org.redfrog404.mobycraft.utils.MessageSender.sendBarMessage;
import static org.redfrog404.mobycraft.utils.MessageSender.sendConfirmMessage;
import static org.redfrog404.mobycraft.utils.MessageSender.sendErrorMessage;
import static org.redfrog404.mobycraft.utils.MessageSender.sendFeedbackMessage;
import static org.redfrog404.mobycraft.utils.MessageSender.sendMessage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;

import org.apache.commons.lang.math.NumberUtils;
import org.redfrog404.mobycraft.main.Mobycraft;
import org.redfrog404.mobycraft.utils.BoxContainer;
import org.redfrog404.mobycraft.utils.StatisticsResultCallback;

import com.github.dockerjava.api.model.Container;

public class BasicDockerCommands {

	// Used for help messages specific to a certain command
	public static Map<String, String[]> specificHelpMessages = new HashMap<String, String[]>();

	public static void help() {
		int size = helpMessages.size();
		int maxCommandsPerPage = 8;

		int page = 1;
		int maxPages = (int) Math.ceil(((double) (size - 1))
				/ maxCommandsPerPage);

		boolean specificHelp = false;

		if (!checkIfArgIsNull(0)) {
			if (NumberUtils.isNumber(args[0])) {
				page = Integer.parseInt(args[0]);
			} else {
				specificHelp = true;
			}
		}

		if (specificHelp) {
			String command = args[0];

			if (!commandNumbers.keySet().contains(command)) {
				sendErrorMessage("\""
						+ command
						+ "\" is not a valid command! Use /docker help for help.");
				return;
			}

			if (!specificHelpMessages.containsKey(command)) {
				sendErrorMessage("There is no specific help for the command \""
						+ command + "\" yet.");
				return;
			}

			String[] messages = specificHelpMessages.get(command);
			sendMessage(EnumChatFormatting.BLUE + "" + EnumChatFormatting.BOLD
					+ "================ Docker Help ================");
			sendMessage("");
			sendMessage(EnumChatFormatting.DARK_AQUA + ""
					+ EnumChatFormatting.BOLD + "== " + EnumChatFormatting.GOLD
					+ "/docker " + messages[0]);
			sendMessage("");
			sendMessage(EnumChatFormatting.AQUA + "= "
					+ helpMessages.get(messages[0]));
			for (String message : Arrays.copyOfRange(messages, 1,
					messages.length)) {
				sendMessage(EnumChatFormatting.AQUA + "= " + message);
			}
			return;
		}

		if (page > maxPages) {
			page = maxPages;
		}

		sendMessage(EnumChatFormatting.BLUE + "" + EnumChatFormatting.BOLD
				+ "================ Docker Help ================");
		sendMessage(EnumChatFormatting.DARK_AQUA + "" + EnumChatFormatting.BOLD
				+ "== Page " + page + "/" + maxPages);
		sendMessage(EnumChatFormatting.AQUA
				+ "= <arg> is required, (arg) and [options] are optional");
		sendMessage(EnumChatFormatting.AQUA
				+ "= \"|\" means \"or\"; e.g. \"<name | amount>\" means you can either put the name or the amount");

		int toIndex = page * maxCommandsPerPage;
		if (toIndex > size) {
			toIndex = size;
		}

		for (String key : asSortedList(helpMessages.keySet()).subList(
				(page - 1) * maxCommandsPerPage, toIndex)) {
			sendHelpMessage(key, helpMessages.get(key));
		}
	}

	public static void ps() {

		boolean showAll = false;

		if (!checkIfArgIsNull(0)
				&& args[0].split("-")[1].toLowerCase().contains("a")) {
			showAll = true;
		}

		List<Container> containers;

		if (showAll) {
			containers = getAll();
		} else {
			containers = getContainers();
		}

		if (containers.size() == 0) {
			if (showAll) {
				sendFeedbackMessage("No containers currently existing.");
			} else {
				sendFeedbackMessage("No containers currently running.");
			}
			return;
		}

		String firstMessage = EnumChatFormatting.AQUA + "Name(s)"
				+ EnumChatFormatting.RESET + ", " + EnumChatFormatting.GOLD
				+ "Image" + EnumChatFormatting.RESET + ", "
				+ EnumChatFormatting.GREEN + "Container ID";

		if (showAll) {
			firstMessage = firstMessage + EnumChatFormatting.RESET + ", "
					+ EnumChatFormatting.LIGHT_PURPLE + "Status ";
		}

		sendBarMessage(EnumChatFormatting.BLUE);
		sendMessage(firstMessage);
		sendBarMessage(EnumChatFormatting.BLUE);

		for (Container container : containers) {
			String message = "";
			for (String name : container.getNames()) {
				if (container.getNames()[0].equals(name)) {
					message += EnumChatFormatting.AQUA
							+ name.substring(1, name.length());
				} else {
					message += ", " + name;
				}
			}

			String state = "running";

			if (isStopped(container.getNames()[0])) {
				state = "stopped";
			}

			message += EnumChatFormatting.RESET + ", "
					+ EnumChatFormatting.GOLD + container.getImage()
					+ EnumChatFormatting.RESET + ", "
					+ EnumChatFormatting.GREEN
					+ container.getId().substring(0, 12);
			if (showAll) {
				message = message + EnumChatFormatting.RESET + ", "
						+ EnumChatFormatting.LIGHT_PURPLE + state;
			}
			sendMessage(message);
		}
	}

	public static void path() {
		if (checkIfArgIsNull(0)) {
			sendErrorMessage("Docker path is not specified! Command is used as /docker path <path> .");
			return;
		}

		certPathProperty.setValue(arg1);
		Mobycraft.config.save();
		sendConfirmMessage("Docker path set to \"" + arg1 + "\"");
	}

	public static void host() {
		if (checkIfArgIsNull(0)) {
			sendErrorMessage("Docker host is not specified! Command is used as /docker host <host> .");
			return;
		}

		if (!arg1.contains(":")) {
			arg1 = arg1 + ":2376";
		}

		dockerHostProperty.setValue(arg1);
		Mobycraft.config.save();
		sendConfirmMessage("Docker host set to \"" + arg1 + "\"");
	}

	public static void pollRate() {
		if (checkIfArgIsNull(0)) {
			sendErrorMessage("Poll rate is not specified! Command is used as /docker poll_rate <rate> .");
			return;
		}

		pollRateProperty.setValue(arg1);
		Mobycraft.config.save();
		sendConfirmMessage("Poll rate set to " + arg1 + " seconds");
	}

	public static void showDetailedInfo() throws InterruptedException {
		refreshContainerIDMap();
		System.out.println(containerIDMap);

		if (getBoxContainerWithID(arg1).equals(null)) {
			return;
		}

		BlockPos senderPos = sender.getPosition();
		sender = sender.getEntityWorld().getClosestPlayer(senderPos.getX(),
				senderPos.getY(), senderPos.getZ(), -1);

		if (isStopped(getBoxContainerWithID(arg1).getName())) {
			BoxContainer boxContainer = getBoxContainerWithID(arg1);
			Container container = getFromAllWithName(boxContainer.getName());
			printBasicContainerInformation(boxContainer, container);
			return;
		}

		execStatsCommand(arg1, true);
	}

	public static void printBasicContainerInformation(
			BoxContainer boxContainer, Container container) {
		sendMessage(EnumChatFormatting.BLUE + "" + EnumChatFormatting.BOLD
				+ "=========== Container Information ===========");
		sendMessage(EnumChatFormatting.AQUA + "" + EnumChatFormatting.BOLD
				+ "Name: " + EnumChatFormatting.RESET + boxContainer.getName());
		sendMessage(EnumChatFormatting.GOLD + "" + EnumChatFormatting.BOLD
				+ "Image: " + EnumChatFormatting.RESET
				+ boxContainer.getImage());
		sendMessage(EnumChatFormatting.GREEN + "" + EnumChatFormatting.BOLD
				+ "ID: " + EnumChatFormatting.RESET + boxContainer.getShortID());
		sendMessage(EnumChatFormatting.LIGHT_PURPLE + ""
				+ EnumChatFormatting.BOLD + "Status: "
				+ EnumChatFormatting.RESET + container.getStatus());
	}

	public static void execStatsCommand(String containerID, boolean sendMessages) {
		StatisticsResultCallback callback = new StatisticsResultCallback(
				containerID, sendMessages);
		callback = getDockerClient().statsCmd().withContainerId(containerID)
				.exec(callback);
		try {
			callback.awaitCompletion();
		} catch (InterruptedException exception) {
			return;
		}
	}

}
