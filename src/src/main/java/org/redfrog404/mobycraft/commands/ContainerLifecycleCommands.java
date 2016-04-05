package org.redfrog404.mobycraft.commands;

import static org.redfrog404.mobycraft.commands.ContainerListCommands.getAll;
import static org.redfrog404.mobycraft.commands.ContainerListCommands.getContainers;
import static org.redfrog404.mobycraft.commands.ContainerListCommands.getFromAllWithName;
import static org.redfrog404.mobycraft.commands.ContainerListCommands.getStopped;
import static org.redfrog404.mobycraft.commands.ContainerListCommands.getWithName;
import static org.redfrog404.mobycraft.commands.ImageCommands.getImageWithName;
import static org.redfrog404.mobycraft.commands.MainCommand.arg1;
import static org.redfrog404.mobycraft.commands.MainCommand.args;
import static org.redfrog404.mobycraft.commands.MainCommand.checkIfArgIsNull;
import static org.redfrog404.mobycraft.commands.MainCommand.getDockerClient;
import static org.redfrog404.mobycraft.utils.MessageSender.sendConfirmMessage;
import static org.redfrog404.mobycraft.utils.MessageSender.sendErrorMessage;
import static org.redfrog404.mobycraft.utils.MessageSender.sendFeedbackMessage;

import java.util.ArrayList;

import org.apache.commons.lang.math.NumberUtils;

import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.command.PullImageResultCallback;

public class ContainerLifecycleCommands {
	
	public static void start() {
		if (checkIfArgIsNull(0)) {
			sendErrorMessage("Container name not specified! Command is used as /docker start <name> .");
			return;
		}

		try {
			getDockerClient().startContainerCmd(
					getFromAllWithName("/" + arg1).getId())
					.exec();
			sendConfirmMessage("Started container with name \"/" + arg1 + "\"");
		} catch (NullPointerException exception) {
			sendErrorMessage("No container exists with the name \"/" + arg1
					+ "\"");
		}
	}
	
	public static void stop() {
		if (checkIfArgIsNull(0)) {
			sendErrorMessage("Container name not specified! Command is used as /docker stop <name> .");
			return;
		}

		try {
			getDockerClient().stopContainerCmd(
					getWithName("/" + arg1).getId()).exec();
			sendConfirmMessage("Stopped container with name \"/" + arg1 + "\"");
		} catch (NullPointerException exception) {
			sendErrorMessage("No container exists with the name \"/" + arg1
					+ "\"");
		}
	}
	
	public static void remove() {
		if (checkIfArgIsNull(0)) {
			sendErrorMessage("Container name not specified! Command is used as /docker rm <name> .");
			return;
		}

		try {
			getDockerClient()
					.removeContainerCmd(
							getFromAllWithName("/" + arg1)
									.getId()).withForce().exec();
			sendConfirmMessage("Removed container with name \"/" + arg1 + "\"");
		} catch (NullPointerException exception) {
			sendErrorMessage("No container exists with the name \"/" + arg1
					+ "\"");
		}
	}
	
	public static void removeAll() {
		sendFeedbackMessage("Working on it...");
		if (getAll().size() < 1) {
			sendFeedbackMessage("No containers currently existing.");
			return;
		}
		for (Container container : getAll()) {
			getDockerClient().removeContainerCmd(container.getId()).withForce()
					.exec();
		}
		sendConfirmMessage("Removed all containers.");
	}
	
	public static void restart() {
		if (checkIfArgIsNull(0)) {
			sendErrorMessage("Container name not specified! Command is used as /docker restart <name> .");
			return;
		}

		try {
			getDockerClient().restartContainerCmd(
					getWithName("/" + arg1).getId()).exec();
			sendConfirmMessage("Restarted container with name \"/" + arg1 + "\"");
		} catch (NullPointerException exception) {
			sendErrorMessage("No container exists with the name \"/" + arg1
					+ "\"");
		}
	}
	
	public static void kill() {
		if (checkIfArgIsNull(0)) {
			sendErrorMessage("Container name not specified! Command is used as /docker kill <name> .");
			return;
		}

		try {
			getDockerClient().killContainerCmd(
					getWithName("/" + arg1).getId()).exec();
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
	
	public static void run() throws InterruptedException {
		sendFeedbackMessage("Working on it...");
		
		if (getImageWithName(arg1) == null) {
			PullImageResultCallback callback = new PullImageResultCallback();
			getDockerClient().pullImageCmd(arg1).withTag("latest").exec(callback);
			callback.awaitCompletion();
		}

		if (args.length < 2) {
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
		} else if (!NumberUtils.isNumber(args[1])) {
			// Name
			CreateContainerResponse response = getDockerClient()
					.createContainerCmd(arg1).withName(args[1]).exec();
			getDockerClient().startContainerCmd(response.getId()).exec();
			sendConfirmMessage("Created container with image \"" + arg1
					+ "\" and name \"/" + args[1] + "\"");
		} else {
			// Number
			ArrayList<String> names = new ArrayList<String>();
			for (int i = 0; i < Integer.parseInt(args[1]); i++) {
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
	
	public static void removeStopped() {
		sendFeedbackMessage("Working on it...");
		if (getStopped().size() < 1) {
			sendFeedbackMessage("No containers currently stopped.");
			return;
		}
		for (Container container : getStopped()) {
			getDockerClient().removeContainerCmd(container.getId()).withForce()
					.exec();
		}
		sendConfirmMessage("Removed all stopped containers.");
	}
}