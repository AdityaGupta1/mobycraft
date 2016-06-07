package org.redfrog404.mobycraft.api;

import org.redfrog404.mobycraft.structure.StructureBuilder;

public interface MobycraftContainerLifecycleCommands {
	public void start();
	public void stop();	
	public void run() throws InterruptedException;
	public void restart();
	public void kill();
	public void killAll();
	public void removeContainer(String containerId);
	public void remove();
	public void removeStopped();
	public void removeAll();
	public void switchState(StructureBuilder builder, String containerID);
}
