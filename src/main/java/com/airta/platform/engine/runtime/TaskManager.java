package com.airta.platform.engine.runtime;

import com.airta.platform.engine.runtime.data.SiteGraph;
import com.airta.platform.engine.runtime.data.SiteLink;
import com.airta.platform.engine.runtime.data.SiteNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * task manager will simply run things underlying
 */
public class TaskManager {

    private static class ResultItem {
        public TaskAgent agent = null;
        public long createTime = 0;
        public long syncTime = 0;
        public TaskResult result = null;
        public Task task = null;
    }

    private ArrayList<TaskAgent> agents;
    private SiteGraph siteGraph;
    private Map<String, String> cxtInfo;
    private ArrayList<ResultItem> resultQueue;
    private String rootUrl = null;

    public TaskManager(String rootUrl, String featureCondition, Map<String, String> cxtInfo) throws Exception {
        agents = new ArrayList<>();
        siteGraph = new SiteGraph();
        // add the root node, which is empty
        siteGraph.setRootNode("###", "");
        resultQueue = new ArrayList<>();
        this.rootUrl = rootUrl;
        TaskData td = new TaskData();

        td.setUrl(rootUrl);
        td.setType(TaskDataType.ELEMENT);
        td.setData(null);
        processActionableDatas(new TaskData[]{td}, siteGraph.getRootNode().getId());
    }


    private void processActionableDatas(TaskData[] datas, int startNode) throws Exception {
        // generate the script
        String[] scripts = generateActionableScript(datas, startNode);
        if (scripts == null || scripts.length < 3) {
            return;
        }
        //ok, now we do have different urls
        String prepScript = scripts[0];
        for (int i = 1; i < scripts.length - 1; i += 2) {
            String script = scripts[i];
            String url = scripts[i + 1];
            SiteNode sn = new SiteNode(url, "");
            if (url != null) {
                log("processing url:" + url);
                int node = siteGraph.getNodeByKey(sn.getKey());
                if (node > 0) {
                    if (siteGraph.getLink(startNode, node) == null) {
                        siteGraph.addLink(startNode, node, script, 0);
                    }
                    continue;
                }
            }
            // create the task in queue
            Task task = new Task(UUID.randomUUID().toString());
            task.setCxtInfo(getCxtInfo());
            task.setStartNode(startNode); // this just means it's a root entry ye
            task.setScript(script);
            task.setPrepScript(prepScript);
            log("adding task with script from node " + startNode);
            log("prepare script::");
            log(prepScript);
            log("target script::");
            log(script);
            ResultItem item = new ResultItem();
            item.task = task;
            resultQueue.add(item);
        }
    }

    // this will generate multiple scripts if there are mutliple links
    private String[] generateActionableScript(TaskData[] datas, int startNode) throws Exception {
        StringBuilder sb = new StringBuilder();
        StringBuilder prep = new StringBuilder();
        if (startNode != siteGraph.getRootNode().getId()) {
            SiteLink[] links = siteGraph.getPath(siteGraph.getRootNode().getId(), startNode);
            if (links == null) {
                throw new Exception("prepare path in site graph is not correct!");
            }
            for (int i = 0; i < links.length; i++) {
                prep.append(links[links.length - i - 1].getStepIn());
                prep.append("\n");
            }
        }
        // generate script is simple, we do the match, and find out
        ArrayList<String> acts = new ArrayList<>();
        acts.add(prep.length() > 0 ? prep.toString() : null);
        for (TaskData td : datas) {
            if (td.getType() == TaskDataType.ELEMENT) { // we only support link, button, and input for now
                // for input, we need to find the data and input
                Map<String, String> attrs = td.getAttributes();
                String type = attrs != null ? attrs.get("type") : null;
                String data = td.getData();
                String url = td.getUrl();
                if (type != null && type.equalsIgnoreCase("input") && url != null) {
                    sb.append("-type ");
                    sb.append(url);
                    sb.append(",");
                    sb.append(getMatchUserInput(data));
                    sb.append("\n");
                } else if (type != null && type.equalsIgnoreCase("button")) {
                    // split the
                    acts.add("-click \"" + url + "\""); // no wait required
                    acts.add(null);
                } else if (type == null || type.equalsIgnoreCase("link")) {
                    acts.add("-open " + url); // this might also need a click
                    acts.add(url); //
                }
            }
        }
        if (acts.size() < 2) {
            return null;
        }
        String[] ret = new String[acts.size()];
        for (int i = 0; i < acts.size(); i++) {
            if ((i & 1) == 0) {
                ret[i] = sb.toString() + acts.get(i);
            } else {
                ret[i] = acts.get(i);
            }
        }
        return ret;
    }

