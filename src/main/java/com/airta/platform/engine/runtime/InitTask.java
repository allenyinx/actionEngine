package com.airta.platform.engine.runtime;

import java.io.Serializable;

/**
 * task manager send the init task to init the env:
 * includes: init agents; init crawl sitemap.
 * @author allenyin
 */
public class InitTask implements Serializable {

    private String taskId;
    private String siteName;
    private int agentNumber;
    private String siteUrl;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public int getAgentNumber() {
        return agentNumber;
    }

    public void setAgentNumber(int agentNumber) {
        this.agentNumber = agentNumber;
    }

    public String getSiteUrl() {
        return siteUrl;
    }

    public void setSiteUrl(String siteUrl) {
        this.siteUrl = siteUrl;
    }
}
