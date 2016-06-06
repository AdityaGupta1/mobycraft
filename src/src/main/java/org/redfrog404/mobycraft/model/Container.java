package org.redfrog404.mobycraft.model;

public class Container {
    private String command;
    private long created;
    private String id;
    private String image;
    private String[] names;

    public Container(String command, long created, String id, String image, String[] names, ContainerStatus status, String statusString) {
        this.command = command;
        this.created = created;
        this.id = id;
        this.image = image;
        this.names = names;
        this.status = status;
        this.statusString = statusString;
    }

    //    public Port[] ports;
//    public Map<String, String> labels;
    private ContainerStatus status;
    private String statusString;

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String[] getNames() {
        return names;
    }

    public void setNames(String[] names) {
        this.names = names;
    }

    public ContainerStatus getStatus() {
        return status;
    }

    public void setStatus(ContainerStatus status) {
        this.status = status;
    }

    public String getStatusString() {
        return statusString;
    }

    public void setStatusString(String statusString) {
        this.statusString = statusString;
    }

    public enum ContainerStatus {
        QUEUED, DISPATCHED, STARTING, RUNNING, FINISHED, ERROR, STOPPED, CRASHED, FAILED, UNKNOWN
    }
}