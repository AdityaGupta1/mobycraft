package org.redfrog404.mobycraft.commands;

import static org.redfrog404.mobycraft.commands.BasicDockerCommands.help;
import static org.redfrog404.mobycraft.commands.BasicDockerCommands.host;
import static org.redfrog404.mobycraft.commands.BasicDockerCommands.path;
import static org.redfrog404.mobycraft.commands.BasicDockerCommands.ps;
import static org.redfrog404.mobycraft.commands.BasicDockerCommands.showDetailedInfo;
import static org.redfrog404.mobycraft.commands.BasicDockerCommands.specificHelpMessages;
import static org.redfrog404.mobycraft.commands.BuildContainerCommands.buildContainersFromList;
import static org.redfrog404.mobycraft.commands.BuildContainerCommands.refreshAndBuildContainers;
import static org.redfrog404.mobycraft.commands.BuildContainerCommands.setContainerAppearance;
import static org.redfrog404.mobycraft.commands.BuildContainerCommands.teleport;
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
import static org.redfrog404.mobycraft.commands.ContainerListCommands.getWithName;
import static org.redfrog404.mobycraft.commands.ContainerListCommands.heatMap;
import static org.redfrog404.mobycraft.commands.ContainerListCommands.isStopped;
import static org.redfrog404.mobycraft.commands.ContainerListCommands.refreshContainerIDMap;
import static org.redfrog404.mobycraft.commands.ImageCommands.images;
import static org.redfrog404.mobycraft.commands.ImageCommands.removeAllImages;
import static org.redfrog404.mobycraft.commands.ImageCommands.removeImage;
import static org.redfrog404.mobycraft.utils.MessageSender.sendConfirmMessage;
import static org.redfrog404.mobycraft.utils.MessageSender.sendErrorMessage;
import static org.redfrog404.mobycraft.utils.MessageSender.sendFeedbackMessage;
import static org.redfrog404.mobycraft.utils.MessageSender.sendMessage;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
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

public class MainCommand implements ICommand {
	public static List commandAliases;

	// Maps commands to numbers for use in the switch statement in
	// processCommand()
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

	static Property certPathProperty;
	static Property dockerHostProperty;
	static Property startPosProperty;
	static Property pollRateProperty;

	static String[] args;
	public static String arg1;

	static StructureBuilder builder = new StructureBuilder();

	public int count = 0;
	public int maxCount;

	static String dockerHost;
	static String certPath;

	private static boolean suppressWarnings = false;

	public MainCommand() {
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
		commandNumbers.put("show_detailed_info", 17);
		commandNumbers.put("tp", 18);
		commandNumbers.put("teleport", 18);
		commandNumbers.put("heat_map", 19);
		commandNumbers.put("host", 20);
		commandNumbers.put("get_host_and_path", 21);
		commandNumbers.put("get_path_and_host", 21);

		helpMessages
				.put("help (page | command)",
						"Brings up page number (page) of this help list or help specifically relating to command (command); if neither is specified, this command will show page 1 of regular help");
		helpMessages
				.put("ps [options]",
						"Lists all of your containers and some important information about them");
		helpMessages
				.put("path <path>",
						"Sets the Docker path to <path>; this value is only used if DOCKER_CERT_PATH environment variable is not set");
		helpMessages
				.put("host <host>",
						"Sets the Docker host to <host>; this value is only used if DOCKER_HOST environment variable is not set");
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
		helpMessages.put("teleport | tp <name>",
				"Teleports to the box container with the name <name>");
		helpMessages
				.put("heat_map <cpu | memory>",
						"Shows a list of the top five containers that use the most <cpu> or <memory>");
		helpMessages.put("get_host_and_path | get_path_and_host",
				"Returns the Docker host and cert path");

		specificHelpMessages
				.put("ps",
						new String[] { "ps [options]",
								"Does not show stopped containers by default",
								"[options]: \"-a\" to show all containers, including stopped ones" });
		specificHelpMessages
				.put("heat_map",
						new String[] {
								"heat_map <cpu | memory>",
								"Shows in the format of (container name) - (image) - (usage)",
								"If there are multiple containers with the same usage, the container name will say \"and (number) others\"",
								"If there are multiple containers with the same usage, the image refers to the one whose name is shown in the (container name)" });

		byteSuffixNumbers.put(0, "B");
		byteSuffixNumbers.put(1, "KB");
		byteSuffixNumbers.put(2, "MB");
		byteSuffixNumbers.put(3, "GB");
	}

