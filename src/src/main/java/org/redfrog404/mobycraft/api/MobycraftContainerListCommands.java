package org.redfrog404.mobycraft.api;

import java.util.List;

import org.redfrog404.mobycraft.utils.BoxContainer;

import com.github.dockerjava.api.model.Container;

public interface MobycraftContainerListCommands {
	public void refresh();

	public void refreshRunning();

	public List<Container> getContainers();
	
	public void refreshContainerIDMap();

	public Container getWithName(String name);

	public Container getFromAllWithName(String name);

	public List<Container> getAll();

	public BoxContainer getBoxContainerWithID(String id);

	public boolean isStopped(String containerName);
	
	public List<Container> getStopped();

	public void heatMap();
}
