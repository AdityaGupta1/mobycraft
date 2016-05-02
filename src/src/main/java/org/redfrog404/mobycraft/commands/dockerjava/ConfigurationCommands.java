package org.redfrog404.mobycraft.commands.dockerjava;

import static org.redfrog404.mobycraft.commands.common.MainCommand.arg1;
import static org.redfrog404.mobycraft.commands.common.MainCommand.args;
import static org.redfrog404.mobycraft.commands.common.MainCommand.sender;
import static org.redfrog404.mobycraft.utils.MessageSender.sendConfirmMessage;
import static org.redfrog404.mobycraft.utils.MessageSender.sendErrorMessage;
import static org.redfrog404.mobycraft.utils.MessageSender.sendFeedbackMessage;
import static org.redfrog404.mobycraft.utils.MessageSender.sendMessage;

import org.apache.commons.lang.math.NumberUtils;
import org.redfrog404.mobycraft.api.MobycraftBuildContainerCommands;
import org.redfrog404.mobycraft.api.MobycraftConfigurationCommands;
import org.redfrog404.mobycraft.commands.common.ConfigProperties;
import org.redfrog404.mobycraft.commands.common.MainCommand;
import org.redfrog404.mobycraft.main.Mobycraft;
import org.redfrog404.mobycraft.utils.Utils;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.config.Property;

import javax.inject.Inject;

public class ConfigurationCommands implements MobycraftConfigurationCommands {
	
	private ConfigProperties configProperties;
	private final MobycraftBuildContainerCommands buildCommands;

	@Inject
	public ConfigurationCommands(MobycraftBuildContainerCommands buildCommands) {
		configProperties = new ConfigProperties();
		this.buildCommands = buildCommands;
	}
	
	public void setPath() {
		if (Utils.checkIfArgIsNull(args, 0)) {
			sendErrorMessage("Docker path is not specified! Command is used as /docker path <path> .");
			return;
		}

		configProperties.getCertPathProperty().setValue(arg1);
		Mobycraft.config.save();
		sendConfirmMessage("Docker path set to \"" + arg1 + "\"");
	}

	public void setHost() {
		if (Utils.checkIfArgIsNull(args, 0)) {
			sendErrorMessage("Docker host is not specified! Command is used as /docker host <host> .");
			return;
		}

		if (!arg1.contains(":")) {
			arg1 = arg1 + ":2376";
		}

		configProperties.getDockerHostProperty().setValue(arg1);
		Mobycraft.config.save();
		sendConfirmMessage("Docker host set to \"" + arg1 + "\"");
	}

	public void setPollRate() {
		if (Utils.checkIfArgIsNull(args, 0)) {
			sendErrorMessage("Poll rate is not specified! Command is used as /docker poll_rate <rate> .");
			return;
		}
		
		if (!NumberUtils.isNumber(arg1)) {
			sendErrorMessage("The argument \"" + arg1 + "\" is invalid (must be a positive integer)! Command is used as /docker poll_rate <rate> .");
			return;
		}

		configProperties.getPollRateProperty().setValue(arg1);
		Mobycraft.config.save();
		int intArg1 = Integer.parseInt(arg1);
		if (intArg1 < 1) {
			if (intArg1 == 0) {
				sendErrorMessage("The argument \"" + arg1 + "\" is invalid (can't be zero)! Command is used as /docker poll_rate <rate> .");
			} else {
				sendErrorMessage("The argument \"" + arg1 + "\" is invalid (can't be negative)! Command is used as /docker poll_rate <rate> .");
			}
		} else if (intArg1 == 1) {
			sendConfirmMessage("Poll rate set to 1 second");
		} else {
			sendConfirmMessage("Poll rate set to " + arg1 + " seconds");
		}
	}
	
	public void setStartPos() {
		BlockPos position = sender.getPosition();
		configProperties.getStartPosProperty().setValue((int) Math.floor(position.getX()) + ", "
				+ (int) Math.floor(position.getY()) + ", " + (int) Math.floor(position.getZ()));
		Mobycraft.config.save();
		sendConfirmMessage("Set start position for building containers to ("
				+ getConfigProperties().getStartPosProperty().getString() + ").");
		buildCommands.updateContainers(false);
	}
	
