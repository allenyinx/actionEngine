package com.airta.platform.engine.runtime;

import java.io.Serializable;
import java.util.Map;

public class Task implements Serializable {

    private String id = null;
    private String script = null; // only script we'll have
    private String prepScript = null;
    private Map<String, String> cxtInfo = null;
    private int startNode = 0;
    private int threads = 1;

    private String rootUrl = "";

    public Task(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public Map<String, String> getCxtInfo() {
        return cxtInfo;
    }

    public void setCxtInfo(Map<String, String> cxtInfo) {
        this.cxtInfo = cxtInfo;
    }

    public int getStartNode() {
        return startNode;
    }

    public void setStartNode(int graphNode) {
        this.startNode = graphNode;
    }

    public String getPrepScript() {
        return prepScript;
    }

    public void setPrepScript(String prepScript) {
        this.prepScript = prepScript;
    }

    public String getRootUrl() {
        return rootUrl;
    }

    public void setRootUrl(String rootUrl) {
        this.rootUrl = rootUrl;
    }

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }
}
