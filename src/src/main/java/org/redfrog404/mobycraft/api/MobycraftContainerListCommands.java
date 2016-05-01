package org.redfrog404.mobycraft.api;

import java.util.List;

import org.redfrog404.mobycraft.structure.BoxContainer;

import com.github.dockerjava.api.model.Container;

public interface MobycraftContainerListCommands {
	public List<Container> getStarted();
	public List<Container> getStopped();
	public boolean isStopped(String containerName);
	public List<Container> getAll();
	
	public void refresh();
	public void refreshRunning();
	public void refreshContainerIDMap();

	public Container getWithName(String name);
	public Container getFromAllWithName(String name);

	public BoxContainer getBoxContainerWithID(String id);

	public void heatMap();
	
	public void numberOfContainers();
}
