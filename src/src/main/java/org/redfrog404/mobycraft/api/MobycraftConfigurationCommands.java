package org.redfrog404.mobycraft.api;

import org.redfrog404.mobycraft.commands.dockerjava.ConfigProperties;

import net.minecraft.util.BlockPos;

public interface MobycraftConfigurationCommands {
	public void setPath();
	public void setHost();
	public void setPollRate();
	public void setStartPos();
	public BlockPos getStartPos();
	public void getHostAndPath();
	public ConfigProperties getConfigProperties();
}
