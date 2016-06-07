package org.redfrog404.mobycraft.main;

import com.google.inject.AbstractModule;
import org.redfrog404.mobycraft.api.*;
import org.redfrog404.mobycraft.commands.dockerjava.MobycraftDockerClient;
import org.redfrog404.mobycraft.commands.dockerjava.MobycraftDockerClientImpl;

public class MobyCraftModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(MobycraftBasicCommands.class).to(org.redfrog404.mobycraft.commands.common.BasicDockerCommands.class);
        bind(MobycraftBuildContainerCommands.class).to(org.redfrog404.mobycraft.commands.common.BuildContainerCommands.class);
        bind(MobycraftConfigurationCommands.class).to(org.redfrog404.mobycraft.commands.dockerjava.ConfigurationCommands.class);

        //  for docker-machine local
        bind(MobycraftContainerListCommands.class).to(org.redfrog404.mobycraft.commands.dockerjava.ContainerListCommands.class);
        bind(MobycraftContainerLifecycleCommands.class).to(org.redfrog404.mobycraft.commands.dockerjava.ContainerLifecycleCommands.class);
        bind(MobycraftImageCommands.class).to(org.redfrog404.mobycraft.commands.dockerjava.ImageCommands.class);
        bind(MobycraftDockerClient.class).to(MobycraftDockerClientImpl.class);

        // for mocking via JSON files under resources
//        bind(MobycraftContainerListCommands.class).to(org.redfrog404.mobycraft.commands.mock.ContainerListCommands.class);
//        bind(MobycraftContainerLifecycleCommands.class).to(org.redfrog404.mobycraft.commands.mock.ContainerLifecycleCommands.class);
//        bind(MobycraftImageCommands.class).to(org.redfrog404.mobycraft.commands.mock.ImageCommands.class);

        // for Titus
//        bind(MobycraftContainerListCommands.class).to(org.redfrog404.mobycraft.commands.titus.ContainerListCommands.class);
//        bind(MobycraftContainerLifecycleCommands.class).to(org.redfrog404.mobycraft.commands.titus.ContainerLifecycleCommands.class);
//        bind(MobycraftImageCommands.class).to(org.redfrog404.mobycraft.commands.titus.ImageCommands.class);
    }
}
