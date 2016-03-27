package org.redfrog404.mobycraft;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.http.conn.UnsupportedSchemeException;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;

public class DockerCommands implements ICommand {
	private static List aliases;

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

	StructureBuilder builder = new StructureBuilder();

	private static int count = 0;
	private static int maxCount;

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

	private void removeStoppedContainers() {
		sendFeedbackMessage("Working on it...");
		if (getStoppedContainers().size() < 1) {
			sendFeedbackMessage("No containers currently stopped.");
			return;
		}
		for (Container container : getStoppedContainers()) {
			getDockerClient().removeContainerCmd(container.getId()).withForce()
					.exec();
		}
		sendConfirmMessage("Removed all stopped containers.");
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

		dockerPath.setValue(arg1);
		Moby.config.save();
		sendConfirmMessage("Docker path set to \"" + arg1 + "\"");
	}

	public void refreshContainers() {
		List<Container> containers = getAllContainers();
		boxContainers = builder.containerPanel(containers, getStartPosition(),
				sender.getEntityWorld());
		List<String> stoppedContainerNames = new ArrayList<String>();
		for (Container container : getStoppedContainers()) {
			stoppedContainerNames.add(container.getNames()[0]);
		}
		for (BoxContainer boxContainer : boxContainers) {
			if (stoppedContainerNames.contains(boxContainer.getName())) {
				boxContainer.setState(!boxContainer.getState());
			}
		}
		refreshContainerIDMap();
	}

	public void refreshContainerIDMap() {
		containerIDMap.clear();
		for (BoxContainer boxContainer : boxContainers) {
			containerIDMap.put(boxContainer.getID(), boxContainer);	
		}
	}

	public List<Container> getContainers() {
		return getDockerClient().listContainersCmd().exec();
	}

	public List<Container> getStoppedContainers() {
		List<Container> containers = new ArrayList<Container>();
		for (Container container : getDockerClient().listContainersCmd()
				.withShowAll(true).exec()) {
			if (container.getStatus().toLowerCase().contains("exited")) {
				containers.add(container);
			}
		}

		return containers;
	}

	public void buildContainers() {
		buildContainersFromList(boxContainers);
	}

	public void buildContainersFromList(List<BoxContainer> containers) {
		for (BoxContainer boxContainer : containers) {
			builder.container(sender.getEntityWorld(),
					boxContainer.getPosition(), Blocks.iron_block,
					boxContainer.getName(), boxContainer.getImage(),
					boxContainer.getID());
			if (!boxContainer.getState()) {
				setContainerAppearance(boxContainer.getID(), false);
			}
		}
	}

	public void refreshAndBuildContainers() {
		refreshContainers();
		if (boxContainers.size() < 1) {
			return;
		}
		buildContainers();
	}

	public Container getContainerWithName(String name) {
		for (Container container : getContainers()) {
			if (container.getNames()[0].equals(name)) {
				return container;
			}
		}
		return null;
	}

	public Container getContainerFromAllContainersWithName(String name) {
		List<Container> containers = getAllContainers();
		for (Container container : containers) {
			if (container.getNames()[0].equals(name)) {
				return container;
			}
		}
		return null;
	}

	public List<Container> getAllContainers() {
		List<Container> containers = getContainers();
		containers.addAll(getStoppedContainers());
		return containers;
	}

	/*
	 * Gets the BoxContainer from the containerIDMap with id <id>
	 */
	public BoxContainer getBoxContainerWithID(String id) {
		if (!containerIDMap.containsKey(id)) {
			return null;
		}
		return containerIDMap.get(id);
	}

