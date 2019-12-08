package com.airta.platform.engine.entity.pool;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author allenyin
 */
public class PodSessionPool implements Serializable {

    private String poolName;
    private List<PodSession> podSessionList = new ArrayList<>();

    public List<PodSession> getPodSessionList() {
        return podSessionList;
    }

    public void setPodSessionList(List<PodSession> podSessionList) {
        this.podSessionList = podSessionList;
    }

    public String getPoolName() {
        return poolName;
    }

    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }
}
