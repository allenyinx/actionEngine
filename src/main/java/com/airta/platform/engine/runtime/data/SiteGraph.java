package com.airta.platform.engine.runtime.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SiteGraph {
    private Map<String, Integer> checker = new HashMap<>();
    private ArrayList<SiteNode> nodes = new ArrayList<>();
    private ArrayList<SiteLink> links = new ArrayList<>();

    // looking for the shortest way (time and steps)
    public SiteLink[] getPath(int start, int end) {
        if (!(start > 0 && end > 0 && start != end && start <= nodes.size() && end <= nodes.size())) {
            return null;
        }
        int[] lnks = new int[nodes.size() + 1];
        int[] weights = new int[nodes.size() + 1];

        boolean got = true;
        while (got) {
            got = false;
            for (SiteLink t : links) {
                int st = t.getStartNode();
                int ed = t.getEndNode();
                if (st == start) {
                    if (weights[ed] != 1) {
                        weights[ed] = 1;
                        lnks[ed] = st;
                        got = true;
                    }
                } else if (ed != start) {
                    if (weights[st] > 0 && (weights[ed] == 0 || weights[st] + 1 < weights[ed])) {
                        lnks[ed] = st;
                        weights[ed] = weights[st] + 1;
                        got = true;

                    }
                }
            }
        }
        if (weights[end] < 1) {
            return null;
        }
        ArrayList<SiteLink> its = new ArrayList<>(); // wieght != count
        while (end != start) {
            int head = lnks[end];
            if (head < 1) {
                its.clear();
                break;
            }
            String key = head + "-" + end;
            its.add(links.get(checker.get(key)).copyOf());
            end = head;
        }
        SiteLink[] ret = null;
        if (end == start) {
            ret = new SiteLink[its.size()];
            its.toArray(ret);
            its.clear();
        }
        return ret;
    }

    public SiteNode getRootNode() {
        return nodes.size() > 0 ? nodes.get(0).copyOf() : null;
    }

    public SiteNode getNodeById(int id) {
        if (id > 0 && id <= nodes.size()) {
            return nodes.get(id - 1).copyOf();
        }
        return null;
    }

    public void setRootNode(String url, String featureCondition) throws Exception {
        if (nodes.size() < 1) {
            addNode(url, featureCondition);
        } else {
            throw new Exception("root node already set!");
        }
    }

    public int addNode(String url, String featureCondition) {
        SiteNode node = new SiteNode(url, featureCondition);
        if (checker.containsKey(node.getKey())) {
            return checker.get(node.getKey());
        }
        node.setId(nodes.size() + 1);
        nodes.add(node);
        checker.put(node.getKey(), node.getId());
        return node.getId();
    }

    public SiteLink getLink(int startNode, int endNode) {
        String key = startNode + "-" + endNode;
        Integer idx = checker.get(key);
        if (idx == null) {
            return null;
        }
        return links.get(idx).copyOf();
    }

    public boolean addLink(int startNode, int endNode, String stepIn, int stepTime) {
        if (startNode != endNode && startNode > 0 && startNode <= nodes.size() && endNode > 0 && endNode <= nodes.size()) {
            String key = startNode + "-" + endNode;
            if (!checker.containsKey(key)) {
                checker.put(key, links.size());
                links.add(new SiteLink(startNode, endNode, stepIn, stepTime));
                return true;
            }
        }
        return false;
    }

    public void reset() {
        checker.clear();
        nodes.clear();
        links.clear();
    }

    public int getNodeSize() {
        return nodes.size();
    }

    public int getNodeByKey(String key) {
        Integer i = checker.get(key);
        if (i != null) {
            return i;
        } else {
            return -1;
        }
    }

    public static void main(String[] args) throws Exception {
        SiteGraph sg = new SiteGraph();
        sg.setRootNode("aa", "bb");
        sg.addNode("bb", "bb");
        sg.addNode("cc", "cc");
        sg.addNode("dd", "dd");
        sg.addLink(1, 2, "aaa", 11);

        sg.addLink(2, 3, "aaa", 11);
        sg.addLink(3, 4, "aaa", 11);
        sg.addLink(1, 3, "aaa", 11);
        SiteLink[] links = sg.getPath(1, 4);
    }

    public String packForDemo() {
        // get nodes
        StringBuilder aa = new StringBuilder();
        aa.append(nodes.size());
        for (int i = 0; i < nodes.size(); i++) {
            aa.append(",");
            aa.append(i == 0 ? "root" : ("node" + (i + 1)));
        }
        // for all the links
        for (SiteLink link : links) {
            aa.append(",");
            aa.append(link.getStartNode() - 1);
            aa.append(",");
            aa.append(link.getEndNode() - 1);
        }
        return aa.toString();
    }
}

