package org.redfrog404.mobycraft.commands.dockerjava;

import static org.redfrog404.mobycraft.commands.dockerjava.MainCommand.arg1;
import static org.redfrog404.mobycraft.commands.dockerjava.MainCommand.args;
import static org.redfrog404.mobycraft.commands.dockerjava.MainCommand.commandNumbers;
import static org.redfrog404.mobycraft.commands.dockerjava.MainCommand.containerIDMap;
import static org.redfrog404.mobycraft.commands.dockerjava.MainCommand.getDockerClient;
import static org.redfrog404.mobycraft.commands.dockerjava.MainCommand.helpMessages;
import static org.redfrog404.mobycraft.commands.dockerjava.MainCommand.sendHelpMessage;
import static org.redfrog404.mobycraft.commands.dockerjava.MainCommand.sender;
import static org.redfrog404.mobycraft.utils.MessageSender.sendBarMessage;
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
import org.redfrog404.mobycraft.api.MobycraftBasicCommands;
import org.redfrog404.mobycraft.api.MobycraftCommandsFactory;
import org.redfrog404.mobycraft.api.MobycraftContainerListCommands;
import org.redfrog404.mobycraft.structure.BoxContainer;
import org.redfrog404.mobycraft.utils.MobycraftException;
import org.redfrog404.mobycraft.utils.Utils;

import com.github.dockerjava.api.model.Container;

public class BasicDockerCommands implements MobycraftBasicCommands {

	// Used for help messages specific to a certain command
	public static Map<String, String[]> specificHelpMessages = new HashMap<String, String[]>();
	
	public void help() {
		int size = helpMessages.size();
		int maxCommandsPerPage = 8;

		int page = 1;
		int maxPages = (int) Math.ceil(((double) (size - 1))
				/ maxCommandsPerPage);

		boolean specificHelp = false;

		if (!Utils.checkIfArgIsNull(args, 0)) {
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

		for (String key : Utils.asSortedList(helpMessages.keySet()).subList(
				(page - 1) * maxCommandsPerPage, toIndex)) {
			sendHelpMessage(key, helpMessages.get(key));
		}
	}

	public void ps() {

		boolean showAll = false;

		if (!Utils.checkIfArgIsNull(args, 0)
				&& args[0].split("-")[1].toLowerCase().contains("a")) {
			showAll = true;
		}

		List<Container> containers;

		if (showAll) {
			containers = MobycraftCommandsFactory.getInstance().getListCommands().getAll();
		} else {
			containers = MobycraftCommandsFactory.getInstance().getListCommands().getStarted();
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

			if (MobycraftCommandsFactory.getInstance().getListCommands().isStopped(container.getNames()[0])) {
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

	public void showDetailedInfo() throws InterruptedException {
		MobycraftContainerListCommands listCommands = MobycraftCommandsFactory.getInstance().getListCommands();
		
		MobycraftCommandsFactory.getInstance().getListCommands().refreshContainerIDMap();
		System.out.println(containerIDMap);

		if (MobycraftCommandsFactory.getInstance().getListCommands().getBoxContainerWithID(arg1).equals(null)) {
			return;
		}

		BlockPos senderPos = sender.getPosition();
		sender = sender.getEntityWorld().getClosestPlayer(senderPos.getX(),
				senderPos.getY(), senderPos.getZ(), -1);

		if (listCommands.isStopped(listCommands.getBoxContainerWithID(arg1).getName())) {
			BoxContainer boxContainer = listCommands.getBoxContainerWithID(arg1);
			Container container = listCommands.getFromAllWithName(boxContainer.getName());
			printContainerInfo(boxContainer, container);
			return;
		}

		execStatsCommand(arg1, true);
	}

	public void printContainerInfo(
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
			throw new MobycraftException(exception);
		}
	}
	
	

}
