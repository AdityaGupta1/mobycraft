package org.redfrog404.mobycraft.commands;

import static org.redfrog404.mobycraft.commands.ContainerListCommands.asSortedList;
import static org.redfrog404.mobycraft.commands.ContainerListCommands.getBoxContainerWithID;
import static org.redfrog404.mobycraft.commands.ContainerListCommands.getFromAllWithName;
import static org.redfrog404.mobycraft.commands.ContainerListCommands.refreshContainerIDMap;
import static org.redfrog404.mobycraft.commands.MainCommand.arg1;
import static org.redfrog404.mobycraft.commands.MainCommand.args;
import static org.redfrog404.mobycraft.commands.MainCommand.checkIfArgIsNull;
import static org.redfrog404.mobycraft.commands.MainCommand.containerIDMap;
import static org.redfrog404.mobycraft.commands.MainCommand.dockerPath;
import static org.redfrog404.mobycraft.commands.MainCommand.getDockerClient;
import static org.redfrog404.mobycraft.commands.MainCommand.helpMessages;
import static org.redfrog404.mobycraft.commands.MainCommand.sendHelpMessage;
import static org.redfrog404.mobycraft.commands.MainCommand.sender;
import static org.redfrog404.mobycraft.utils.SendMessagesToCommandSender.sendBarMessage;
import static org.redfrog404.mobycraft.utils.SendMessagesToCommandSender.sendConfirmMessage;
import static org.redfrog404.mobycraft.utils.SendMessagesToCommandSender.sendErrorMessage;
import static org.redfrog404.mobycraft.utils.SendMessagesToCommandSender.sendFeedbackMessage;
import static org.redfrog404.mobycraft.utils.SendMessagesToCommandSender.sendMessage;

import java.util.List;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;

import org.redfrog404.mobycraft.main.Mobycraft;
import org.redfrog404.mobycraft.utils.BoxContainer;
import org.redfrog404.mobycraft.utils.StatisticsResultCallback;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Event;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.core.async.ResultCallbackTemplate;
import com.github.dockerjava.core.command.EventsResultCallback;

public class BasicDockerCommands {

	public static void help() {
		int size = helpMessages.size();
		int maxCommandsPerPage = 8;

		int page = 1;
		int maxPages = ((size - size % maxCommandsPerPage) / maxCommandsPerPage) + 1;

		if (!checkIfArgIsNull(2)) {
			page = Integer.parseInt(args[1]);
		}

		if (page > maxPages) {
			page = maxPages;
		}

		sendMessage(EnumChatFormatting.BLUE + "" + EnumChatFormatting.BOLD
				+ "============== Docker Help ==============");
		sendMessage(EnumChatFormatting.DARK_AQUA + "" + EnumChatFormatting.BOLD
				+ "== Page " + page + "/" + maxPages);
		sendMessage(EnumChatFormatting.AQUA
				+ "-- <arg> is required, (arg) is optional");
		sendMessage(EnumChatFormatting.AQUA
				+ "-- \"|\" means \"or\"; e.g. \"<name | amount>\" means you can either put the name or the amount");

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
		sendFeedbackMessage("Loading...");

		DockerClient dockerClient = getDockerClient();

		List<Container> containers = dockerClient.listContainersCmd().exec();

		if (containers.size() == 0) {
			sendFeedbackMessage("No containers currently running.");
			return;
		}

		sendBarMessage(EnumChatFormatting.BLUE);
		sendMessage(EnumChatFormatting.AQUA + "Name(s)"
				+ EnumChatFormatting.RESET + ", " + EnumChatFormatting.GOLD
				+ "Image" + EnumChatFormatting.RESET + ", "
				+ EnumChatFormatting.GREEN + "Container ID");
		sendBarMessage(EnumChatFormatting.BLUE);

		for (Container container : containers) {
			String message = "";
			for (String name : container.getNames()) {
				if (container.getNames()[0].equals(name)) {
					message += EnumChatFormatting.AQUA + name;
				} else {
					message += ", " + name;
				}
			}
			message += EnumChatFormatting.RESET + ", "
					+ EnumChatFormatting.GOLD + container.getImage()
					+ EnumChatFormatting.RESET + ", "
					+ EnumChatFormatting.GREEN
					+ container.getId().substring(0, 12);
			sendMessage(message);
		}
	}

	public static void path() {
		if (args.length < 2) {
			sendErrorMessage("Docker path is not specified! Command is used as /docker path <path> .");
			return;
		}

		dockerPath.setValue(arg1);
		Mobycraft.config.save();
		sendConfirmMessage("Docker path set to \"" + arg1 + "\"");
	}

	public static void showDetailedInfo() {
		refreshContainerIDMap();
		System.out.println(containerIDMap);

		if (getBoxContainerWithID(arg1).equals(null)) {
			return;
		}

		BlockPos senderPos = sender.getPosition();
		sender = sender.getEntityWorld().getClosestPlayer(
				senderPos.getX(), senderPos.getY(),
				senderPos.getZ(), -1);

		getDockerClient().statsCmd().exec(new StatisticsResultCallback());
	}

}
