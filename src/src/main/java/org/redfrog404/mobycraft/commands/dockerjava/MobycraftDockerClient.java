package org.redfrog404.mobycraft.commands.dockerjava;

import com.github.dockerjava.api.DockerClient;

public interface MobycraftDockerClient {
    public DockerClient getDockerClient();
}
