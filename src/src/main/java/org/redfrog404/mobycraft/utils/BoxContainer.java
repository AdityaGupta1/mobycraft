package org.redfrog404.mobycraft.utils;

import static org.redfrog404.mobycraft.commands.BasicDockerCommands.execStatsCommand;
import static org.redfrog404.mobycraft.commands.MainCommand.arg1;
import static org.redfrog404.mobycraft.commands.MainCommand.getDockerClient;

import java.util.List;

import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import com.github.dockerjava.api.model.Container;

public class BoxContainer {

	private BlockPos position;
	private String ID;
	private String name;
	private String image;
	private boolean state = true;
	private World world;
	private double memoryUsage;
	private double cpuUsage;

	public BoxContainer(BlockPos position, String ID, String name,
			String image, World world) {
		this.position = position;
		this.ID = ID;
		this.name = name;
		this.image = image;
		this.world = world;
	}

	public BlockPos getPosition() {
		return position;
	}

	public String getID() {
		return ID;
	}

	public String getShortID() {
		return ID.substring(0, 12);
	}

	public void setPosition(BlockPos newPos) {
		position = newPos;
	}

	public String getName() {
		return name;
	}

	public String getImage() {
		return image;
	}

	public boolean getState() {
		return state;
	}

	public void setState(boolean newState) {
		state = newState;
	}

	public World getWorld() {
		return world;
	}

	public Container getContainer(List<Container> containers) {
		for (Container container : containers) {
			if (container.getId().equals(ID)) {
				return container;
			}
		}

		return null;
	}

	@Override
	public String toString() {
		return "[\"" + this.getName() + "\", \"" + this.getID() + "\""
				+ "\", \"" + this.getState() + "\"]";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		return obj.toString().equals(this.toString());
	}

	public void setCpuUsage(double newCpuUsage) {
		cpuUsage = newCpuUsage;
	}

	public void setMemoryUsage(double newMemoryUsage) {
		memoryUsage = newMemoryUsage;
	}

	public double getCpuUsage() {
		execStatsCommand(this.getID(), false);
		return cpuUsage;
	}

	public double getMemoryUsage() {
		execStatsCommand(this.getID(), false);
		return memoryUsage;
	}

}
