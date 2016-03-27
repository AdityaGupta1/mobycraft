package org.redfrog404.mobycraft.commands;

import static org.redfrog404.mobycraft.commands.BuildContainerCommands.buildContainersFromList;
import static org.redfrog404.mobycraft.commands.BuildContainerCommands.refreshAndBuildContainers;
import static org.redfrog404.mobycraft.commands.BuildContainerCommands.setContainerAppearance;
import static org.redfrog404.mobycraft.commands.ContainerLifecycleCommands.kill;
import static org.redfrog404.mobycraft.commands.ContainerLifecycleCommands.killAll;
import static org.redfrog404.mobycraft.commands.ContainerLifecycleCommands.remove;
import static org.redfrog404.mobycraft.commands.ContainerLifecycleCommands.removeAll;
import static org.redfrog404.mobycraft.commands.ContainerLifecycleCommands.removeStopped;
import static org.redfrog404.mobycraft.commands.ContainerLifecycleCommands.restart;
import static org.redfrog404.mobycraft.commands.ContainerLifecycleCommands.run;
import static org.redfrog404.mobycraft.commands.ContainerLifecycleCommands.start;
import static org.redfrog404.mobycraft.commands.ContainerLifecycleCommands.stop;
import static org.redfrog404.mobycraft.commands.ContainerListCommands.asSortedList;
import static org.redfrog404.mobycraft.commands.ContainerListCommands.getAll;
import static org.redfrog404.mobycraft.commands.ContainerListCommands.getBoxContainerWithID;
import static org.redfrog404.mobycraft.commands.ContainerListCommands.refreshContainerIDMap;
import static org.redfrog404.mobycraft.commands.ImageCommands.images;
import static org.redfrog404.mobycraft.commands.ImageCommands.removeAllImages;
import static org.redfrog404.mobycraft.commands.ImageCommands.removeImage;
import static org.redfrog404.mobycraft.utils.SendMessagesToCommandSender.sendBarMessage;
import static org.redfrog404.mobycraft.utils.SendMessagesToCommandSender.sendConfirmMessage;
import static org.redfrog404.mobycraft.utils.SendMessagesToCommandSender.sendErrorMessage;
import static org.redfrog404.mobycraft.utils.SendMessagesToCommandSender.sendFeedbackMessage;
import static org.redfrog404.mobycraft.utils.SendMessagesToCommandSender.sendMessage;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

import org.apache.http.conn.UnsupportedSchemeException;
import org.redfrog404.mobycraft.main.Mobycraft;
import org.redfrog404.mobycraft.utils.BoxContainer;
import org.redfrog404.mobycraft.utils.StructureBuilder;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;

public class DockerCommands implements ICommand {
	public static List commandAliases;

	// Maps commands to numbers for use in the switch statement in processCommand()
	static Map<String, Integer> commandNumbers = new HashMap<String, Integer>();
	// Help messages that are shown when /docker help is used
	static Map<String, String> helpMessages = new HashMap<String, String>();
	// Current box containers
	static List<BoxContainer> boxContainers = new ArrayList<BoxContainer>();
	// Containers container's IDs
	static Map<String, BoxContainer> containerIDMap = new HashMap<String, BoxContainer>();
	// Used for byte conversions
	static Map<Integer, String> byteSuffixNumbers = new HashMap<Integer, String>();

	public static ICommandSender sender;

	static Property dockerPath;
	static Property startPos;
	static Property pollRate;

	static String[] args;
	static String arg1;

	static StructureBuilder builder = new StructureBuilder();

	public int count = 0;
	public int maxCount;

