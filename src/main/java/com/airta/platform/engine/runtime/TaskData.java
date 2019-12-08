package com.airta.platform.engine.runtime;

import java.util.Map;

public class TaskData {

    private int type = TaskDataType.ALL;
    private String data = null;
    private String url = null;
    private long createTime = 0;
    private Map<String, String> attrs = null; // exepct it to be a map with attributes

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public Map<String, String> getAttributes() {
        return attrs;
    }

    public void setAttributes(Map<String, String> attrs) {
        this.attrs = attrs;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