	public BlockPos getStartPos() {
		String[] posStrings = configProperties.getStartPosProperty().getString().split(", ");
		return new BlockPos(Integer.parseInt(posStrings[0]),
				Integer.parseInt(posStrings[1]),
				Integer.parseInt(posStrings[2]));
	}

	public void getHostAndPath() {
//		if (configProperties.getDockerHostProperty() == null || configProperties.getCertPathProperty() == null) {
//			refreshHostAndPath();
//		}

		sendMessage(EnumChatFormatting.BLUE + "" + EnumChatFormatting.BOLD
				+ "Docker host: " + EnumChatFormatting.RESET + getConfigProperties().getDockerHostProperty());
		sendMessage(EnumChatFormatting.GOLD + "" + EnumChatFormatting.BOLD
				+ "Docker cert path: " + EnumChatFormatting.RESET + getConfigProperties().getCertPathProperty());
	}
	
	/**
	 * Refreshes the Docker host and cert path
	 */
	private String getDefaultHost() {
		String dockerHost = System.getProperty("DOCKER_HOST");
		
		if (dockerHost == null) {
			sendFeedbackMessage("The DOCKER_HOST environment variable has not been set");

			if (configProperties.getDockerHostProperty() == null || configProperties.getDockerHostProperty().isDefault()) {
				dockerHost = "192.168.99.100:2376";
				sendConfirmMessage("Using default value of \"" + dockerHost
						+ "\"");
				sendConfirmMessage("Use /docker host <host> to override this value");
			} else {
				dockerHost = configProperties.getDockerHostProperty().getString();
				sendConfirmMessage("Using config value of \""
						+ configProperties.getDockerHostProperty().getString() + "\"");
			}
		} else {
			dockerHost = dockerHost.substring(6, dockerHost.length());
			sendConfirmMessage("Using DOCKER_HOST value of \"" + dockerHost
					+ "\"");
		}
		dockerHost = "https://" + dockerHost;
		
		return dockerHost;
	}
	
	private String getDefaultPath() {

		String certPath = System.getProperty("DOCKER_CERT_PATH");
		if (certPath == null) {
			sendFeedbackMessage("The DOCKER_CERT_PATH environment variable has not been set");

			if (configProperties.getCertPathProperty() == null || configProperties.getCertPathProperty().isDefault()) {
				certPath = System.getProperty("user.home")
						+ "/.docker/machine/machines/default";
				sendConfirmMessage("Using default value of \"" + certPath
						+ "\"");
				sendConfirmMessage("Use /docker path <path> to override this value");
			} else {
				certPath = configProperties.getCertPathProperty().getString();
				sendConfirmMessage("Using config value of \"" + certPath + "\"");
			}
		} else {
			sendConfirmMessage("Using DOCKER_CERT_PATH value of \"" + certPath
					+ "\"");
		}
		
		return certPath;
	}	

	@Override
	public ConfigProperties getConfigProperties() {
		Property property = Mobycraft.config.get("files", "docker-host", "Docker host IP",
				"The IP of your Docker host (set using /docker host <host>); only used if DOCKER_HOST environment variable is not set");
		if (property.isDefault()) {
			property.setValue(getDefaultHost());
		}
		configProperties.setDockerHostProperty(property);

		property = Mobycraft.config.get("files", "docker-cert-path", "File path",
				"The directory path of your Docker certificate (set using /docker path <path>); only used if DOCKER_CERT_PATH environment variable is not set");
		if (property.isDefault()) {
			property.setValue(getDefaultPath());
		}
		configProperties.setCertPathProperty(property);

		configProperties.setStartPosProperty(Mobycraft.config.get("container-building", "start-pos", "0, 0, 0",
				"The position - x, y, z - to start building containers at (set using /docker start_pos"));

		configProperties.setPollRateProperty(Mobycraft.config.get("container-building", "poll-rate", "2",
				"The rate in seconds at which the containers will update (set using /docker poll_rate <rate in seconds>)"));

		MainCommand.maxCount = (int) Math.floor((Float.parseFloat(configProperties.getPollRateProperty().getString()) * 50));

		return configProperties;
	}	
}