	public DockerCommands() {
		this.commandAliases = new ArrayList();
		this.commandAliases.add("docker");

		commandNumbers.put("help", 0);
		commandNumbers.put("ps", 1);
		commandNumbers.put("path", 2);
		commandNumbers.put("switch_state", 3);
		commandNumbers.put("rm_stopped", 4);
		commandNumbers.put("run", 5);
		commandNumbers.put("kill_all", 6);
		commandNumbers.put("kill", 7);
		commandNumbers.put("restart", 8);
		commandNumbers.put("rm", 9);
		commandNumbers.put("rm_all", 10);
		commandNumbers.put("stop", 11);
		commandNumbers.put("start", 12);
		commandNumbers.put("images", 13);
		commandNumbers.put("rmi", 14);
		commandNumbers.put("rmi_all", 15);
		commandNumbers.put("set_start_pos", 16);

		helpMessages.put("help <page>",
				"Brings up page number <page> of this help list");
		helpMessages
				.put("ps",
						"Lists all of your containers and some important information about them");
		helpMessages.put("path <path>", "Sets the Docker path to <path>");
		helpMessages
				.put("run <image> (name | amount)",
						"Creates and runs a container with image <image> and name (name) or (amount) number of containers");
		helpMessages.put("kill <name>", "Kills container <name>");
		helpMessages.put("kill_all", "Kills all currently running containers");
		helpMessages.put("restart <name>", "Restarts container <name>");
		helpMessages.put("rm <name>", "Removes container <name>");
		helpMessages.put("rm_all", "Removes all containers");
		helpMessages.put("stop <name>", "Stops container <name>");
		helpMessages.put("start <name>", "Starts container <name>");
		helpMessages.put("images",
				"Lists all of your currently installed images");
		helpMessages.put("rmi <name>", "Removes image <name>");
		helpMessages.put("rmi_all", "Removes all images");
		helpMessages
				.put("set_start_pos",
						"Sets the start position of container building to the sender's current position");
		helpMessages.put("rm_stopped",
				"Removes all currently stopped containers");

		byteSuffixNumbers.put(0, "B");
		byteSuffixNumbers.put(1, "KB");
		byteSuffixNumbers.put(2, "MB");
		byteSuffixNumbers.put(3, "GB");
	}
	
	public void readConfigProperties() {
		dockerPath = Mobycraft.config
				.get("files", "docker-cert-path", "File path",
						"The directory path of your Docker certificate (set using /docker path <path>)");
		startPos = Mobycraft.config
				.get("container-building",
						"start-pos",
						"0, 0, 0",
						"The position - x, y, z - to start building contianers at (set using /docker start_pos");
		pollRate = Mobycraft.config
				.get("container-building",
						"poll-rate",
						"2",
						"The rate in seconds at which the containers will update (set using /docker poll_rate <rate in seconds>)");
		maxCount = (int) Math
				.floor((Float.parseFloat(pollRate.getString()) * 50));
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
		return this.commandAliases;
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
		if (args.length > 1) {
			arg1 = args[1];
		}

		if (!commandNumbers.containsKey(command)) {
			sendErrorMessage("\"" + command
					+ "\" is not a valid command! Use /docker help for help.");
			return;
		}

		int commandNumber = commandNumbers.get(command);

		if (dockerPath.isDefault() && commandNumber != 2) {
			sendErrorMessage("Docker path has not been set! Set it using /docker path <path> .");
			return;
		}

		BlockPos position = sender.getPosition();

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
				switchState(arg1);
				break;
			case 4:
				removeStopped();
				break;
			case 5:
				run();
				break;
			case 6:
				killAll();
				break;
			case 7:
				kill();
				break;
			case 8:
				restart();
				break;
			case 9:
				remove();
				break;
			case 10:
				removeAll();
				break;
			case 11:
				stop();
				break;
			case 12:
				start();
				break;
			case 13:
				images();
				break;
			case 14:
				removeImage();
				break;
			case 15:
				removeAllImages();
				break;
			case 16:
				startPos.setValue(position.getX() + ", " + position.getY()
						+ ", " + position.getZ());
				Mobycraft.config.save();
				sendConfirmMessage("Set start position for building containers to ("
						+ startPos.getString() + ").");
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
		return new ArrayList<String>(commandNumbers.keySet());
	}

	private void sendHelpMessage(String command, String helpMessage) {
		sendMessage(EnumChatFormatting.DARK_GREEN + "/docker " + command
				+ " - " + EnumChatFormatting.GOLD + helpMessage);
	}

