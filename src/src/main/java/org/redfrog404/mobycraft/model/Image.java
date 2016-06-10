package org.redfrog404.mobycraft.model;

public class Image {
    private long created;
    private String id;
    private String parentId;
    private String[] repoTags;
    private long size;
    private long virtualSize;

    public Image(long created, String id, String parentId, String[] repoTags, long size, long virtualSize) {
        this.created = created;
        this.id = id;
        this.parentId = parentId;
        this.repoTags = repoTags;
        this.size = size;
        this.virtualSize = virtualSize;
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

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String[] getRepoTags() {
        return repoTags;
    }

    public void setRepoTags(String[] repoTags) {
        this.repoTags = repoTags;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getVirtualSize() {
        return virtualSize;
    }

    public void setVirtualSize(long virtualSize) {
        this.virtualSize = virtualSize;
    }
}
