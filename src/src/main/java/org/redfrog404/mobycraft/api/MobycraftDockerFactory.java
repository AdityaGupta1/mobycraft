package org.redfrog404.mobycraft.api;

import org.redfrog404.mobycraft.commands.dockerjava.BasicDockerCommands;

public class MobycraftDockerFactory {
	public static final MobycraftDockerFactory INSTANCE = new MobycraftDockerFactory(); 
	
	public static MobycraftDockerFactory getInstance() {
		return INSTANCE;
	}
	
	public MobycraftCommands getMobycraftCommands() {
		return new BasicDockerCommands();
	}
	
	public MobycraftCommands getMobycraftCommands(String type) {
		if (type.equals("docker-java")) {
			return getMobycraftCommands();
		}
		
		throw new UnsupportedOperationException();
	}
}
