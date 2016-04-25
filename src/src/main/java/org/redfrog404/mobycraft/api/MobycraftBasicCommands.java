package org.redfrog404.mobycraft.api;

import org.redfrog404.mobycraft.commands.dockerjava.ConfigProperties;
import org.redfrog404.mobycraft.utils.BoxContainer;

import com.github.dockerjava.api.model.Container;

import net.minecraft.util.BlockPos;

public interface MobycraftBasicCommands {
	public void help();
	public void ps();
	public void showDetailedInfo() throws InterruptedException;
	public void printBasicContainerInformation(BoxContainer boxContainer, Container container);
}
