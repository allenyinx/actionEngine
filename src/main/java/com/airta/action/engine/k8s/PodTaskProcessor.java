package com.airta.action.engine.k8s;

import com.airta.action.engine.entity.pool.AgentPool;
import com.airta.action.engine.k8s.process.IDestroy;
import com.airta.action.engine.k8s.process.IExec;
import com.airta.action.engine.k8s.process.IInit;
import com.airta.action.engine.k8s.process.IWait;
import com.google.common.io.ByteStreams;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.Exec;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.models.*;
import io.kubernetes.client.util.Config;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PodTaskProcessor implements IInit, IDestroy, IExec, IWait {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String NAMESPACE = "airgent";
    private final String AGENTIMAGE = "airta/airgent:latest";
    private final String AGENTPODPrefix = "agent-";
    private final String AGENTContainerName = "agent";
    private final String ShareVolumeName = "sharedata";

    private ApiClient client = null;
    private CoreV1Api coreV1Api = null;

    @Autowired
    public PodTaskProcessor() {

        initClient();
    }

    private void initClient() {

        try {
            client = Config.defaultClient();
            client.setVerifyingSsl(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Configuration.setDefaultApiClient(client);

        coreV1Api = new CoreV1Api();
    }

    public boolean scheduleInitAgent(AgentPool agentPool) {

        logger.info("## scheduling {} pod for pool: {}", agentPool.getAgentSize(), agentPool.getPoolName());

        for (int index = 0; index < agentPool.getAgentSize(); index++) {

            String poolName = agentPool.getPoolName();
            int groupId = agentPool.getPoolGroup();
            String agentPodName = AGENTPODPrefix + poolName + "-" + groupId + "-" + index;

            V1Pod agentPod = createPod(agentPodName, agentPool.getPoolName(), agentPool.getPoolGroup());
            V1Service agentService = createService(agentPodName, agentPool.getPoolName(), agentPool.getPoolGroup());

            waitForPodReady();
            registerAgentSession(agentPod, agentService, agentPool);
        }

        return true;
    }

    public boolean scheduleCleanAgents(AgentPool agentPool) {
        logger.info("## cleaning specified agent pool {} ..", agentPool.getPoolName());

        String poolName = agentPool.getPoolName();
        int groupId = agentPool.getPoolGroup();

        deletePods(poolName, groupId);
        deleteServices(poolName, groupId);

        return true;
    }

    private void registerAgentSession(V1Pod agentPod, V1Service agentService, AgentPool agentPool) {

        logger.info("## register agent {} to pool {} .", agentPod.getMetadata().getName(), agentPool.getPoolName());

    }

    private void unRegisterAgentSession(V1Pod agentPod, V1Service agentService, AgentPool agentPool) {

        logger.info("## unRegister agent {} from pool {} .", agentPod.getMetadata().getName(), agentPool.getPoolName());

    }

    private V1Pod createPod(String agentPodName, String poolName, int groupId) {


        Map<String, String> podLabelMap = new HashMap<>();
        podLabelMap.put("app", agentPodName);
        podLabelMap.put("pool", poolName);
        podLabelMap.put("group", String.valueOf(groupId));

        V1Pod pod =
                new V1PodBuilder()
                        .withNewMetadata()
                        .withName(agentPodName)
                        .withLabels(podLabelMap)
                        .endMetadata()
                        .withNewSpec()
                        .addNewContainer()
                        .withName(AGENTContainerName)
                        .withImage(AGENTIMAGE)
                        .endContainer()
                        .endSpec()
                        .build();

        try {
            coreV1Api.createNamespacedPod(NAMESPACE, pod, null, null, null);
        } catch (ApiException e) {
            e.printStackTrace();
        }

        logger.info("## POD {} created.", pod.getMetadata().getName());
        return pod;
    }

    @Override
    public V1Service createService() {
        return null;
    }

    private V1Service createService(String agentPodName, String poolName, int groupId) {

        Map<String, String> svcSelectorMap = new HashMap<>();
        svcSelectorMap.put("app", agentPodName);

        Map<String, String> serviceLabelMap = new HashMap<>();
        serviceLabelMap.put("pool", poolName);
        serviceLabelMap.put("group", String.valueOf(groupId));

        V1Service svc =
                new V1ServiceBuilder()
                        .withNewMetadata()
                        .withName(agentPodName)
                        .withLabels(serviceLabelMap)
                        .endMetadata()
                        .withNewSpec()
                        .withSessionAffinity("ClientIP")
                        .withType("NodePort")
                        .addNewPort()
                        .withProtocol("TCP")
                        .withName("http")
                        .withPort(8228)
                        .withTargetPort(new IntOrString(8228))
                        .endPort()
                        .addNewPort()
                        .withProtocol("TCP")
                        .withName("vnc")
                        .withPort(5900)
                        .withTargetPort(new IntOrString(5900))
                        .endPort()
                        .addToSelector(svcSelectorMap)
                        .endSpec()
                        .build();

        try {
            coreV1Api.createNamespacedService(NAMESPACE, svc, null, null, null);
        } catch (ApiException e) {
            e.printStackTrace();
        }

        logger.info("## service for {} created ..", agentPodName);

        return svc;
    }

    private void deletePods(String poolName, int groupId) {

        V1PodList podList = getPodList();

        for (V1Pod item : podList.getItems()) {
            logger.info(item.getMetadata().getName());

            Map<String, String> labelMap = item.getMetadata().getLabels();
            if (labelMap.containsKey("pool") && labelMap.containsKey("group")) {
                String meta_poolName = labelMap.get("pool");
                String meta_groupId = labelMap.get("group");
                if (poolName.equals(meta_poolName) && String.valueOf(groupId).equals(meta_groupId)) {

                    try {
                        coreV1Api.deleteNamespacedPod(item.getMetadata().getName(), NAMESPACE,
                                null, null, null, null, null, null);
                    } catch (ApiException e) {
                        e.printStackTrace();
                    }

                    logger.info("## pod {} deleted ..", item.getMetadata().getName());
                }

            }
        }
    }

    private void deleteServices(String poolName, int groupId) {

        V1ServiceList serviceList = getServiceList();

        for (V1Service item : serviceList.getItems()) {

            Map<String, String> labelMap = item.getMetadata().getLabels();
            if (labelMap.containsKey("pool") && labelMap.containsKey("group")) {
                String meta_poolName = labelMap.get("pool");
                String meta_groupId = labelMap.get("group");
                if (poolName.equals(meta_poolName) && String.valueOf(groupId).equals(meta_groupId)) {

                    try {
                        coreV1Api.deleteNamespacedService(item.getMetadata().getName(), NAMESPACE,
                                null, null, null, null, null, null);
                    } catch (ApiException e) {
                        e.printStackTrace();
                    }

                    logger.info("## service {} deleted ..", item.getMetadata().getName());
                }

            }
        }

    }

    private V1PodList getPodList() {

        V1PodList list = null;
        try {
            list = coreV1Api.listNamespacedPod(NAMESPACE, null, null, null, null, null, null, null, null);
        } catch (ApiException e) {
            e.printStackTrace();
        }

        return list;
    }

    private V1ServiceList getServiceList() {

        V1ServiceList list = null;
        try {
            list = coreV1Api.listNamespacedService(NAMESPACE, null, null, null, null, null, null, null, null);
        } catch (ApiException e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public boolean createPod() {

        return false;
    }

    @Override
    public boolean deletePod() {
        return false;
    }

    @Override
    public boolean runCMD(String[] commands) throws IOException, ApiException, InterruptedException, ParseException {

        final Options options = new Options();
        options.addOption(new Option("p", "pod", true, "The name of the pod"));
        options.addOption(new Option("n", "namespace", true, "The namespace of the pod"));

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, commands);

        String podName = cmd.getOptionValue("p", "nginx-dbddb74b8-s4cx5");
        String namespace = cmd.getOptionValue("n", "default");
        List<String> commandsList = new ArrayList<>();

        commands = cmd.getArgs();
        for (int i = 0; i < commands.length; i++) {
            commandsList.add(commands[i]);
        }

        ApiClient client = null;
        try {
            client = Config.defaultClient();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Configuration.setDefaultApiClient(client);

        Exec exec = new Exec();
        boolean tty = System.console() != null;
        // final Process proc = exec.exec("default", "nginx-4217019353-k5sn9", new String[]
        //   {"sh", "-c", "echo foo"}, true, tty);
        final Process proc = exec.exec(
                namespace,
                podName,
                commandsList.isEmpty()
                        ? new String[]{"sh"}
                        : commandsList.toArray(new String[commandsList.size()]),
                true,
                tty);

        Thread in =
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    ByteStreams.copy(System.in, proc.getOutputStream());
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        });
        in.start();

        Thread out =
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    ByteStreams.copy(proc.getInputStream(), System.out);
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        });
        out.start();

        proc.waitFor();

        // wait for any last output; no need to wait for input thread
        out.join();

        proc.destroy();

        System.exit(proc.exitValue());

        return false;
    }

    @Override
    public void readStdout() {

    }

    @Override
    public void waitForPodReady() {

    }

    @Override
    public void waitForServiceReady() {

    }

    @Override
    public void waitForCmdFinish() {

    }
}
