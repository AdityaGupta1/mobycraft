package org.redfrog404.mobycraft.api;

import org.redfrog404.mobycraft.structure.BoxContainer;

import org.redfrog404.mobycraft.model.Container;

public interface MobycraftBasicCommands {
	public void help();
	public void ps();
	public void showDetailedInfo() throws InterruptedException;
	public void printContainerInfo(BoxContainer boxContainer, Container container);
}