	public void readConfigProperties() {
		certPathProperty = Mobycraft.config
				.get("files",
						"docker-cert-path",
						"File path",
						"The directory path of your Docker certificate (set using /docker path <path>); only used if DOCKER_CERT_PATH environment variable is not set");
		dockerHostProperty = Mobycraft.config
				.get("files",
						"docker-host",
						"Docker host IP",
						"The IP of your Docker host (set using /docker host <host>); only used if DOCKER_HOST environment variable is not set");
		startPosProperty = Mobycraft.config
				.get("container-building",
						"start-pos",
						"0, 0, 0",
						"The position - x, y, z - to start building containers at (set using /docker start_pos");
		pollRateProperty = Mobycraft.config
				.get("container-building",
						"poll-rate",
						"2",
						"The rate in seconds at which the containers will update (set using /docker poll_rate <rate in seconds>)");
		maxCount = (int) Math.floor((Float.parseFloat(pollRateProperty
				.getString()) * 50));
		System.out.println(maxCount);
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
		this.args = Arrays.copyOfRange(args, 1, args.length);
		if (args.length > 1) {
			arg1 = args[1];
		}

		if (!commandNumbers.containsKey(command)) {
			sendErrorMessage("\"" + command
					+ "\" is not a valid command! Use /docker help for help.");
			return;
		}

		int commandNumber = commandNumbers.get(command);

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
				startPosProperty.setValue(position.getX() + ", "
						+ position.getY() + ", " + position.getZ());
				Mobycraft.config.save();
				sendConfirmMessage("Set start position for building containers to ("
						+ startPosProperty.getString() + ").");
				readConfigProperties();
				updateContainers();
				break;
			case 17:
				showDetailedInfo();
				break;
			case 18:
				teleport();
				break;
			case 19:
				heatMap();
				break;
			case 20:
				host();
				break;
			case 21:
				getHostAndPath();
				break;
			}
		} catch (Exception exception) {
			if (exception instanceof UnsupportedSchemeException) {
				sendErrorMessage("Invalid Docker host/path! Set the host by using /docker host <host> ; set the path by using /docker path <path>");
			}
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
		return asSortedList(commandNumbers.keySet());
	}

	public static void sendHelpMessage(String command, String helpMessage) {
		sendMessage(EnumChatFormatting.DARK_GREEN + "/docker " + command
				+ " - " + EnumChatFormatting.GOLD + helpMessage);
	}

	public static DockerClient getDockerClient() {
		DockerClient dockerClient;

		if (!suppressWarnings) {
			refreshHostAndPath();
			suppressWarnings = true;
		}

		DockerClientConfig dockerConfig = DockerClientConfig
				.createDefaultConfigBuilder().withUri(dockerHost)
				.withDockerCertPath(certPath).build();
		dockerClient = DockerClientBuilder.getInstance(dockerConfig).build();

		return dockerClient;
	}

	/**
	 * Refreshes the Docker host and cert path
	 */
	private static void refreshHostAndPath() {
		dockerHost = System.getProperty("DOCKER_HOST");
		if (dockerHost == null) {
			sendFeedbackMessage("The DOCKER_HOST environment variable has not been set");

			if (dockerHostProperty.isDefault()) {
				dockerHost = "192.168.99.100:2376";
				sendConfirmMessage("Using default value of \"" + dockerHost
						+ "\"");
			} else {
				dockerHost = dockerHostProperty.getString();
				sendConfirmMessage("Using config value of \""
						+ dockerHostProperty.getString() + "\"");
			}
		} else {
			dockerHost = dockerHost.substring(6, dockerHost.length());
			sendConfirmMessage("Using DOCKER_HOST value of \"" + dockerHost
					+ "\"");
		}
		dockerHost = "https://" + dockerHost;

		certPath = System.getProperty("DOCKER_CERT_PATH");
		if (certPath == null) {
			sendFeedbackMessage("The DOCKER_CERT_PATH environment variable has not been set");

			if (certPathProperty.isDefault()) {
				certPath = System.getProperty("user.home")
						+ "machine/machines/default";
				sendConfirmMessage("Using default value of \"" + certPath
						+ "\"");
			} else {
				certPath = certPathProperty.getString();
				sendConfirmMessage("Using config value of \"" + certPath + "\"");
			}
		} else {
			sendConfirmMessage("Using DOCKER_CERT_PATH value of \"" + certPath
					+ "\"");
		}
	}

	public static void switchState(String containerID) {
		refreshContainerIDMap();

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
			if (args[argNumber].equals(null)) {
				return true;
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			return true;
		}
		return false;
	}

	public static BlockPos getStartPosition() {
		String[] posStrings = startPosProperty.getString().split(", ");
		return new BlockPos(Integer.parseInt(posStrings[0]),
				Integer.parseInt(posStrings[1]),
				Integer.parseInt(posStrings[2]));
	}

	/*
	 * NOTE TO SELF: DO NOT ADD "STATIC" MODIFIER - otherwise exception is
	 * thrown
	 */
	/**
	 * Used to update containers at the rate specified by the poll rate in the
	 * config file
	 */
	@SubscribeEvent
	public void onTick(PlayerTickEvent event) {
		if (event.player.getEntityWorld().isRemote) {
			return;
		}

		count++;
		if (count >= maxCount) {
			sender = event.player;
			if (boxContainers == null) {
				refreshAndBuildContainers();
			} else {
				updateContainers();
			}
			count = 0;
		}
	}

	private void updateContainers() {
		refreshContainerIDMap();

		List<Container> containers = getAll();
		List<BoxContainer> newContainers = builder.containerPanel(containers,
				getStartPosition(), sender.getEntityWorld());

		if (boxContainers.equals(newContainers)) {
			return;
		}

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
		start--;
		
		if (start < 0) {
			start = 0;
		}

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

		boxContainers = newContainers;

		for (BoxContainer container : boxContainers) {
			if (isStopped(container.getName())) {
				setContainerAppearance(container, false);
				container.setState(false);
			} else {
				container.setState(true);
			}
		}
	}

	public static String imageSizeConversion(double bytes) {
		int suffixNumber = 0;

		/*
		 * Docker seems to convert bytes using 1000 instead of 1024
		 */
		while (bytes / 1000 > 1) {
			bytes /= 1000;
			suffixNumber++;
		}

		NumberFormat formatter = new DecimalFormat("#0.0");
		String byteString = formatter.format(bytes) + " "
				+ byteSuffixNumbers.get(suffixNumber);
		if (byteString.contains(".0")) {
			byteString = byteString.replace(".0", "");
		}

		return byteString;
	}

	@SubscribeEvent
	public void containerWand(PlayerInteractEvent event) {
		EntityPlayer player = event.entityPlayer;

		if (!event.action.equals(Action.RIGHT_CLICK_BLOCK)
				|| !event.action.equals(Action.LEFT_CLICK_BLOCK)) {
			return;
		}

		if (player.getHeldItem() == null
				|| player.getHeldItem().getItem() != Mobycraft.container_wand) {
			return;
		}

		World world = event.world;
		BlockPos pos = event.pos;

		if (world.getBlockState(pos).getBlock() != Blocks.wall_sign
				&& world.getBlockState(pos).getBlock() != Blocks.standing_sign) {
			return;
		}

		TileEntitySign sign = (TileEntitySign) world.getTileEntity(pos);

		if (!sign.signText[0].getUnformattedText().contains("Name:")) {
			return;
		}

		String name = sign.signText[1].getUnformattedText().concat(
				sign.signText[2].getUnformattedText().concat(
						sign.signText[3].getUnformattedText()));

		System.out.println(name);

		if (getWithName(name) == null) {
			return;
		}

		Container container = getWithName(name);
		getDockerClient().removeContainerCmd(container.getId()).withForce()
				.exec();
		sendConfirmMessage("Removed container with name \"" + name + "\"");
	}

	private void getHostAndPath() {
		if (dockerHost == null || certPath == null) {
			refreshHostAndPath();
		}

		sendMessage(EnumChatFormatting.BLUE + "" + EnumChatFormatting.BOLD
				+ "Docker host: " + EnumChatFormatting.RESET + dockerHost);
		sendMessage(EnumChatFormatting.GOLD + "" + EnumChatFormatting.BOLD
				+ "Docker cert path: " + EnumChatFormatting.RESET + certPath);
	}
}