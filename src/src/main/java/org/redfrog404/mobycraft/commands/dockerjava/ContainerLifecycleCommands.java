package org.redfrog404.mobycraft.commands.dockerjava;

import static org.redfrog404.mobycraft.commands.common.MainCommand.arg1;
import static org.redfrog404.mobycraft.commands.common.MainCommand.args;
import static org.redfrog404.mobycraft.utils.MessageSender.sendConfirmMessage;
import static org.redfrog404.mobycraft.utils.MessageSender.sendErrorMessage;
import static org.redfrog404.mobycraft.utils.MessageSender.sendFeedbackMessage;

import java.util.ArrayList;

import com.github.dockerjava.api.DockerClient;
import org.apache.commons.lang.math.NumberUtils;
import org.redfrog404.mobycraft.api.MobycraftContainerLifecycleCommands;
import org.redfrog404.mobycraft.api.MobycraftContainerListCommands;
import org.redfrog404.mobycraft.api.MobycraftImageCommands;
import org.redfrog404.mobycraft.structure.BoxContainer;
import org.redfrog404.mobycraft.structure.StructureBuilder;
import org.redfrog404.mobycraft.utils.Utils;

import com.github.dockerjava.api.DockerClientException;
import com.github.dockerjava.api.command.CreateContainerResponse;
import org.redfrog404.mobycraft.model.Container;
import com.github.dockerjava.core.command.PullImageResultCallback;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

import javax.inject.Inject;

public class ContainerLifecycleCommands implements MobycraftContainerLifecycleCommands {

	private final MobycraftContainerListCommands listCommands;
	private final MobycraftImageCommands imageCommands;
	private final MobycraftDockerClient mobycraftDockerClient;

	@Inject
	public ContainerLifecycleCommands(MobycraftContainerListCommands listCommands,
									  MobycraftImageCommands imageCommands,
									  MobycraftDockerClient mobycraftDockerClient) {
		this.listCommands = listCommands;
		this.imageCommands = imageCommands;
		this.mobycraftDockerClient = mobycraftDockerClient;
	}

	public void start() {
		if (Utils.checkIfArgIsNull(args, 0)) {
			sendErrorMessage("Container name not specified! Command is used as /docker start <name> .");
			return;
		}

		try {
			mobycraftDockerClient.getDockerClient().startContainerCmd(
					listCommands.getFromAllWithName("/" + arg1).getId())
					.exec();
			sendConfirmMessage("Started container with name \"" + arg1 + "\"");
		} catch (NullPointerException exception) {
			sendErrorMessage("No container exists with the name \"" + arg1
					+ "\"");
		}
	}
	
	public void stop() {
		if (Utils.checkIfArgIsNull(args, 0)) {
			sendErrorMessage("Container name not specified! Command is used as /docker stop <name> .");
			return;
		}

		try {
			mobycraftDockerClient.getDockerClient().stopContainerCmd(
					listCommands.getWithName("/" + arg1).getId()).exec();
			sendConfirmMessage("Stopped container with name \"" + arg1 + "\"");
		} catch (NullPointerException exception) {
			sendErrorMessage("No container exists with the name \"" + arg1
					+ "\"");
		}
	}

	public void removeContainer(String containerId) {
		try {
			mobycraftDockerClient.getDockerClient().removeContainerCmd(containerId).withForce().exec();
		} catch (NullPointerException exception) {
			sendErrorMessage("No container exists with the name \"" + arg1 + "\"");
		}
	}

	public void remove() {
		if (Utils.checkIfArgIsNull(args, 0)) {
			sendErrorMessage("Container name not specified! Command is used as /docker rm <name> .");
			return;
		}

		try {
			mobycraftDockerClient.getDockerClient().removeContainerCmd(
							listCommands.getFromAllWithName("/" + arg1)
									.getId()).withForce().exec();
			sendConfirmMessage("Removed container with name \"" + arg1 + "\"");
		} catch (NullPointerException exception) {
			sendErrorMessage("No container exists with the name \"" + arg1
					+ "\"");
		}
	}
	
	public void removeAll() {
		sendFeedbackMessage("Working on it...");
		if (listCommands.getAll().size() < 1) {
			sendFeedbackMessage("No containers currently existing.");
			return;
		}
		for (Container container : listCommands.getAll()) {
			mobycraftDockerClient.getDockerClient().removeContainerCmd(container.getId()).withForce()
					.exec();
		}
		sendConfirmMessage("Removed all containers.");
	}
	
	public void restart() {
		if (Utils.checkIfArgIsNull(args, 0)) {
			sendErrorMessage("Container name not specified! Command is used as /docker restart <name> .");
			return;
		}

		try {
			mobycraftDockerClient.getDockerClient().restartContainerCmd(
					listCommands.getWithName("/" + arg1).getId()).exec();
			sendConfirmMessage("Restarted container with name \"" + arg1 + "\"");
		} catch (NullPointerException exception) {
			sendErrorMessage("No container exists with the name \"" + arg1
					+ "\"");
		}
	}
	
