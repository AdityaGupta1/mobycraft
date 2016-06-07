package org.redfrog404.mobycraft.commands.mock;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.redfrog404.mobycraft.model.Container;
import com.google.common.io.Resources;
import net.minecraft.util.EnumChatFormatting;
import org.redfrog404.mobycraft.api.MobycraftBuildContainerCommands;
import org.redfrog404.mobycraft.api.MobycraftConfigurationCommands;
import org.redfrog404.mobycraft.api.MobycraftContainerListCommands;
import org.redfrog404.mobycraft.structure.BoxContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static org.redfrog404.mobycraft.commands.common.MainCommand.arg1;
import static org.redfrog404.mobycraft.commands.common.MainCommand.boxContainers;
import static org.redfrog404.mobycraft.commands.common.MainCommand.containerIDMap;
import static org.redfrog404.mobycraft.commands.common.MainCommand.sender;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.redfrog404.mobycraft.utils.MessageSender.*;

public class ContainerListCommands implements MobycraftContainerListCommands {

	Logger logger = LoggerFactory.getLogger(ContainerListCommands.class);

	private final MobycraftBuildContainerCommands buildCommands;
	private final MobycraftConfigurationCommands configurationCommands;

	@Inject
	public ContainerListCommands(MobycraftBuildContainerCommands buildCommands, MobycraftConfigurationCommands configurationCommands) {
		this.buildCommands = buildCommands;
		this.configurationCommands = configurationCommands;
	}

	public void refresh() {
		List<Container> containers = getAll();
		boxContainers = buildCommands.containerPanel(containers,
				configurationCommands.getStartPos(),
				sender.getEntityWorld());
		List<String> stoppedContainerNames = new ArrayList<String>();
		for (Container container : getStopped()) {
			stoppedContainerNames.add(container.getNames()[0]);
		}
		for (BoxContainer boxContainer : boxContainers) {
			if (stoppedContainerNames.contains(boxContainer.getName())) {
				boxContainer.setState(!boxContainer.getState());
			}
		}
		refreshContainerIDMap();
	}

	public void refreshRunning() {
		List<Container> containers = getStarted();
		boxContainers = buildCommands.containerPanel(containers,
				configurationCommands.getStartPos(),
				sender.getEntityWorld());
		refreshContainerIDMap();
	}

	public void refreshContainerIDMap() {
		containerIDMap.clear();
		for (BoxContainer boxContainer : boxContainers) {
			containerIDMap.put(boxContainer.getID(), boxContainer);
		}
	}

	public List<Container> getStarted() {
		return getAll();
	}

	public List<Container> getStopped() {
		List<Container> containers = getAll();
		for (Container container : containers) {
			if (container.getStatusString().toLowerCase().contains("exited")) {
				containers.add(container);
			}
		}

		return containers;
	}

	public Container getWithName(String name) {
		for (Container container : getStarted()) {
			if (container.getNames()[0].equals(name)) {
				return container;
			}
		}
		return null;
	}

	public Container getFromAllWithName(String name) {
		List<Container> containers = getAll();
		for (Container container : containers) {
			if (container.getNames()[0].equals(name)) {
				return container;
			}
		}
		return null;
	}

	public List<Container> getAll() {
		ObjectMapper mapper = new ObjectMapper();
		List<Container> containers = new ArrayList<Container>();
		try {
			URL url1 = Resources.getResource("mockContainers.json");
			String jsonText1 = Resources.toString(url1, StandardCharsets.UTF_8);
			containers = mapper.readValue(jsonText1, new TypeReference<List<Container>>() {
			});
		} catch (IOException ioe) {
			logger.warn("Could not load mockContainers.json");
		}
		return containers;
	}

	/*
	 * Gets the BoxContainer from the containerIDMap with id <id>
	 */
	public BoxContainer getBoxContainerWithID(String id) {
		refreshContainerIDMap();
		if (!containerIDMap.containsKey(id)) {
			return null;
		}
		return containerIDMap.get(id);
	}

	public boolean isStopped(String containerName) {
		if (getFromAllWithName(containerName) == null) {
			return false;
		}

		Container container = getFromAllWithName(containerName);

		if (container.getStatusString().toLowerCase().contains("exited")) {
			return true;
		}

		return false;
	}

	public void heatMap() {
		if (arg1 == null) {
			sendErrorMessage("Type of heat map not specified! \"cpu\" and \"memory\" are accepted as types.");
			return;
		}

		if (!arg1.equalsIgnoreCase("cpu") && !arg1.equalsIgnoreCase("memory")) {
			sendErrorMessage("\""
					+ arg1
					+ "\" is not a valid argument! Only \"cpu\" and \"memory\" are accepted.");
			return;
		}

		sendFeedbackMessage("Not yet implemented...");
	}

	@Override
	public void execStatsCommand(String containerID, boolean sendMessages) {
	}

	public void numberOfContainers() {
		sendMessage(EnumChatFormatting.GOLD
				+ "Number of container(s) currently running: "
				+ EnumChatFormatting.GREEN + "NOT IMPLEMENTED");

	}
}