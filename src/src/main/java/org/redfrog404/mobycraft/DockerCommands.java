package org.redfrog404.mobycraft;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
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
	private List aliases;

	Map<String, Integer> argNumbers = new HashMap<String, Integer>();

	Map<String, String> helpMessages = new HashMap<String, String>();

	ICommandSender sender;

	Property dockerPath = Moby.config
			.get("files", "docker-cert-path", "File path",
					"The directory path of your Docker certificate (set using /docker path <path>)");
	Property startPos = Moby.config
			.get("container-building",
					"start-pos",
					"0, 0, 0",
					"The position - x, y, z - to start building contianers at (set using /docker start_pos");
	Property pollRate = Moby.config
			.get("container-building",
					"poll-rate",
					"2",
					"The rate in seconds at which the containers will update (set using /docker poll_rate <rate in seconds>)");

	String[] args;
	String arg1;

	static List<BoxContainer> boxContainers = new ArrayList<BoxContainer>();
	static Map<String, BoxContainer> containerIDMap = new HashMap<String, BoxContainer>();
	static Map<Integer, String> suffixNumbers = new HashMap<Integer, String>();

	public DockerCommands() {
		this.aliases = new ArrayList();
		this.aliases.add("docker");

		argNumbers.put("help", 0);
		argNumbers.put("ps", 1);
		argNumbers.put("path", 2);
		argNumbers.put("switch_state", 3);
		// TODO Remove after adding automatic building when player joins game
		argNumbers.put("build_containers", 4);
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
		// TODO Remove after adding automatic container updating
		argNumbers.put("update_containers", 16);
		argNumbers.put("set_start_pos", 17);

		helpMessages.put("help", "Brings up this help page");
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

		suffixNumbers.put(0, "B");
		suffixNumbers.put(1, "KB");
		suffixNumbers.put(2, "MB");
		suffixNumbers.put(3, "GB");
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
				switchState(arg1, true);
				break;
			case 4:
				refreshAndBuildContainers();
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
				updateContainers();
				break;
			case 17:
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
		sendMessage(EnumChatFormatting.AQUA
				+ "-- <arg> is required, (arg) is optional");
		sendMessage(EnumChatFormatting.AQUA
				+ "-- \"|\" means \"or\"; for example, \"<name | amount>\" means you can either put the name or the amount");
		for (String key : asSortedList(helpMessages.keySet())) {
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
		boxContainers = Moby.builder.containerPanel(containers,
				getStartPosition(), sender.getEntityWorld());
		List<String> stoppedContainerIDs = new ArrayList<String>();
		for (Container container : getStoppedContainers()) {
			stoppedContainerIDs.add(container.getId().substring(0, 12));
		}
		containerIDMap.clear();
		for (BoxContainer boxContainer : boxContainers) {
			containerIDMap.put(boxContainer.getShortID(), boxContainer);
			if (stoppedContainerIDs.contains(boxContainer.getShortID())) {
				boxContainer.setState(!boxContainer.getState());
			}
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
		DockerClient dockerClient = getDockerClient();

		for (BoxContainer boxContainer : boxContainers) {
			Moby.builder.container(sender.getEntityWorld(),
					boxContainer.getPosition(), Blocks.iron_block,
					boxContainer.getName(), boxContainer.getImage(),
					boxContainer.getShortID());
			if (!boxContainer.getState()) {
				switchState(boxContainer.getShortID(), false);
			}
		}
	}

	public void buildContainersFromList(List<BoxContainer> containers) {
		for (BoxContainer boxContainer : containers) {
			Moby.builder.container(sender.getEntityWorld(),
					boxContainer.getPosition(), Blocks.iron_block,
					boxContainer.getName(), boxContainer.getImage(),
					boxContainer.getShortID());
			if (!boxContainer.getState()) {
				switchState(boxContainer.getShortID(), false);
			}
		}
	}

	public void refreshAndBuildContainers() {
		sendFeedbackMessage("Getting containers...");
		refreshContainers();
		if (boxContainers.size() < 1) {
			sendFeedbackMessage("No containers currently running.");
			return;
		}
		sendFeedbackMessage("Building containers...");
		buildContainers();
		sendFeedbackMessage("Done!");
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

	public BoxContainer getContainerWithID(String id) {
		if (!containerIDMap.containsKey(id)) {
			return null;
		}
		return containerIDMap.get(id);
	}

	private void switchState(String containerID, boolean switchActualState) {
		if (getContainerWithID(containerID) == null) {
			return;
		}

		BoxContainer container = getContainerWithID(containerID);

		if (switchActualState) {
			container.setState(!container.getState());
		}

		Block containerBlock;
		Block prevContainerBlock;

		if (container.getState()) {
			containerBlock = Blocks.iron_block;
			prevContainerBlock = Blocks.redstone_block;
			if (switchActualState) {
				getDockerClient().startContainerCmd(container.getID()).exec();
			}
		} else {
			containerBlock = Blocks.redstone_block;
			prevContainerBlock = Blocks.iron_block;
			if (switchActualState) {
				getDockerClient().stopContainerCmd(container.getID()).exec();
			}
		}

		Moby.builder.replace(container.getWorld(),
				container.getPosition().add(2, 0, 1), container.getPosition()
						.add(-2, 0, 7), prevContainerBlock, containerBlock);

		Moby.builder.replace(container.getWorld(),
				container.getPosition().add(2, 4, 1), container.getPosition()
						.add(-2, 4, 7), prevContainerBlock, containerBlock);
	}

	private void runContainer() {
		if (getImageWithName(arg1).equals(null)) {
			sendErrorMessage("The requested image is not pulled yet! Please pull the image and run this command again. NOTE: This is a bug and will be fixed.");
			// getDockerClient().pullImageCmd(arg1).exec(null);
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
		if (args[argNumber - 1].equals(null)) {
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
		sendConfirmMessage("Succesfully updated containers.");
		List<Container> containers = getAllContainers();
		List<BoxContainer> newContainers = Moby.builder.containerPanel(
				containers, getStartPosition(), sender.getEntityWorld());
		if (boxContainers.equals(newContainers)) {
			return;
		}

		int start = 0;

		System.out.println(boxContainers);
		System.out.println(newContainers);

		findDifferences: for (; start < boxContainers.size(); start++) {
			if (!boxContainers.get(start).equals(newContainers.get(start))) {
				break findDifferences;
			}
		}

		start -= start % 10;

		List<BoxContainer> containersToReplace = new ArrayList<BoxContainer>();
		containersToReplace = boxContainers
				.subList(start, boxContainers.size());

		for (int i = 0; i < containersToReplace.size(); i++) {
			Moby.builder.airContainer(sender.getEntityWorld(),
					containersToReplace.get(i).getPosition());
		}

		List<BoxContainer> newContainersToBuild = new ArrayList<BoxContainer>();
		newContainersToBuild = Moby.builder.containerPanel(getContainers(),
				getStartPosition(), sender.getEntityWorld()).subList(start,
				newContainers.size());
		buildContainersFromList(newContainersToBuild);
	}

	public BlockPos getStartPosition() {
		String[] posStrings = startPos.getString().split(", ");
		return new BlockPos(Integer.parseInt(posStrings[0]),
				Integer.parseInt(posStrings[1]),
				Integer.parseInt(posStrings[2]));
	}
}