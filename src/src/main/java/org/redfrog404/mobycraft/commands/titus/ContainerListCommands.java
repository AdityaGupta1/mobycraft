package org.redfrog404.mobycraft.commands.titus;

import org.redfrog404.mobycraft.model.Container;
import org.redfrog404.mobycraft.model.Container.ContainerStatus;
import net.minecraft.util.EnumChatFormatting;
import org.redfrog404.mobycraft.api.MobycraftBuildContainerCommands;
import org.redfrog404.mobycraft.api.MobycraftConfigurationCommands;
import org.redfrog404.mobycraft.api.MobycraftContainerListCommands;
import org.redfrog404.mobycraft.structure.BoxContainer;
import org.redfrog404.mobycraft.commands.titus.model.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.client.*;
import javax.ws.rs.core.GenericType;

import static org.redfrog404.mobycraft.commands.common.MainCommand.arg1;
import static org.redfrog404.mobycraft.commands.common.MainCommand.boxContainers;
import static org.redfrog404.mobycraft.commands.common.MainCommand.containerIDMap;
import static org.redfrog404.mobycraft.commands.common.MainCommand.sender;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
		List<Container> containers = new ArrayList<>();
//		List<Container> containers = getAll();
//		for (Container container : containers) {
//			if (container.getStatus().toLowerCase().contains("exited")) {
//				containers.add(container);
//			}
//		}
//
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
		Client client = ClientBuilder.newClient().register(FixHeadersClientResponseFilter.class);
		WebTarget target = client
				.target("http://TITUSAPIHOST:TITUSPORT")
//				.target("http://localhost:8000")
				.path("/v2/jobs")
//				.path("R.json")
				.queryParam("taskState", "RUNNING");
		List<Job> jobs = target.request().get(new GenericType<List<Job>>() {});
		List<Container> containers = convertJobsToContainers(jobs);
//		logger.info("jobs = " + jobs);

		return containers;
	}

	private List<Container> convertJobsToContainers(List<Job> jobs) {
		List<Container> containers = new ArrayList<>();
		for (Job job: jobs) {
			for (Job.Task task : job.getTasks()) {
				DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
				ZonedDateTime date = ZonedDateTime.parse(task.getStartedAt(), formatter);
				long startedAtLong = date.toInstant().getEpochSecond() * 1000;

				String names[] = {task.getId()};
				// TODO: parse the states
				Container c = new Container(job.getEntryPoint(), startedAtLong, task.getId(), job.getApplicationName(), names, ContainerStatus.RUNNING, "running");
				containers.add(c);
			}
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