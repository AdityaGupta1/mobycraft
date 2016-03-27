package org.redfrog404.mobycraft.commands;

import static org.redfrog404.mobycraft.commands.ContainerListCommands.getAllContainers;
import static org.redfrog404.mobycraft.commands.ContainerListCommands.getContainerFromAllContainersWithName;
import static org.redfrog404.mobycraft.commands.ContainerListCommands.getContainerWithName;
import static org.redfrog404.mobycraft.commands.ContainerListCommands.getContainers;
import static org.redfrog404.mobycraft.commands.ContainerListCommands.getStoppedContainers;
import static org.redfrog404.mobycraft.commands.DockerCommands.arg1;
import static org.redfrog404.mobycraft.commands.DockerCommands.args;
import static org.redfrog404.mobycraft.commands.DockerCommands.checkIfArgIsNull;
import static org.redfrog404.mobycraft.commands.DockerCommands.getDockerClient;
import static org.redfrog404.mobycraft.commands.DockerCommands.sendConfirmMessage;
import static org.redfrog404.mobycraft.commands.DockerCommands.sendErrorMessage;
import static org.redfrog404.mobycraft.commands.DockerCommands.sendFeedbackMessage;
import static org.redfrog404.mobycraft.commands.ImageCommands.getImageWithName;

import java.util.ArrayList;

import org.apache.commons.lang.math.NumberUtils;

import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Container;

public class BasicContainerCommands {
	
	public static void startContainer() {
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
	
	public static void stopContainer() {
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
	
	public static void remove() {
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
	
	public static void removeAll() {
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
	
	public static void restart() {
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
	
	public static void kill() {
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
	
	public static void killAll() {
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
	
	public static void runContainer() {
		sendFeedbackMessage("Working on it...");
		
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
	
	public static void removeStoppedContainers() {
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
}
