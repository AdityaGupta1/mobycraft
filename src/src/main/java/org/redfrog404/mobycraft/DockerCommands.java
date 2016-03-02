package org.redfrog404.mobycraft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Vec3i;
import net.minecraftforge.common.config.Property;

import org.apache.http.conn.UnsupportedSchemeException;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;

public class DockerCommands implements ICommand {
	private List aliases;

	Map<String, Integer> argNumbers = new HashMap<String, Integer>();

	Map<String, String> helpMessages = new HashMap<String, String>();

	ICommandSender sender;

	Property dockerPath = Moby.config.get("files", "docker-cert-path",
			"File path", "The directory path of your Docker certificate");

	String[] args;

	List<BoxContainer> boxContainers = new ArrayList<BoxContainer>();

	public DockerCommands() {
		this.aliases = new ArrayList();
		this.aliases.add("docker");

		argNumbers.put("help", 0);
		argNumbers.put("ps", 1);
		argNumbers.put("path", 2);
		// TODO Remove after adding automatic building when player joins game
		argNumbers.put("build_containers", 3);

		helpMessages.put("help", "Brings up this help page");
		helpMessages
				.put("ps",
						"Lists all of your containers and some important information about them");
		helpMessages.put("path <path>", "Sets the Docker path to <path>");
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
			sendFeedbackMessage("For help with this command, use /docker help");
			return;
		}

		String command = args[0].toLowerCase();
		this.args = args;

		if (!argNumbers.containsKey(command)) {
			sendErrorMessage("\"" + command
					+ "\" is not a valid command! Use /docker help for help.");
			return;
		}

		int commandNumber = argNumbers.get(command);

		if (dockerPath.isDefault() && commandNumber != 2) {
			sendErrorMessage("Docker path has not been set! Set it using /docker path <path> .");
			return;
		}

		try {
			switch (commandNumber) {
			case 0:
				help();
				break;
			case 1:
				ps();
				break;
			case 2:
				path();
				break;
			case 3:
				refreshAndBuildContainers(sender.getPosition());
				break;
			}
		} catch (Exception e) {
			if (e instanceof UnsupportedSchemeException && commandNumber == 2) {
				sendErrorMessage("Invalid Docker path! Set it using /docker path <path> .");
				return;
			}
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

	private void sendFeedbackMessage(String message) {
		sendMessage(EnumChatFormatting.GOLD + message);
	}

	private void sendBarMessage(EnumChatFormatting color) {
		sendMessage(color + "" + EnumChatFormatting.BOLD
				+ "=============================================");
	}

	private void sendHelpMessage(String command, String helpMessage) {
		sendMessage(EnumChatFormatting.DARK_GREEN + "/docker " + command
				+ " - " + EnumChatFormatting.GOLD + helpMessage);
	}

	private void help() {
		sendMessage(EnumChatFormatting.BLUE + "" + EnumChatFormatting.BOLD
				+ "============== Docker Help ==============");

		// for (int help = 0; help < helpMessages.size(); help++) {
		// sendHelpMessage(helpMessages.keySet().toArray()[help].toString(),
		// helpMessages.get(helpMessages.keySet().toArray()[help]));
		// }

		for (String key : helpMessages.keySet()) {
			sendHelpMessage(key, helpMessages.get(key));
		}
	}

	private void ps() {

		sendFeedbackMessage("Loading...");

		DockerClient dockerClient = getDockerClient();

		List<Container> containers = dockerClient.listContainersCmd().exec();

		if (containers.size() == 0) {
			sendFeedbackMessage("No containers currently running");
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

	private DockerClient getDockerClient() {
		DockerClient dockerClient;
		DockerClientConfig dockerConfig = DockerClientConfig
				.createDefaultConfigBuilder()
				.withUri("https://192.168.99.100:2376")
				.withDockerCertPath(dockerPath.getString()).build();
		dockerClient = DockerClientBuilder.getInstance(dockerConfig).build();
		return dockerClient;
	}

	private void path() {
		if (args.length < 2) {
			sendErrorMessage("Docker path is not specified! Command is used as /docker path <path> .");
			return;
		}

		dockerPath.setValue(args[1]);
		Moby.config.save();
		sendConfirmMessage("Docker path set to \"" + args[1] + "\"");
	}

	public void refreshContainers(BlockPos pos) {
//		DockerClient dockerClient = getDockerClient();
//
//		List<Container> containers = dockerClient.listContainersCmd().exec();
//		for (Container container : containers) {
//			boxContainers.add(new BoxContainer(pos, container.getId()));
//			pos = pos.add(6, 0, 0);
//		}
		
		boxContainers = Moby.builder.containerPanel(getContainers(), pos);
	}

	public List<Container> getContainers() {
		DockerClient dockerClient = getDockerClient();
		return dockerClient.listContainersCmd().exec();
	}

	public void buildContainers() {
		DockerClient dockerClient = getDockerClient();

		for (BoxContainer boxContainer : boxContainers) {
			String containerName = "";
			String containerImage = "";

			List<Container> containers = dockerClient.listContainersCmd()
					.exec();
			for (Container container : containers) {
				if (container.getId().equals(boxContainer.getID())) {
					containerName = container.getNames()[0];
					containerImage = container.getImage();
					break;
				}
			}

			if (!containerName.equals("")) {
				Moby.builder.container(sender.getEntityWorld(),
						boxContainer.getPosition(), Blocks.iron_block,
						containerName, containerImage);
			}
		}
	}

	public void refreshAndBuildContainers(BlockPos pos) {
		sendFeedbackMessage("Getting containers...");
		refreshContainers(pos);
		sendFeedbackMessage("Building containers...");
		buildContainers();
		sendFeedbackMessage("Done!");
	}
}