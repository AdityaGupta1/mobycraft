package org.redfrog404.mobycraft;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;

public class DockerCommands implements ICommand {
	private List aliases;

	Map<String, Integer> argNumbers = new HashMap<String, Integer>();

	ICommandSender sender;

	String dockerPath;

	String[] args;

	public DockerCommands() {
		this.aliases = new ArrayList();
		this.aliases.add("docker");
		argNumbers.put("help", 0);
		argNumbers.put("ps", 1);
		argNumbers.put("path", 2);
	}

	@Override
	public String getCommandName() {
		return "docker";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "docker <command | help>";
	}

	@Override
	public List getCommandAliases() {
		return this.aliases;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {

		this.sender = sender;

		if (args.length == 0) {
			sendMessage(EnumChatFormatting.GOLD
					+ "For help with this command, use /docker help");
			return;
		}

		String command = args[0].toLowerCase();
		this.args = args;

		if (!argNumbers.containsKey(command)) {
			sendErrorMessage("\"" + command
					+ "\" is not a valid command! Use /docker help for help.");
			return;
		}

		if (dockerPath == null && argNumbers.get(command) != 2) {
			sendErrorMessage("Docker path has not been set! Set it using /docker path <path> .");
			return;
		}

		try {
			switch (argNumbers.get(command)) {
			case 0:
				help();
				break;
			case 1:
				ps();
				break;
			case 2:
				path();
				break;
			}
		} catch (Exception e) {
			sendErrorMessage(e.toString());
			e.printStackTrace();
			return;
		}

	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		return true;
	}

	@Override
	public boolean isUsernameIndex(String[] args, int i) {
		return false;
	}

	@Override
	public int compareTo(ICommand command) {
		return 0;
	}

	@Override
	public List<String> addTabCompletionOptions(ICommandSender sender,
			String[] args, BlockPos pos) {
		return null;
	}

	private void sendMessage(String message) {
		sender.addChatMessage(new ChatComponentText(message));
	}

	private void sendErrorMessage(String message) {
		sendMessage(EnumChatFormatting.DARK_RED + message);
	}

	private void sendConfirmMessage(String message) {
		sendMessage(EnumChatFormatting.GREEN + message);
	}

	private void sendHelpMessage(String command, String helpMessage) {
		sendMessage(EnumChatFormatting.DARK_GREEN + "/docker " + command
				+ " - " + EnumChatFormatting.GOLD + helpMessage);
	}

	private void help() {
		sendMessage(EnumChatFormatting.AQUA + "" + EnumChatFormatting.BOLD
				+ "================ Docker Help ================");
		sendHelpMessage("help", "Brings up this help page");
		sendHelpMessage("ps",
				"Lists all of your containers and some important information about them");
	}

	private void ps() {
		DockerClientConfig dockerConfig = DockerClientConfig
				.createDefaultConfigBuilder()
				.withUri("https://192.168.99.100:2376")
				.withDockerCertPath(dockerPath).build();

		sendMessage(EnumChatFormatting.AQUA + "Name(s)"
				+ EnumChatFormatting.RESET + ", " + EnumChatFormatting.GOLD
				+ "Image" + EnumChatFormatting.RESET + ", "
				+ EnumChatFormatting.GREEN + "Container ID");

		DockerClient dockerClient = DockerClientBuilder.getInstance(
				dockerConfig).build();
		List<Container> containers = dockerClient.listContainersCmd().exec();
		for (Container container : containers) {
			
//			for (String name : container.getNames()) {
//				System.out.println(name);
//			}
		}
	}

	private void path() {
		if (args.length < 2) {
			sendErrorMessage("Path is not specified! Command is used as /docker path <path> .");
			return;
		}

		dockerPath = args[1];
		sendConfirmMessage("Docker path set to \"" + args[1] + "\"");
	}
}