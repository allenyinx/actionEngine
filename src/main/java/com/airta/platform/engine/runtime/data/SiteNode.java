package com.airta.platform.engine.runtime.data;

import java.util.ArrayList;

public class SiteNode {
    private String url = null;
    private String featureCondition = null;
    private int id = 0;
    private ArrayList<Integer> links = null;

    public SiteNode(String url, String featureCondition) {
        this.url = url;
        this.featureCondition = featureCondition;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFeatureCondition() {
        return featureCondition;
    }

    public void setFeatureCondition(String featureCondition) {
        this.featureCondition = featureCondition;
    }

    public int getId() {
        return id;
    }

    public void setId(int nodeId) {
        this.id = nodeId;
    }

    public int[] getLinkedNodes(boolean outbound) {
        ArrayList<Integer> ret = new ArrayList<>();
        for (Integer i : links) {
            if (i > 0 && outbound) {
                ret.add(i);
            } else if (i < 0 && !outbound) {
                ret.add(-i);
            }
        }
        if (ret.size() < 1) {
            return null;
        }
        int[] ret2 = new int[ret.size()];
        for (int i = 0; i < ret2.length; i++) {
            ret2[i] = ret.get(i);
        }
        return ret2;
    }


    public void addLink(int boundNode, boolean outbound) {
        if (boundNode > 0 && boundNode != id && id > 0) {
            // no validation
            if (outbound) {
                links.add(boundNode);
            } else {
                links.add(-boundNode);
            }
        }
    }

    public SiteNode copyOf() {
        SiteNode ret = new SiteNode(url, featureCondition);
        ret.id = id;
        return ret;
    }

    public String getKey() {
        return url;
    }
}
