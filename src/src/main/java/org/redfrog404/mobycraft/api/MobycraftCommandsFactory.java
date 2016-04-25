package org.redfrog404.mobycraft.api;

import org.redfrog404.mobycraft.commands.dockerjava.BasicDockerCommands;
import org.redfrog404.mobycraft.commands.dockerjava.BuildContainerCommands;
import org.redfrog404.mobycraft.commands.dockerjava.ConfigurationCommands;
import org.redfrog404.mobycraft.commands.dockerjava.ContainerLifecycleCommands;
import org.redfrog404.mobycraft.commands.dockerjava.ContainerListCommands;
import org.redfrog404.mobycraft.commands.dockerjava.ImageCommands;

public class MobycraftCommandsFactory {
	private static final MobycraftCommandsFactory INSTANCE = new MobycraftCommandsFactory(); 
	private static MobycraftBasicCommands basicCommands;
	private static MobycraftContainerLifecycleCommands lifecycleCommands;
	private static MobycraftBuildContainerCommands buildCommands;
	private static MobycraftContainerListCommands listCommands;
	private static MobycraftImageCommands imageCommands;
	private static MobycraftConfigurationCommands configCommands;
	
	public static MobycraftCommandsFactory getInstance() {
		return INSTANCE;
	}
	
	public MobycraftBasicCommands getBasicCommands() {
		if (basicCommands == null) {
			basicCommands = new BasicDockerCommands();
		}
		return basicCommands;
	}
	
	public MobycraftContainerLifecycleCommands getLifecycleCommands() {
		if (lifecycleCommands == null) {
			lifecycleCommands = new ContainerLifecycleCommands(); 
		}
		return lifecycleCommands;
	}
	
	public MobycraftBuildContainerCommands getBuildCommands() {
		if (buildCommands == null) {
			buildCommands = new BuildContainerCommands(); 
		}
		return buildCommands;
	}
	
	public MobycraftContainerListCommands getListCommands() {
		if (listCommands == null) {
			listCommands = new ContainerListCommands(); 
		}
		return listCommands;
	}

	public MobycraftImageCommands getImageCommands() {
		if (imageCommands  == null) {
			imageCommands = new ImageCommands(); 
		}
		return imageCommands ;
	}

	public MobycraftConfigurationCommands getConfigurationCommands() {
		if (configCommands  == null) {
			configCommands = new ConfigurationCommands(); 
		}
		return configCommands ;
	}

	public MobycraftBasicCommands getMobycraftCommands(String type) {
		if (type.equals("docker-java")) {
			return getBasicCommands();
		}
		
		throw new UnsupportedOperationException();
	}
}
