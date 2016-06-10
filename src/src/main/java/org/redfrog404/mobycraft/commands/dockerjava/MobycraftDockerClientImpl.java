package org.redfrog404.mobycraft.commands.dockerjava;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import org.redfrog404.mobycraft.api.MobycraftConfigurationCommands;
import org.redfrog404.mobycraft.commands.common.ConfigProperties;

import javax.inject.Inject;

public class MobycraftDockerClientImpl implements MobycraftDockerClient {
    private final MobycraftConfigurationCommands configurationCommands;
    private DockerClient dockerClient;

    @Inject
    private MobycraftDockerClientImpl(MobycraftConfigurationCommands configurationCommands) {
        this.configurationCommands = configurationCommands;
    }

    @Override
    public DockerClient getDockerClient() {
        if (dockerClient == null) {
            ConfigProperties configProperties = configurationCommands.getConfigProperties();

            DockerClientConfig dockerConfig = DockerClientConfig
                    .createDefaultConfigBuilder()
                    .withUri(configProperties.getDockerHostProperty().getString())
                    .withDockerCertPath(configProperties.getCertPathProperty().getString()).build();

            dockerClient = DockerClientBuilder.getInstance(dockerConfig).build();
        }
        return dockerClient;
    }
}
