package org.redfrog404.mobycraft.commands.titus.model;

import com.fasterxml.jackson.annotation.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Job {
    @JsonProperty
    private String id;

    @JsonProperty("applicationName")
    private String applicationName;

    @JsonProperty("entryPoint")
    private String entryPoint;

    @JsonProperty("cpu")
    private double cpu;

    @JsonProperty("mem")
    private double mem;

    @JsonProperty("disk")
    private double disk;

    @JsonProperty("submittedAt")
    private String submittedAt;

    @JsonProperty("tasks")
    private Task[] tasks;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getEntryPoint() {
        return entryPoint;
    }

    public void setEntryPoint(String entryPoint) {
        this.entryPoint = entryPoint;
    }

    public double getCpu() {
        return cpu;
    }

    public void setCpu(double cpu) {
        this.cpu = cpu;
    }

    public double getMem() {
        return mem;
    }

    public void setMem(double mem) {
        this.mem = mem;
    }

    public double getDisk() {
        return disk;
    }

    public void setDisk(double disk) {
        this.disk = disk;
    }

    public String getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(String submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Task[] getTasks() {
        return tasks;
    }

    public void setTasks(Task[] tasks) {
        this.tasks = tasks;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Task {
        @JsonProperty("id")
        private String id;

        @JsonProperty("instanceId")
        private String instanceId;

        @JsonProperty("state")
        private String state;

        @JsonProperty("host")
        private String host;

        @JsonProperty("region")
        private String region;

        @JsonProperty("zone")
        private String zone;

        @JsonProperty("submittedAt")
        private String submittedAt;

        @JsonProperty("launchedAt")
        private String launchedAt;

        @JsonProperty("startedAt")
        private String startedAt;

        @JsonProperty("finishedAt")
        private String finishedAt;

        @JsonProperty("message")
        private String message;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getInstanceId() {
            return instanceId;
        }

        public void setInstanceId(String instanceId) {
            this.instanceId = instanceId;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public String getZone() {
            return zone;
        }

        public void setZone(String zone) {
            this.zone = zone;
        }

        public String getSubmittedAt() {
            return submittedAt;
        }

        public void setSubmittedAt(String submittedAt) {
            this.submittedAt = submittedAt;
        }

        public String getLaunchedAt() {
            return launchedAt;
        }

        public void setLaunchedAt(String launchedAt) {
            this.launchedAt = launchedAt;
        }

        public String getStartedAt() {
            return startedAt;
        }

        public void setStartedAt(String startedAt) {
            this.startedAt = startedAt;
        }

        public String getFinishedAt() {
            return finishedAt;
        }

        public void setFinishedAt(String finishedAt) {
            this.finishedAt = finishedAt;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}