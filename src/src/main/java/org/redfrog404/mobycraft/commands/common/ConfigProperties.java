package org.redfrog404.mobycraft.commands.common;

import net.minecraftforge.common.config.Property;

public class ConfigProperties {
	Property certPathProperty;
	Property dockerHostProperty;
	Property startPosProperty;
	Property pollRateProperty;
	
	public Property getCertPathProperty() {
		return certPathProperty;
	}
	
	public void setCertPathProperty(Property certPathProperty) {
		this.certPathProperty = certPathProperty;
	}
	
	public Property getDockerHostProperty() {
		return dockerHostProperty;
	}
	
	public void setDockerHostProperty(Property dockerHostProperty) {
		this.dockerHostProperty = dockerHostProperty;
	}
	
	public Property getStartPosProperty() {
		return startPosProperty;
	}
	
	public void setStartPosProperty(Property startPosProperty) {
		this.startPosProperty = startPosProperty;
	}
	
	public Property getPollRateProperty() {
		return pollRateProperty;
	}
	
	public void setPollRateProperty(Property pollRateProperty) {
		this.pollRateProperty = pollRateProperty;
	}
}
