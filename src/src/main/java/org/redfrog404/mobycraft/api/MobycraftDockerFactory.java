package org.redfrog404.mobycraft.api;

import org.apache.commons.lang.NotImplementedException;
import org.redfrog404.mobycraft.commands.dockerjava.BasicCommands;

public class MobycraftDockerFactory {
	public static final MobycraftDockerFactory INSTANCE = new MobycraftDockerFactory(); 
	
	public static MobycraftDockerFactory getInstance() {
		return INSTANCE;
	}
	
	public MobycraftCommands getMobycraftCommands() {
		return new BasicCommands();
	}
	
	public MobycraftCommands getMobycraftCommands(String type) {
		if (type.equals("docker-java")) {
			return getMobycraftCommands();
		}
		
		throw new NotImplementedException();
	}
}
