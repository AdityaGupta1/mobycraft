package org.redfrog404.mobycraft.commands;

import static org.redfrog404.mobycraft.commands.BasicContainerCommands.kill;
import static org.redfrog404.mobycraft.commands.BasicContainerCommands.killAll;
import static org.redfrog404.mobycraft.commands.BasicContainerCommands.remove;
import static org.redfrog404.mobycraft.commands.BasicContainerCommands.removeAll;
import static org.redfrog404.mobycraft.commands.BasicContainerCommands.removeStoppedContainers;
import static org.redfrog404.mobycraft.commands.BasicContainerCommands.restart;
import static org.redfrog404.mobycraft.commands.BasicContainerCommands.runContainer;
import static org.redfrog404.mobycraft.commands.BasicContainerCommands.startContainer;
import static org.redfrog404.mobycraft.commands.BasicContainerCommands.stopContainer;
import static org.redfrog404.mobycraft.commands.BuildContainerCommands.buildContainersFromList;
import static org.redfrog404.mobycraft.commands.BuildContainerCommands.refreshAndBuildContainers;
import static org.redfrog404.mobycraft.commands.BuildContainerCommands.setContainerAppearance;
import static org.redfrog404.mobycraft.commands.ContainerListCommands.asSortedList;
import static org.redfrog404.mobycraft.commands.ContainerListCommands.getAllContainers;
import static org.redfrog404.mobycraft.commands.ContainerListCommands.getBoxContainerWithID;
import static org.redfrog404.mobycraft.commands.ContainerListCommands.refreshContainerIDMap;
import static org.redfrog404.mobycraft.commands.ImageCommands.images;
import static org.redfrog404.mobycraft.commands.ImageCommands.removeAllImages;
import static org.redfrog404.mobycraft.commands.ImageCommands.removeImage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

import org.apache.http.conn.UnsupportedSchemeException;
import org.redfrog404.mobycraft.generic.BoxContainer;
import org.redfrog404.mobycraft.generic.Moby;
import org.redfrog404.mobycraft.generic.StructureBuilder;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;

public class DockerCommands implements ICommand {
	public static List aliases;

	static Map<String, Integer> argNumbers = new HashMap<String, Integer>();

	static Map<String, String> helpMessages = new HashMap<String, String>();

	static ICommandSender sender;

	static Property dockerPath;
	static Property startPos;
	static Property pollRate;

	static String[] args;
	static String arg1;

	// Contains current box containers
	static List<BoxContainer> boxContainers = new ArrayList<BoxContainer>();
	// Containers container's IDs
	static Map<String, BoxContainer> containerIDMap = new HashMap<String, BoxContainer>();
	// Used for byte conversions
	static Map<Integer, String> suffixNumbers = new HashMap<Integer, String>();

	static StructureBuilder builder = new StructureBuilder();

	public static int count = 0;
	public static int maxCount;

	public DockerCommands() {
		this.aliases = new ArrayList();
		this.aliases.add("docker");

		argNumbers.put("help", 0);
		argNumbers.put("ps", 1);
		argNumbers.put("path", 2);
		argNumbers.put("switch_state", 3);
		argNumbers.put("rm_stopped", 4);
		argNumbers.put("run", 5);
		argNumbers.put("kill_all", 6);
		argNumbers.put("kill", 7);
		argNumbers.put("restart", 8);
		argNumbers.put("rm", 9);
		argNumbers.put("rm_all", 10);
		argNumbers.put("stop", 11);
		argNumbers.put("start", 12);
		argNumbers.put("images", 13);
		argNumbers.put("rmi", 14);
		argNumbers.put("rmi_all", 15);
		argNumbers.put("set_start_pos", 16);

		helpMessages.put("help <page>", "Brings up page number <page> of this help list");
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

		suffixNumbers.put(0, "B");
		suffixNumbers.put(1, "KB");
		suffixNumbers.put(2, "MB");
		suffixNumbers.put(3, "GB");
	}

	public static void updateProperties() {
		dockerPath = Moby.config
				.get("files", "docker-cert-path", "File path",
						"The directory path of your Docker certificate (set using /docker path <path>)");
		startPos = Moby.config
				.get("container-building",
						"start-pos",
						"0, 0, 0",
						"The position - x, y, z - to start building contianers at (set using /docker start_pos");
		pollRate = Moby.config
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
		if (args.length > 1) {
			arg1 = args[1];
		}

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
				removeStoppedContainers();
				break;
			case 5:
				runContainer();
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
				stopContainer();
				break;
			case 12:
				startContainer();
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
				Moby.config.save();
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
		return new ArrayList<String>(argNumbers.keySet());
	}

	public static void sendMessage(String message) {
		sender.addChatMessage(new ChatComponentText(message));
	}

	public static void sendErrorMessage(String message) {
		sendMessage(EnumChatFormatting.DARK_RED + message);
	}

	public static void sendConfirmMessage(String message) {
		sendMessage(EnumChatFormatting.GREEN + message);
	}

	public static void sendFeedbackMessage(String message) {
		sendMessage(EnumChatFormatting.GOLD + message);
	}

	public static void sendBarMessage(EnumChatFormatting color) {
		sendMessage(color + "" + EnumChatFormatting.BOLD
				+ "=============================================");
	}

	public static void sendHelpMessage(String command, String helpMessage) {
		sendMessage(EnumChatFormatting.DARK_GREEN + "/docker " + command
				+ " - " + EnumChatFormatting.GOLD + helpMessage);
	}

	public static void help() {
		int size = helpMessages.size();
		
		int page = 1;
		int maxPages = ((size - size % 10) / 10) + 1;
		
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
		
		int toIndex = page * 10;
		if (toIndex > size) {
			toIndex = size;
		}
		
		for (String key : asSortedList(helpMessages.keySet()).subList((page - 1) * 10, toIndex)) {
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

	public static DockerClient getDockerClient() {
		DockerClient dockerClient;
		DockerClientConfig dockerConfig = DockerClientConfig
				.createDefaultConfigBuilder()
				.withUri("https://192.168.99.100:2376")
				.withDockerCertPath(dockerPath.getString()).build();
		dockerClient = DockerClientBuilder.getInstance(dockerConfig).build();
		return dockerClient;
	}

	public static void path() {
		if (args.length < 2) {
			sendErrorMessage("Docker path is not specified! Command is used as /docker path <path> .");
			return;
		}

		dockerPath.setValue(arg1);
		Moby.config.save();
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
	 * NOTE: DO NOT ADD "STATIC" MODIFIER
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

	public static void updateContainers() {
		List<Container> containers = getAllContainers();
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
		newContainersToBuild = builder.containerPanel(getAllContainers(),
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
}