	private void switchState(String containerID) {
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

	private void runContainer() {
		if (getImageWithName(arg1).equals(null)) {
			sendErrorMessage("The requested image is not pulled yet! Please pull the image and run this command again. NOTE: This is a bug and will be fixed.");
			getDockerClient().pullImageCmd(arg1).exec(null);
		}

		if (args.length < 3) {
			// No name, no number
			CreateContainerResponse response = getDockerClient()
					.createContainerCmd(arg1).exec();
			getDockerClient().startContainerCmd(response.getId()).exec();
			String name = "";
			for (Container container : getContainers()) {
				if (container.getId().equals(response.getId())) {
					name = container.getNames()[0];
				}
			}
			sendConfirmMessage("Created container with image \"" + arg1
					+ "\" and name \"" + name + "\"");
		} else if (!NumberUtils.isNumber(args[2])) {
			// Name
			CreateContainerResponse response = getDockerClient()
					.createContainerCmd(arg1).withName(args[2]).exec();
			getDockerClient().startContainerCmd(response.getId()).exec();
			sendConfirmMessage("Created container with image \"" + arg1
					+ "\" and name \"/" + args[2] + "\"");
		} else {
			// Number
			ArrayList<String> names = new ArrayList<String>();
			for (int i = 0; i < Integer.parseInt(args[2]); i++) {
				CreateContainerResponse response = getDockerClient()
						.createContainerCmd(arg1).exec();
				getDockerClient().startContainerCmd(response.getId()).exec();
				String name = "";
				for (Container container : getContainers()) {
					if (container.getId().equals(response.getId())) {
						name = container.getNames()[0];
						names.add(name);
					}
				}
			}

			String namesMessage = "";

			for (int i = 0; i < names.size(); i++) {
				if (i == names.size() - 1) {
					namesMessage.concat(" and \"" + names.get(i) + "\"");
				} else {
					namesMessage.concat("\"" + names.get(i) + "\", ");
				}
			}

			sendConfirmMessage("Created containers with image \"" + arg1
					+ "\" and names " + names);
		}
	}

	private void killAll() {
		sendFeedbackMessage("Working on it...");
		if (getContainers().size() < 1) {
			sendFeedbackMessage("No containers currently running.");
			return;
		}
		for (Container container : getContainers()) {
			getDockerClient().killContainerCmd(container.getId()).exec();
		}
		sendConfirmMessage("Killed all containers.");
	}

	private void kill() {
		if (checkIfArgIsNull(2)) {
			sendErrorMessage("Container name not specified! Command is used as /docker kill <name> .");
			return;
		}

		try {
			getDockerClient().killContainerCmd(
					getContainerWithName("/" + arg1).getId()).exec();
			sendConfirmMessage("Killed container with name \"/" + arg1 + "\"");
		} catch (NullPointerException exception) {
			sendErrorMessage("No container exists with the name \"/" + arg1
					+ "\"");
		}
	}

	public static <T extends Comparable<? super T>> List<T> asSortedList(
			Collection<T> c) {
		List<T> list = new ArrayList<T>(c);
		java.util.Collections.sort(list);
		return list;
	}

	private void restart() {
		if (checkIfArgIsNull(2)) {
			sendErrorMessage("Container name not specified! Command is used as /docker restart <name> .");
			return;
		}

		try {
			getDockerClient().restartContainerCmd(
					getContainerWithName("/" + arg1).getId()).exec();
			sendConfirmMessage("Restart container with name \"/" + arg1 + "\"");
		} catch (NullPointerException exception) {
			sendErrorMessage("No container exists with the name \"/" + arg1
					+ "\"");
		}
	}

	private boolean checkIfArgIsNull(int argNumber) {
		try {
			if (args[argNumber - 1].equals(null)) {
				return true;
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			return true;
		}
		return false;
	}

	private void removeAll() {
		sendFeedbackMessage("Working on it...");
		if (getAllContainers().size() < 1) {
			sendFeedbackMessage("No containers currently existing.");
			return;
		}
		for (Container container : getAllContainers()) {
			getDockerClient().removeContainerCmd(container.getId()).withForce()
					.exec();
		}
		sendConfirmMessage("Removed all containers.");
	}

	private void remove() {
		if (checkIfArgIsNull(2)) {
			sendErrorMessage("Container name not specified! Command is used as /docker rm <name> .");
			return;
		}

		try {
			getDockerClient()
					.removeContainerCmd(
							getContainerFromAllContainersWithName("/" + arg1)
									.getId()).withForce().exec();
			sendConfirmMessage("Removed container with name \"/" + arg1 + "\"");
		} catch (NullPointerException exception) {
			sendErrorMessage("No container exists with the name \"/" + arg1
					+ "\"");
		}
	}

	private void stopContainer() {
		if (checkIfArgIsNull(2)) {
			sendErrorMessage("Container name not specified! Command is used as /docker stop <name> .");
			return;
		}

		try {
			getDockerClient().stopContainerCmd(
					getContainerWithName("/" + arg1).getId()).exec();
			sendConfirmMessage("Stopped container with name \"/" + arg1 + "\"");
		} catch (NullPointerException exception) {
			sendErrorMessage("No container exists with the name \"/" + arg1
					+ "\"");
		}
	}

	private void startContainer() {
		if (checkIfArgIsNull(2)) {
			sendErrorMessage("Container name not specified! Command is used as /docker start <name> .");
			return;
		}

		try {
			getDockerClient().startContainerCmd(
					getContainerFromAllContainersWithName("/" + arg1).getId())
					.exec();
			sendConfirmMessage("Started container with name \"/" + arg1 + "\"");
		} catch (NullPointerException exception) {
			sendErrorMessage("No container exists with the name \"/" + arg1
					+ "\"");
		}
	}
	
	private void images() {
		sendFeedbackMessage("Loading...");

		DockerClient dockerClient = getDockerClient();

		List<Image> images = dockerClient.listImagesCmd().exec();

		if (images.size() == 0) {
			sendFeedbackMessage("No images currently installed.");
			return;
		}

		sendBarMessage(EnumChatFormatting.BLUE);
		sendMessage(EnumChatFormatting.AQUA + "Repository"
				+ EnumChatFormatting.RESET + ", " + EnumChatFormatting.GOLD
				+ "Tag" + EnumChatFormatting.RESET + ", "
				+ EnumChatFormatting.GREEN + "Size");
		sendBarMessage(EnumChatFormatting.BLUE);

		String tag = "";

		for (Image image : images) {
			String message = "";
			for (String name : image.getRepoTags()) {
				if (image.getRepoTags()[0].equals(name)) {
					message += EnumChatFormatting.AQUA + name.split(":")[0];
				} else {
					message += ", " + name.split(":")[0];
				}
				tag = name.split(":")[1];
			}
			message += EnumChatFormatting.RESET + ", "
					+ EnumChatFormatting.GOLD + tag + EnumChatFormatting.RESET
					+ ", " + EnumChatFormatting.GREEN
					+ convertBytesAndMultiply(image.getSize());
			sendMessage(message);
		}
	}

	public String convertBytesAndMultiply(double bytes) {
		int suffixNumber = 0;

		while (bytes / 1024 > 1) {
			bytes /= 1024;
			suffixNumber++;
		}

		if (suffixNumber != 0) {
			bytes *= 1.04851005D;
		}

		NumberFormat formatter = new DecimalFormat("#0.0");
		String byteString = formatter.format(bytes) + " "
				+ suffixNumbers.get(suffixNumber);
		if (byteString.contains(".0")) {
			byteString = byteString.replace(".0", "");
		}

		return byteString;
	}

	private void removeAllImages() {
		sendFeedbackMessage("Working on it...");
		if (getAllContainers().size() < 1) {
			sendFeedbackMessage("No images currently currently.");
			return;
		}
		for (Image image : getImages()) {
			getDockerClient().removeImageCmd(image.getId()).exec();
		}
		sendConfirmMessage("Removed all images.");
	}

	private void removeImage() {
		if (checkIfArgIsNull(2)) {
			sendErrorMessage("Container name not specified! Command is used as /docker rmi <name> .");
			return;
		}

		try {
			getDockerClient().removeImageCmd(getImageWithName(arg1).getId())
					.withForce().exec();
			sendConfirmMessage("Removed image with name \"" + arg1 + "\"");
		} catch (NullPointerException exception) {
			sendErrorMessage("No image exists with the name \"" + arg1 + "\"");
		}
	}

	public List<Image> getImages() {
		return getDockerClient().listImagesCmd().exec();
	}

	public Image getImageWithName(String name) {
		for (Image image : getImages()) {
			if (image.getRepoTags()[0].split(":")[0].equals(name)) {
				return image;
			}
		}
		return null;
	}

	public void updateContainers() {
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

	public BlockPos getStartPosition() {
		String[] posStrings = startPos.getString().split(", ");
		return new BlockPos(Integer.parseInt(posStrings[0]),
				Integer.parseInt(posStrings[1]),
				Integer.parseInt(posStrings[2]));
	}

	public boolean isContainerStopped(String containerName) {
		if (getContainerWithName(containerName).equals(null)) {
			return false;
		}

		Container container = getContainerWithName(containerName);

		if (container.getStatus().toLowerCase().contains("exited")) {
			return true;
		}

		return false;
	}

	/*
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
			if (boxContainers.equals(null)) {
				refreshAndBuildContainers();
			} else {
				updateContainers();
			}
			count = 0;
		}
	}

	private void setContainerAppearance(String containerID, boolean state) {
		
		System.out.println(containerIDMap);
		System.out.println(getBoxContainerWithID(containerID));

		if (getBoxContainerWithID(containerID) == null) {
			return;
		}

		BoxContainer boxContainer = getBoxContainerWithID(containerID);

		Block containerBlock;
		Block prevContainerBlock;

		if (state) {
			containerBlock = Blocks.iron_block;
			prevContainerBlock = Blocks.redstone_block;

		} else {
			containerBlock = Blocks.redstone_block;
			prevContainerBlock = Blocks.iron_block;
		}

		builder.replace(boxContainer.getWorld(), boxContainer.getPosition()
				.add(2, 0, 1), boxContainer.getPosition().add(-2, 0, 7),
				prevContainerBlock, containerBlock);

		builder.replace(boxContainer.getWorld(), boxContainer.getPosition()
				.add(2, 4, 1), boxContainer.getPosition().add(-2, 4, 7),
				prevContainerBlock, containerBlock);
	}

	/* 
	 * ==================================================================================================================================================================================================================
	 * StructureBuilder
	 * ==================================================================================================================================================================================================================
	 */

	public class StructureBuilder {

		public void fill(World world, BlockPos start, BlockPos end,
				Block material) {
			int startX = start.getX();
			int endX = end.getX();
			int startY = start.getY();
			int endY = end.getY();
			int startZ = start.getZ();
			int endZ = end.getZ();

			int[] intSwitchArray = new int[2];

			if (endX < startX) {
				intSwitchArray = switchNumbers(startX, endX);
				startX = intSwitchArray[0];
				endX = intSwitchArray[1];
			}

			if (endY < startY) {
				intSwitchArray = switchNumbers(startY, endY);
				startY = intSwitchArray[0];
				endY = intSwitchArray[1];
			}

			if (endZ < startZ) {
				intSwitchArray = switchNumbers(startZ, endZ);
				startZ = intSwitchArray[0];
				endZ = intSwitchArray[1];
			}

			for (int x = startX; x < endX + 1; x++) {
				for (int y = startY; y < endY + 1; y++) {
					for (int z = startZ; z < endZ + 1; z++) {
						world.setBlockState(new BlockPos(x, y, z),
								material.getDefaultState());
					}
				}
			}
		}

		public void room(World world, BlockPos start, BlockPos end,
				Block material) {
			fill(world, start, end, material);

			int airStartX = -(start.getX() - end.getX())
					/ Math.abs(start.getX() - end.getX());
			int airEndX = -(end.getX() - start.getX())
					/ Math.abs(end.getX() - start.getX());
			int airStartY = -(start.getY() - end.getY())
					/ Math.abs(start.getY() - end.getY());
			int airEndY = -(end.getY() - start.getY())
					/ Math.abs(end.getY() - start.getY());
			int airStartZ = -(start.getZ() - end.getZ())
					/ Math.abs(start.getZ() - end.getZ());
			int airEndZ = -(end.getZ() - start.getZ())
					/ Math.abs(end.getZ() - start.getZ());

			fill(world, start.add(airStartX, airStartY, airStartZ),
					end.add(airEndX, airEndY, airEndZ), Blocks.air);
		}

		public void container(World world, BlockPos start, Block material,
				String containerName, String containerImage,
				String containerID) {

			start = start.add(-2, 0, 1);
			room(world, start, start.add(4, 4, 4), Blocks.iron_block);

			world.setBlockState(start.add(2, 3, 0),
					Moby.docker_block.getDefaultState());
			fill(world, start.add(2, 2, 0), start.add(2, 1, 0), Blocks.air);

			world.setBlockState(start.add(2, 2, 3),
					Blocks.stone_button.getDefaultState());
			IBlockState wallSign = Blocks.wall_sign.getDefaultState();
			world.setBlockState(start.add(2, 3, 3), wallSign);
			TileEntitySign sign = ((TileEntitySign) world.getTileEntity(start
					.add(2, 3, 3)));
			sign.signText[1] = new ChatComponentText(EnumChatFormatting.GREEN
					+ "" + EnumChatFormatting.BOLD + "Start"
					+ EnumChatFormatting.BLACK + "" + EnumChatFormatting.BOLD
					+ " / " + EnumChatFormatting.RED + ""
					+ EnumChatFormatting.BOLD + "Stop");
			sign.signText[2] = new ChatComponentText(EnumChatFormatting.BLACK
					+ "" + EnumChatFormatting.BOLD + "Container");

			world.setBlockState(start.add(4, 1, -1), wallSign);
			sign = ((TileEntitySign) world.getTileEntity(start.add(4, 1, -1)));
			sign.signText[0] = new ChatComponentText(EnumChatFormatting.BOLD
					+ "Name:");
			wrapSignText(containerName, sign);

			world.setBlockState(start.add(3, 1, -1), wallSign);
			sign = ((TileEntitySign) world.getTileEntity(start.add(3, 1, -1)));
			sign.signText[0] = new ChatComponentText(EnumChatFormatting.BOLD
					+ "Image:");
			wrapSignText(containerImage, sign);

			room(world, start.add(0, 0, 6), start.add(4, 4, 4),
					Blocks.iron_block);
			world.setBlockState(start.add(2, 2, 5),
					Blocks.command_block.getDefaultState());
			TileEntityCommandBlock commandBlock = (TileEntityCommandBlock) world
					.getTileEntity(start.add(2, 2, 5));
			commandBlock.getCommandBlockLogic().setCommand(
					"/docker switch_state " + containerID);

			IBlockState glowstone = Blocks.glowstone.getDefaultState();
			Vec3i[] addVectors = { new Vec3i(3, 0, 1), new Vec3i(1, 0, 1),
					new Vec3i(3, 0, 3), new Vec3i(1, 0, 3), new Vec3i(3, 0, 5),
					new Vec3i(1, 0, 5), new Vec3i(3, 4, 1), new Vec3i(1, 4, 1),
					new Vec3i(3, 4, 3), new Vec3i(1, 4, 3), new Vec3i(3, 4, 5),
					new Vec3i(1, 4, 5) };
			for (Vec3i vector : addVectors) {
				world.setBlockState(start.add(vector), glowstone);
			}
		}

		private void wrapSignText(String containerProperty, TileEntitySign sign) {
			if (containerProperty.length() < 14) {
				sign.signText[1] = new ChatComponentText(containerProperty);
			} else if (containerProperty.length() < 27) {
				sign.signText[1] = new ChatComponentText(
						containerProperty.substring(0, 13));
				sign.signText[2] = new ChatComponentText(
						containerProperty.substring(13,
								containerProperty.length()));
			} else {
				sign.signText[1] = new ChatComponentText(
						containerProperty.substring(0, 13));
				sign.signText[1] = new ChatComponentText(
						containerProperty.substring(13, 26));
				sign.signText[2] = new ChatComponentText(
						containerProperty.substring(26,
								containerProperty.length()));
			}
		}

		public int[] switchNumbers(int num1, int num2) {
			int[] ints = { num2, num1 };
			return ints;
		}

		public List<BoxContainer> containerColumn(List<Container> containers,
				int index, BlockPos pos, World world) {

			List<BoxContainer> boxContainers = new ArrayList<BoxContainer>();

			int endIndex = 10;

			if (containers.size() - (index * 10) < 10) {
				endIndex = containers.size() - index * 10;
			}

			for (int i = index * 10; i < (index * 10) + endIndex; i++) {
				Container container = containers.get(i);
				boxContainers.add(new BoxContainer(pos,
						container.getId(), container.getNames()[0],
						container.getImage(), world));
				pos = pos.add(0, 6, 0);
			}

			return boxContainers;
		}

		public List<BoxContainer> containerPanel(List<Container> containers,
				BlockPos pos, World world) {
			List<BoxContainer> boxContainers = new ArrayList<BoxContainer>();

			int lastIndex = (containers.size() - (containers.size() % 10)) / 10;
			for (int i = 0; i <= lastIndex; i++) {
				boxContainers
						.addAll(containerColumn(containers, i, pos, world));
				pos = pos.add(6, 0, 0);
			}

			return boxContainers;
		}

		public void replace(World world, BlockPos start, BlockPos end,
				Block blockToReplace, Block blockToReplaceWith) {
			int startX = start.getX();
			int endX = end.getX();
			int startY = start.getY();
			int endY = end.getY();
			int startZ = start.getZ();
			int endZ = end.getZ();

			int[] intSwitchArray = new int[2];

			if (endX < startX) {
				intSwitchArray = switchNumbers(startX, endX);
				startX = intSwitchArray[0];
				endX = intSwitchArray[1];
			}

			if (endY < startY) {
				intSwitchArray = switchNumbers(startY, endY);
				startY = intSwitchArray[0];
				endY = intSwitchArray[1];
			}

			if (endZ < startZ) {
				intSwitchArray = switchNumbers(startZ, endZ);
				startZ = intSwitchArray[0];
				endZ = intSwitchArray[1];
			}

			for (int x = startX; x < endX + 1; x++) {
				for (int y = startY; y < endY + 1; y++) {
					for (int z = startZ; z < endZ + 1; z++) {
						if (world.getBlockState(new BlockPos(x, y, z)) == blockToReplace
								.getDefaultState()) {
							world.setBlockState(new BlockPos(x, y, z),
									blockToReplaceWith.getDefaultState());

						}
					}
				}
			}
		}

		public void airContainer(World world, BlockPos start) {
			start = start.add(-2, 0, 0);
			fill(world, start, start.add(4, 4, 7), Blocks.air);
		}
	}
}