package org.redfrog404.mobycraft.commands.dockerjava;

import static org.redfrog404.mobycraft.commands.dockerjava.BasicDockerCommands.specificHelpMessages;
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

import org.apache.http.conn.UnsupportedSchemeException;
import org.redfrog404.mobycraft.api.MobycraftBasicCommands;
import org.redfrog404.mobycraft.api.MobycraftBuildContainerCommands;
import org.redfrog404.mobycraft.api.MobycraftCommandsFactory;
import org.redfrog404.mobycraft.main.Mobycraft;
import org.redfrog404.mobycraft.structure.BoxContainer;
import org.redfrog404.mobycraft.structure.StructureBuilder;
import org.redfrog404.mobycraft.utils.Utils;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;

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

public class MainCommand implements ICommand {
	public static List commandAliases;

	// Maps commands to numbers for use in the switch statement in
	// processCommand()
	static Map<String, Integer> commandNumbers = new HashMap<String, Integer>();
	// Help messages that are shown when /docker help is used
	static Map<String, String> helpMessages = new HashMap<String, String>();
	// Current box containers
	public static List<BoxContainer> boxContainers = new ArrayList<BoxContainer>();
	// Containers container's IDs
	static Map<String, BoxContainer> containerIDMap = new HashMap<String, BoxContainer>();

	public static ICommandSender sender;

	static String[] args;
	public static String arg1;

	static StructureBuilder builder = new StructureBuilder();

	public int count = 0;
	public static int maxCount;

	// static String dockerHost;
	// static String certPath;

	// ConfigProperties configProperties;

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
		commandNumbers.put("poll_rate", 22);

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
						"Shows a list of the top 5 containers that use the most <cpu> or <memory>");
		helpMessages.put("get_host_and_path | get_path_and_host",
				"Returns the Docker host and cert path");
		helpMessages.put("poll_rate <rate>",
				"Sets the poll rate to <rate> seconds");

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

		MobycraftCommandsFactory factory = MobycraftCommandsFactory
				.getInstance();

		try {
			switch (commandNumber) {
			case 0:
				factory.getBasicCommands().help();
				break;
			case 1:
				factory.getBasicCommands().ps();
				break;
			case 2:
				factory.getConfigurationCommands().setPath();
				break;
			case 3:
				factory.getLifecycleCommands().switchState(builder, arg1);
				break;
			case 4:
				factory.getLifecycleCommands().removeStopped();
				break;
			case 5:
				factory.getLifecycleCommands().run();
				break;
			case 6:
				factory.getLifecycleCommands().killAll();
				break;
			case 7:
				factory.getLifecycleCommands().kill();
				break;
			case 8:
				factory.getLifecycleCommands().restart();
				break;
			case 9:
				factory.getLifecycleCommands().remove();
				break;
			case 10:
				factory.getLifecycleCommands().removeAll();
				break;
			case 11:
				factory.getLifecycleCommands().stop();
				break;
			case 12:
				factory.getLifecycleCommands().start();
				break;
			case 13:
				factory.getImageCommands().images();
				break;
			case 14:
				factory.getImageCommands().removeImage();
				break;
			case 15:
				factory.getImageCommands().removeAllImages();
				break;
			case 16:
				factory.getConfigurationCommands().setStartPos();
				break;
			case 17:
				factory.getBasicCommands().showDetailedInfo();
				break;
			case 18:
				factory.getBuildCommands().teleport();
				break;
			case 19:
				factory.getListCommands().heatMap();
				break;
			case 20:
				factory.getConfigurationCommands().setHost();
				break;
			case 21:
				factory.getConfigurationCommands().getHostAndPath();
				break;
			case 22:
				factory.getConfigurationCommands().setPollRate();
			}
		} catch (Exception exception) {
			if (exception instanceof UnsupportedSchemeException) {
				sendErrorMessage("Invalid Docker host/path! Set the host by using /docker host <host> ; set the path by using /docker path <path>");
			}
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
		return Utils.asSortedList(commandNumbers.keySet());
	}

	public static void sendHelpMessage(String command, String helpMessage) {
		sendMessage(EnumChatFormatting.DARK_GREEN + "/docker " + command
				+ " - " + EnumChatFormatting.GOLD + helpMessage);
	}

	public static DockerClient getDockerClient() {
		ConfigProperties configProperties = MobycraftCommandsFactory
				.getInstance().getConfigurationCommands().getConfigProperties();

		DockerClientConfig dockerConfig = DockerClientConfig
				.createDefaultConfigBuilder()
				.withUri(configProperties.getDockerHostProperty().getString())
				.withDockerCertPath(
						configProperties.getCertPathProperty().getString())
				.build();

		return DockerClientBuilder.getInstance(dockerConfig).build();
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
		MobycraftBuildContainerCommands buildCommands = MobycraftCommandsFactory
				.getInstance().getBuildCommands();

		if (event.player.getEntityWorld().isRemote) {
			return;
		}

		count++;
		if (count >= maxCount) {
			sender = event.player;
			if (boxContainers == null) {
				buildCommands.refreshAndBuildContainers();
			} else {
				buildCommands.updateContainers(true);
			}
			count = 0;
		}
	}

	@SubscribeEvent
	public void containerWand(PlayerInteractEvent event) {
		MobycraftCommandsFactory factory = MobycraftCommandsFactory
				.getInstance();

		EntityPlayer player = event.entityPlayer;

		if (!event.action.equals(Action.RIGHT_CLICK_BLOCK)
				&& !event.action.equals(Action.LEFT_CLICK_BLOCK)) {
			return;
		}

		if (player.getHeldItem() == null
				|| player.getHeldItem().getItem() != Mobycraft.container_wand) {
			return;
		}

		sender = player;
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

		if (factory.getListCommands().getWithName(name) == null) {
			return;
		}

		Container container = factory.getListCommands().getWithName(name);
		getDockerClient().removeContainerCmd(container.getId()).withForce()
				.exec();
		sendConfirmMessage("Removed container with name \"" + name + "\"");

		factory.getBuildCommands().updateContainers(false);
	}

}
