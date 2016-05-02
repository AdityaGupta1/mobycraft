package org.redfrog404.mobycraft.main;

import com.google.inject.AbstractModule;
import org.redfrog404.mobycraft.api.*;

public class MobyCraftModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(MobycraftBasicCommands.class).to(org.redfrog404.mobycraft.commands.common.BasicDockerCommands.class);
        bind(MobycraftBuildContainerCommands.class).to(org.redfrog404.mobycraft.commands.common.BuildContainerCommands.class);
        bind(MobycraftConfigurationCommands.class).to(org.redfrog404.mobycraft.commands.dockerjava.ConfigurationCommands.class);

        //  for docker-machine local
//        bind(MobycraftContainerListCommands.class).to(org.redfrog404.mobycraft.commands.dockerjava.ContainerListCommands.class);
//        bind(MobycraftContainerLifecycleCommands.class).to(org.redfrog404.mobycraft.commands.dockerjava.ContainerLifecycleCommands.class);
//        bind(MobycraftImageCommands.class).to(org.redfrog404.mobycraft.commands.dockerjava.ImageCommands.class);

        // for mocking via JSON files under resources
        bind(MobycraftContainerListCommands.class).to(org.redfrog404.mobycraft.commands.mock.ContainerListCommands.class);
        bind(MobycraftContainerLifecycleCommands.class).to(org.redfrog404.mobycraft.commands.mock.ContainerLifecycleCommands.class);
        bind(MobycraftImageCommands.class).to(org.redfrog404.mobycraft.commands.mock.ImageCommands.class);

    }
}