    private String getMatchUserInput(String label) {
        return label;
    }

    public void addTaskAgent(TaskAgent agent) {
        if (agent != null && agents.indexOf(agent) < 0) agents.add(agent);
    }

    public void runBlocking() throws Exception {
        if (agents.size() < 1) {
            throw new Exception("no avaialbe agents to run!");
        }
        if (resultQueue.size() < 1) {
            throw new Exception("nothing to run!");
        }
        // let's kick off the first run
        while (true) {
            ResultItem todo = null;
            ResultItem tocheck = null;
            for (ResultItem item : resultQueue) {
                if (item.agent == null && todo == null) {
                    todo = item;
                } else if (tocheck == null && item.agent != null && item.result == null && (System.currentTimeMillis() - item.syncTime) > 5000) {
                    tocheck = item;
                }
                if (tocheck != null && todo != null) {
                    break;
                }
            }
            if (tocheck != null) {
                checkResult(tocheck);
            }
            if (todo != null) {
                checkTodo(todo);
            }

            if (todo == null && tocheck == null) {
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
    }

    private void checkTodo(ResultItem item) throws Exception {
        for (TaskAgent agent : agents) {
            if (agent.getCapacity() < 1) {
                continue;
            }
            if (!agent.run(item.task)) {
                continue;
            }
            item.createTime = System.currentTimeMillis();
            item.syncTime = 0;
            item.agent = agent;
            break;
        }
        //if (item.agent == null) throw new Exception("task execution faild, can not dispatch!");
    }

    private void checkResult(ResultItem item) throws Exception {
        TaskResult tr = item.agent.update(item.task.getId());
        if (tr == null) {
            throw new Exception("invalid check result!");
        }
        if (tr.getStatus() != TaskStatus.FAILED && tr.getStatus() != TaskStatus.SUCCESS) {
            return;
        }

        item.result = tr; // the task failure may not cause anything
        if (tr.getStatus() == TaskStatus.FAILED) {
            return;
        }
        TaskData[] datas = item.agent.getData(tr.getTaskId(), null, TaskDataType.ELEMENT | TaskDataType.URL); // it could be click, or navigate
        // get the link
        if (datas == null || datas.length < 1) {
            return;
        }
        String url = null;

        for (TaskData td : datas) {
            if (td.getType() == TaskDataType.URL) {
                url = td.getData();
                break;
            }
        }
        if (url == null || url.length() < 1) {
            return;
        }
        int nodeId = siteGraph.addNode(url, "");
        log("adding link " + tr.getStartNode() + " - " + nodeId);
        siteGraph.addLink(tr.getStartNode(), nodeId, item.task.getScript(), (int) (item.result.getLastUpdate() - item.createTime));
        // process rest stuff
        processActionableDatas(datas, nodeId);

        // call back, but here we hard code for demo purpose
        Object o = TempClient.invokeAction("http://localhost:1392/base.aspx", "updatestate", new TempClient.Payload(siteGraph.packForDemo()), null, null);
    }

    private Map<String, String> getCxtInfo() {
        if (cxtInfo == null) {
            cxtInfo = new HashMap<>();
        }
        cxtInfo.put("rootUrl", rootUrl);
        return cxtInfo;
    }

    private void log(String info) {
        System.out.println(info);
    }

}