	public void kill() {
		if (Utils.checkIfArgIsNull(args, 0)) {
			sendErrorMessage("Container name not specified! Command is used as /docker kill <name> .");
			return;
		}

		try {
			mobycraftDockerClient.getDockerClient().killContainerCmd(
					listCommands.getWithName("/" + arg1).getId()).exec();
			sendConfirmMessage("Killed container with name \"" + arg1 + "\"");
		} catch (NullPointerException exception) {
			sendErrorMessage("No container exists with the name \"" + arg1
					+ "\"");
		}
	}
	
	public void killAll() {
		sendFeedbackMessage("Working on it...");
		if (listCommands.getStarted().size() < 1) {
			sendFeedbackMessage("No containers currently running.");
			return;
		}
		for (Container container : listCommands.getStarted()) {
			mobycraftDockerClient.getDockerClient().killContainerCmd(container.getId()).exec();
		}
		sendConfirmMessage("Killed all containers.");
	}
	
	public void run() throws InterruptedException {
		if (args.length < 1) {
			sendErrorMessage("No arguments specified! Command is used as /docker run <image> (name | amount) .");
			return;
		}
		
		sendFeedbackMessage("Working on it...");
		
		if (imageCommands.getImageWithName(arg1) == null) {
			PullImageResultCallback callback = new PullImageResultCallback();
			mobycraftDockerClient.getDockerClient().pullImageCmd(arg1).withTag("latest").exec(callback);
			try {
				callback.awaitCompletion();
			} catch (DockerClientException exception) {
				sendErrorMessage("\"" + arg1 + "\" + is not a valid image name!");
			}
		}

		if (args.length < 2) {
			// No name, no number
			CreateContainerResponse response = mobycraftDockerClient.getDockerClient()
					.createContainerCmd(arg1).exec();
			mobycraftDockerClient.getDockerClient().startContainerCmd(response.getId()).exec();
			String name = "";
			for (Container container : listCommands.getStarted()) {
				if (container.getId().equals(response.getId())) {
					name = container.getNames()[0];
				}
			}
			sendConfirmMessage("Created container with image \"" + arg1
					+ "\" and name \"" + name + "\"");
		} else if (!NumberUtils.isNumber(args[1])) {
			// Name
			CreateContainerResponse response = mobycraftDockerClient.getDockerClient()
					.createContainerCmd(arg1).withName(args[1]).exec();
			mobycraftDockerClient.getDockerClient().startContainerCmd(response.getId()).exec();
			sendConfirmMessage("Created container with image \"" + arg1
					+ "\" and name \"" + args[1] + "\"");
		} else {
			// Number
			ArrayList<String> names = new ArrayList<String>();
			for (int i = 0; i < Integer.parseInt(args[1]); i++) {
				CreateContainerResponse response = mobycraftDockerClient.getDockerClient()
						.createContainerCmd(arg1).exec();
				mobycraftDockerClient.getDockerClient().startContainerCmd(response.getId()).exec();
				String name = "";
				for (Container container : listCommands.getStarted()) {
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
		if (listCommands.getStopped().size() < 1) {
			sendFeedbackMessage("No containers currently stopped.");
			return;
		}
		for (Container container : listCommands.getStopped()) {
			mobycraftDockerClient.getDockerClient().removeContainerCmd(container.getId()).withForce()
					.exec();
		}
		sendConfirmMessage("Removed all stopped containers.");
	}
	
	public void switchState(StructureBuilder builder, String containerID) {
		listCommands.refreshContainerIDMap();

		// If there is no container with the ID, return
		if (listCommands.getBoxContainerWithID(containerID) == null) {
			return;
		}

		// New BoxContainer variable called boxContainer to store the container
		BoxContainer boxContainer = listCommands.getBoxContainerWithID(containerID);

		boxContainer.setState(!boxContainer.getState());

		Block containerBlock;
		Block prevContainerBlock;

		// If the container is now on (previously off):
		if (boxContainer.getState()) {
			containerBlock = Blocks.iron_block;
			prevContainerBlock = Blocks.redstone_block;
			mobycraftDockerClient.getDockerClient().startContainerCmd(boxContainer.getID()).exec();
		}
		// Otherwise, if the container is now off (previously on):
		else {
			containerBlock = Blocks.redstone_block;
			prevContainerBlock = Blocks.iron_block;
			mobycraftDockerClient.getDockerClient().stopContainerCmd(boxContainer.getID()).exec();
		}

		builder.replace(boxContainer.getWorld(), boxContainer.getPosition()
				.add(2, 0, 1), boxContainer.getPosition().add(-2, 0, 7),
				prevContainerBlock, containerBlock);

		builder.replace(boxContainer.getWorld(), boxContainer.getPosition()
				.add(2, 4, 1), boxContainer.getPosition().add(-2, 4, 7),
				prevContainerBlock, containerBlock);
	}
}