	private void help() {
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

	private void ps() {
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

	public static DockerClient getDockerClient() {
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

		dockerPath.setValue(arg1);
		Mobycraft.config.save();
		sendConfirmMessage("Docker path set to \"" + arg1 + "\"");
	}

	public static void switchState(String containerID) {
		// If there is no container with the ID, return
		if (getBoxContainerWithID(containerID) == null) {
			return;
		}

		// New BoxContainer variable called boxContainer to store the container
		BoxContainer boxContainer = getBoxContainerWithID(containerID);

		boxContainer.setState(!boxContainer.getState());

		Block containerBlock;
		Block prevContainerBlock;

		// If the container is now on (previously off):
		if (boxContainer.getState()) {
			containerBlock = Blocks.iron_block;
			prevContainerBlock = Blocks.redstone_block;
			getDockerClient().startContainerCmd(boxContainer.getID()).exec();

		}
		// Otherwise, if the container is now off (previously on):
		else {
			containerBlock = Blocks.redstone_block;
			prevContainerBlock = Blocks.iron_block;

			getDockerClient().stopContainerCmd(boxContainer.getID()).exec();

		}

		builder.replace(boxContainer.getWorld(), boxContainer.getPosition()
				.add(2, 0, 1), boxContainer.getPosition().add(-2, 0, 7),
				prevContainerBlock, containerBlock);

		builder.replace(boxContainer.getWorld(), boxContainer.getPosition()
				.add(2, 4, 1), boxContainer.getPosition().add(-2, 4, 7),
				prevContainerBlock, containerBlock);
	}

	public static boolean checkIfArgIsNull(int argNumber) {
		try {
			if (args[argNumber - 1].equals(null)) {
				return true;
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			return true;
		}
		return false;
	}

	public static BlockPos getStartPosition() {
		String[] posStrings = startPos.getString().split(", ");
		return new BlockPos(Integer.parseInt(posStrings[0]),
				Integer.parseInt(posStrings[1]),
				Integer.parseInt(posStrings[2]));
	}

	/*
	 * Used to update containers at the rate specified by the poll rate in the
	 * config file
	 * 
	 * NOTE TO SELF: DO NOT ADD "STATIC" MODIFIER - otherwise exception is thrown
	 */
	@SubscribeEvent
	public void onTick(PlayerTickEvent event) {
		if (event.player.getEntityWorld().isRemote) {
			return;
		}
		count++;
		if (count >= maxCount) {
			sender = event.player;
			if (boxContainers.equals(null)) {
				refreshAndBuildContainers();
			} else {
				updateContainers();
			}
			count = 0;
		}
	}

	private void updateContainers() {
		List<Container> containers = getAll();
		List<BoxContainer> newContainers = builder.containerPanel(containers,
				getStartPosition(), sender.getEntityWorld());
		if (boxContainers.equals(newContainers)) {
			return;
		}

		refreshContainerIDMap();

		int start = 0;

		findDifferences: for (; start < boxContainers.size(); start++) {
			if (start == newContainers.size()) {
				start--;
				break findDifferences;
			}
			if (!boxContainers.get(start).equals(newContainers.get(start))) {
				break findDifferences;
			}
		}

		start -= start % 10;

		List<BoxContainer> containersToReplace = new ArrayList<BoxContainer>();
		containersToReplace = boxContainers
				.subList(start, boxContainers.size());

		for (int i = 0; i < containersToReplace.size(); i++) {
			builder.airContainer(sender.getEntityWorld(), containersToReplace
					.get(i).getPosition());
		}

		List<BoxContainer> newContainersToBuild = new ArrayList<BoxContainer>();
		newContainersToBuild = builder.containerPanel(getAll(),
				getStartPosition(), sender.getEntityWorld());
		newContainersToBuild = newContainersToBuild.subList(start,
				newContainersToBuild.size());
		buildContainersFromList(newContainersToBuild);

		for (BoxContainer container : newContainersToBuild) {
			if (!container.getState()) {
				setContainerAppearance(container.getID(), false);
			}
		}

		boxContainers = newContainers;
	}

	public static String convertBytesAndMultiply(double bytes) {
		int suffixNumber = 0;

		while (bytes / 1024 > 1) {
			bytes /= 1024;
			suffixNumber++;
		}

		// Multiplying by a constant that seems to make the memory size match that shown by the Docker command "docker images"
		if (suffixNumber != 0) {
			bytes *= 1.04851005D;
		}

		NumberFormat formatter = new DecimalFormat("#0.0");
		String byteString = formatter.format(bytes) + " "
				+ byteSuffixNumbers.get(suffixNumber);
		if (byteString.contains(".0")) {
			byteString = byteString.replace(".0", "");
		}

		return byteString;
	}
}