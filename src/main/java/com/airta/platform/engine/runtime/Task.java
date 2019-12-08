package com.airta.platform.engine.runtime;

import java.util.Map;

public class Task {
    private String id = null;
    private String script = null; // only script we'll have
    private String prepScript = null;
    private Map<String, String> cxtInfo = null;
    private int startNode = 0;

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
}
