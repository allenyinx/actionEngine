package com.airta.platform.engine.entity.pool;

import java.io.Serializable;

/**
 * @author allenyin
 */
public class AgentPool implements Serializable {

    private String poolName;
    private int agentSize;
    private int poolGroup;
    private String type;
    private String url;

    public String getPoolName() {
        return poolName;
    }

    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }

    public int getAgentSize() {
        return agentSize;
    }

    public void setAgentSize(int agentSize) {
        this.agentSize = agentSize;
    }

    public int getPoolGroup() {
        return poolGroup;
    }

    public void setPoolGroup(int poolGroup) {
        this.poolGroup = poolGroup;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
