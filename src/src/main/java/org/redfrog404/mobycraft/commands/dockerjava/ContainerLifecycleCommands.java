package org.redfrog404.mobycraft.commands.dockerjava;

import static org.redfrog404.mobycraft.commands.dockerjava.MainCommand.arg1;
import static org.redfrog404.mobycraft.commands.dockerjava.MainCommand.args;
import static org.redfrog404.mobycraft.commands.dockerjava.MainCommand.getDockerClient;
import static org.redfrog404.mobycraft.utils.MessageSender.sendConfirmMessage;
import static org.redfrog404.mobycraft.utils.MessageSender.sendErrorMessage;
import static org.redfrog404.mobycraft.utils.MessageSender.sendFeedbackMessage;

import java.util.ArrayList;

import org.apache.commons.lang.math.NumberUtils;
import org.redfrog404.mobycraft.api.MobycraftCommandsFactory;
import org.redfrog404.mobycraft.api.MobycraftContainerLifecycleCommands;
import org.redfrog404.mobycraft.structure.BoxContainer;
import org.redfrog404.mobycraft.structure.StructureBuilder;
import org.redfrog404.mobycraft.utils.Utils;

import com.github.dockerjava.api.DockerClientException;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.command.PullImageResultCallback;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

public class ContainerLifecycleCommands implements
		MobycraftContainerLifecycleCommands {

	public void start() {
		sendFeedbackMessage("Working on it...");

		if (Utils.checkIfArgIsNull(args, 0)) {
			sendErrorMessage("Container name not specified! Command is used as /docker start <name> .");
			return;
		}

		try {
			getDockerClient().startContainerCmd(
					MobycraftCommandsFactory.getInstance().getListCommands()
							.getFromAllWithName("/" + arg1).getId()).exec();
			sendConfirmMessage("Started container with name \"" + arg1 + "\"");
		} catch (NullPointerException exception) {
			sendErrorMessage("No container exists with the name \"" + arg1
					+ "\"");
		}
	}

	public void stop() {
		sendFeedbackMessage("Working on it...");

		if (Utils.checkIfArgIsNull(args, 0)) {
			sendErrorMessage("Container name not specified! Command is used as /docker stop <name> .");
			return;
		}

		try {
			getDockerClient().stopContainerCmd(
					MobycraftCommandsFactory.getInstance().getListCommands()
							.getWithName("/" + arg1).getId()).exec();
			sendConfirmMessage("Stopped container with name \"" + arg1 + "\"");
		} catch (NullPointerException exception) {
			sendErrorMessage("No container exists with the name \"" + arg1
					+ "\"");
		}
	}

	public void remove() {
		sendFeedbackMessage("Working on it...");

		if (Utils.checkIfArgIsNull(args, 0)) {
			sendErrorMessage("Container name not specified! Command is used as /docker rm <name> .");
			return;
		}

		try {
			getDockerClient()
					.removeContainerCmd(
							MobycraftCommandsFactory.getInstance()
									.getListCommands()
									.getFromAllWithName("/" + arg1).getId())
					.withForce().exec();
			sendConfirmMessage("Removed container with name \"" + arg1 + "\"");
		} catch (NullPointerException exception) {
			sendErrorMessage("No container exists with the name \"" + arg1
					+ "\"");
		}
	}

	public void removeAll() {
		sendFeedbackMessage("Working on it...");

		if (MobycraftCommandsFactory.getInstance().getListCommands().getAll()
				.size() < 1) {
			sendFeedbackMessage("No containers currently existing.");
			return;
		}
		for (Container container : MobycraftCommandsFactory.getInstance()
				.getListCommands().getAll()) {
			getDockerClient().removeContainerCmd(container.getId()).withForce()
					.exec();
		}
		sendConfirmMessage("Removed all containers.");
	}

	public void restart() {
		sendFeedbackMessage("Working on it...");

		if (Utils.checkIfArgIsNull(args, 0)) {
			sendErrorMessage("Container name not specified! Command is used as /docker restart <name> .");
			return;
		}

		try {
			getDockerClient().restartContainerCmd(
					MobycraftCommandsFactory.getInstance().getListCommands()
							.getWithName("/" + arg1).getId()).exec();
			sendConfirmMessage("Restarted container with name \"" + arg1 + "\"");
		} catch (NullPointerException exception) {
			sendErrorMessage("No container exists with the name \"" + arg1
					+ "\"");
		}
	}

	public void kill() {
		sendFeedbackMessage("Working on it...");

		if (Utils.checkIfArgIsNull(args, 0)) {
			sendErrorMessage("Container name not specified! Command is used as /docker kill <name> .");
			return;
		}

		try {
			getDockerClient().killContainerCmd(
					MobycraftCommandsFactory.getInstance().getListCommands()
							.getWithName("/" + arg1).getId()).exec();
			sendConfirmMessage("Killed container with name \"" + arg1 + "\"");
		} catch (NullPointerException exception) {
			sendErrorMessage("No container exists with the name \"" + arg1
					+ "\"");
		}
	}

	public void killAll() {

		sendFeedbackMessage("Working on it...");

		if (MobycraftCommandsFactory.getInstance().getListCommands()
				.getStarted().size() < 1) {
			sendFeedbackMessage("No containers currently running.");
			return;
		}
		for (Container container : MobycraftCommandsFactory.getInstance()
				.getListCommands().getStarted()) {
			getDockerClient().killContainerCmd(container.getId()).exec();
		}
		sendConfirmMessage("Killed all containers.");
	}

	public void run() throws InterruptedException {
		if (args.length < 1) {
			sendErrorMessage("No arguments specified! Command is used as /docker run <image> (name | amount) .");
			return;
		}

		sendFeedbackMessage("Working on it...");

		if (MobycraftCommandsFactory.getInstance().getImageCommands()
				.getImageWithName(arg1) == null) {
			PullImageResultCallback callback = new PullImageResultCallback();
			getDockerClient().pullImageCmd(arg1).withTag("latest")
					.exec(callback);
			try {
				callback.awaitCompletion();
			} catch (DockerClientException exception) {
				sendErrorMessage("\"" + arg1
						+ "\" + is not a valid image name!");
			}
		}

		if (args.length < 2) {
			// No name, no number
			CreateContainerResponse response = getDockerClient()
					.createContainerCmd(arg1).exec();
			getDockerClient().startContainerCmd(response.getId()).exec();
			String name = "";
			for (Container container : MobycraftCommandsFactory.getInstance()
					.getListCommands().getStarted()) {
				if (container.getId().equals(response.getId())) {
					name = container.getNames()[0];
				}
			}
			sendConfirmMessage("Created container with image \"" + arg1
					+ "\" and name \"" + name + "\"");
		} else if (!NumberUtils.isNumber(args[1])) {
			// Name
			for (Container container : MobycraftCommandsFactory.getInstance()
					.getListCommands().getAll()) {
				if (args[1].equals(container.getNames()[0])) {
					sendErrorMessage("The name \""
							+ args[1]
							+ "\" is already in use! Remove or rename that container to be able to use that name.");
					return;
				}
			}
			CreateContainerResponse response = getDockerClient()
					.createContainerCmd(arg1).withName(args[1]).exec();
			getDockerClient().startContainerCmd(response.getId()).exec();
			sendConfirmMessage("Created container with image \"" + arg1
					+ "\" and name \"" + args[1] + "\"");
		} else {
			// Number
			ArrayList<String> names = new ArrayList<String>();
			for (int i = 0; i < Integer.parseInt(args[1]); i++) {
				CreateContainerResponse response = getDockerClient()
						.createContainerCmd(arg1).exec();
				getDockerClient().startContainerCmd(response.getId()).exec();
				String name = "";
				for (Container container : MobycraftCommandsFactory
						.getInstance().getListCommands().getStarted()) {
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

	public void removeStopped() {
		sendFeedbackMessage("Working on it...");
		if (MobycraftCommandsFactory.getInstance().getListCommands()
				.getStopped().size() < 1) {
			sendFeedbackMessage("No containers currently stopped.");
			return;
		}
		for (Container container : MobycraftCommandsFactory.getInstance()
				.getListCommands().getStopped()) {
			getDockerClient().removeContainerCmd(container.getId()).withForce()
					.exec();
		}
		sendConfirmMessage("Removed all stopped containers.");
	}

	public void switchState(StructureBuilder builder, String containerID) {
		MobycraftCommandsFactory.getInstance().getListCommands()
				.refreshContainerIDMap();

		// If there is no container with the ID, return
		if (MobycraftCommandsFactory.getInstance().getListCommands()
				.getBoxContainerWithID(containerID) == null) {
			return;
		}

		// New BoxContainer variable called boxContainer to store the container
		BoxContainer boxContainer = MobycraftCommandsFactory.getInstance()
				.getListCommands().getBoxContainerWithID(containerID);

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
}