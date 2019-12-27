package com.airta.platform.engine.k8s;

import com.airta.platform.engine.entity.pool.AgentPool;
import com.airta.platform.engine.entity.pool.PodSession;
import com.airta.platform.engine.entity.pool.PodSessionPool;
import com.airta.platform.engine.k8s.process.IDestroy;
import com.airta.platform.engine.k8s.process.IExec;
import com.airta.platform.engine.k8s.process.IInit;
import com.airta.platform.engine.k8s.process.IWait;
import com.airta.platform.engine.util.RedisClient;
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
import java.util.*;

/**
 * @author allenyin
 */
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

    private final RedisClient redisClient;

    @Autowired
    public PodTaskProcessor(RedisClient redisClient) {

        initClient();
//        redisClient = new RedisClient();
        this.redisClient = redisClient;
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

    public List<V1Service> scheduleInitAgent(AgentPool agentPool) {

        logger.info("## scheduling {} pod for pool: {}", agentPool.getAgentSize(), agentPool.getPoolName());

        List<V1Service> agentServiceList = new ArrayList<>();
        for (int index = 0; index < agentPool.getAgentSize(); index++) {

            String poolName = agentPool.getPoolName();
            int groupId = agentPool.getPoolGroup();
            String agentPodName = AGENTPODPrefix + poolName + "-" + groupId + "-" + index;

            V1Pod agentPod = createPod(agentPodName, agentPool.getPoolName(), agentPool.getPoolGroup(), agentPool.getUrl());
            V1Service agentService = createService(agentPodName, agentPool.getPoolName(), agentPool.getPoolGroup());

            waitForPodReady(agentPod);
            registerAgentSession(agentPod, agentService, agentPool);
            agentServiceList.add(agentService);
        }

        return agentServiceList;
    }

    public boolean scheduleCleanAgents(AgentPool agentPool) {
        logger.info("## cleaning specified agent pool: {} ..", agentPool.getPoolName());

        String poolName = agentPool.getPoolName();
        int groupId = agentPool.getPoolGroup();

        deletePods(poolName, groupId);
        deleteServices(poolName, groupId);

        unRegisterAgentSession(agentPool);

        return true;
    }

    private void registerAgentSession(V1Pod agentPod, V1Service agentService, AgentPool agentPool) {

        logger.info("## register agent {} to pool {} .", agentPod.getMetadata().getName(), agentPool.getPoolName());

        PodSessionPool podSessionPool = redisClient.readObject(agentPool.getPoolName());
        if (podSessionPool == null) {
            podSessionPool = new PodSessionPool();
            podSessionPool.setPoolName(agentPool.getPoolName());
        }
        List<PodSession> podSessionList = podSessionPool.getPodSessionList();
        for (PodSession podSession : podSessionList) {
            if (podSession.getName().equals(agentPod.getMetadata().getName())) {
                logger.warn("## already exist this pod session, skip register ..");
                return;
            }
        }

        PodSession podSession = new PodSession();
        podSession.setGroup(agentPool.getPoolName());
        podSession.setGroup(String.valueOf(agentPool.getPoolGroup()));
        podSession.setName(agentPod.getMetadata().getName());
        podSession.setService(agentService.getMetadata().getName());
        podSession.setPoolName(agentPool.getPoolName());

        List<V1ServicePort> portList = agentService.getSpec().getPorts();
        for (V1ServicePort v1ServicePort : portList) {
            if ("http".equals(v1ServicePort.getName())) {
                podSession.setPort(v1ServicePort.getPort());
                if (v1ServicePort.getNodePort() != null) {
                    podSession.setNodePort(v1ServicePort.getNodePort());
                }
                break;
            }
        }

        podSessionList.add(podSession);
        logger.info("## PodSession {} added into session List pool {}", podSession.getName(), agentPool.getPoolName());
        podSessionPool.setPodSessionList(podSessionList);
        redisClient.storeObject(agentPool.getPoolName(), podSessionPool);
    }

    private void unRegisterAgentSession(AgentPool agentPool) {

        logger.info("## unRegister  pool {} .", agentPool.getPoolName());

        PodSessionPool podSessionPool = redisClient.readObject(agentPool.getPoolName());
        if (podSessionPool == null) {
            podSessionPool = new PodSessionPool();
            podSessionPool.setPoolName(agentPool.getPoolName());
        }
        podSessionPool.setPodSessionList(Collections.emptyList());
        redisClient.storeObject(agentPool.getPoolName(), podSessionPool);
    }

    public PodSessionPool readPodSessionPool(String poolName) {

        return redisClient.readObject(poolName);
    }

    private V1Pod createPod(String agentPodName, String poolName, int groupId, String url) {

        Map<String, String> podLabelMap = new HashMap<>();
        podLabelMap.put("app", agentPodName);
        podLabelMap.put("pool", poolName);
        podLabelMap.put("group", String.valueOf(groupId));

        V1EnvVar v1EnvVar = new V1EnvVar();
        v1EnvVar.setName("AgentEntryURL");
        v1EnvVar.setValue(url);

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
                        .withEnv(v1EnvVar)
                        .endContainer()
                        .endSpec()
                        .build();

        try {
            coreV1Api.createNamespacedPod(NAMESPACE, pod, null, null, null);
        } catch (ApiException e) {
            logger.error(e.getMessage());
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

        logger.info("## clean pods for pool: {}", poolName);
        V1PodList podList = getPodList();

        for (V1Pod item : podList.getItems()) {
            String tmpPodName = item.getMetadata().getName();
            logger.info("## current checking pod: {}", tmpPodName);

            Map<String, String> labelMap = item.getMetadata().getLabels();
            if (labelMap.containsKey("pool") && labelMap.containsKey("group")) {
                String meta_poolName = labelMap.get("pool");
                String meta_groupId = labelMap.get("group");
                if (poolName.equals(meta_poolName) && String.valueOf(groupId).equals(meta_groupId)) {

                    try {
//                        coreV1Api.deleteNamespacedPodAsync(tmpPodName, NAMESPACE,
//                                null, null, null, 0, null, null, null);
                        coreV1Api.deleteNamespacedPod(tmpPodName, NAMESPACE,
                                null, null, null, 0, null, null);
                    } catch (ApiException e) {
                        e.printStackTrace();
                    }

                    logger.info("## pod {} deleted ..", tmpPodName);
                }

            }
        }
    }

    private void deleteServices(String poolName, int groupId) {

        V1ServiceList serviceList = getServiceList();

        for (V1Service item : serviceList.getItems()) {

            String tmpServiceName = item.getMetadata().getName();
            logger.info("## current checking service: {}", tmpServiceName);

            Map<String, String> labelMap = item.getMetadata().getLabels();
            if (labelMap != null && labelMap.containsKey("pool") && labelMap.containsKey("group")) {
                String meta_poolName = labelMap.get("pool");
                String meta_groupId = labelMap.get("group");
                if (poolName.equals(meta_poolName) && String.valueOf(groupId).equals(meta_groupId)) {

                    try {
                        coreV1Api.deleteNamespacedService(tmpServiceName, NAMESPACE,
                                null, null, null, null, null, null);
                    } catch (ApiException e) {
                        e.printStackTrace();
                    }

                    logger.info("## service {} deleted ..", tmpServiceName);
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
    public void waitForPodReady(V1Pod agentPod) {


    }

    @Override
    public void waitForServiceReady() {

    }

    @Override
    public void waitForCmdFinish() {

    }
}
