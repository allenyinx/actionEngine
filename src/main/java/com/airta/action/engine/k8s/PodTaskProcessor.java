package com.airta.action.engine.k8s;

import com.airta.action.engine.k8s.process.IDestroy;
import com.airta.action.engine.k8s.process.IExec;
import com.airta.action.engine.k8s.process.IInit;
import com.airta.action.engine.k8s.process.IWait;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1Container;
import io.kubernetes.client.models.V1ObjectMeta;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1PodBuilder;
import io.kubernetes.client.models.V1PodList;
import io.kubernetes.client.models.V1PodSpec;
import io.kubernetes.client.util.Config;

import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.models.V1DeleteOptions;
import io.kubernetes.client.models.V1Service;
import io.kubernetes.client.models.V1ServiceBuilder;
import io.kubernetes.client.models.V1Status;
import io.kubernetes.client.util.Yaml;

import com.google.common.io.ByteStreams;
import io.kubernetes.client.Exec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import java.io.File;
import java.util.HashMap;

import java.util.Arrays;

@Service
public class PodTaskProcessor implements IInit, IDestroy, IExec, IWait {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    public boolean scheduleInitAgent() {

        createPod();
        waitForPodReady();

        return true;
    }

    ;

    @Override
    public boolean createPod() {

        logger.info("## Creating pod with init process ..");

        ApiClient client = null;
        try {
            client = Config.defaultClient();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();

        V1Pod pod =
                new V1PodBuilder()
                        .withNewMetadata()
                        .withName("apod")
                        .endMetadata()
                        .withNewSpec()
                        .addNewContainer()
                        .withName("www")
                        .withImage("nginx")
                        .endContainer()
                        .endSpec()
                        .build();

        try {
            api.createNamespacedPod("default", pod, null, null, null);
        } catch (ApiException e) {
            e.printStackTrace();
        }

        V1Pod pod2 =
                new V1Pod()
                        .metadata(new V1ObjectMeta().name("anotherpod"))
                        .spec(
                                new V1PodSpec()
                                        .containers(Arrays.asList(new V1Container().name("www").image("nginx"))));

        try {
            api.createNamespacedPod("default", pod2, null, null, null);
        } catch (ApiException e) {
            e.printStackTrace();
        }

        V1PodList list =
                null;
        try {
            list = api.listNamespacedPod("default", null, null, null, null, null, null, null, null);
        } catch (ApiException e) {
            e.printStackTrace();
        }
        for (V1Pod item : list.getItems()) {
            System.out.println(item.getMetadata().getName());
        }

        return false;
    }

    @Override
    public boolean createService() {

        V1Pod pod =
                new V1PodBuilder()
                        .withNewMetadata()
                        .withName("apod")
                        .endMetadata()
                        .withNewSpec()
                        .addNewContainer()
                        .withName("www")
                        .withImage("nginx")
                        .withNewResources()
                        .withLimits(new HashMap<>())
                        .endResources()
                        .endContainer()
                        .endSpec()
                        .build();
        System.out.println(Yaml.dump(pod));

        V1Service svc =
                new V1ServiceBuilder()
                        .withNewMetadata()
                        .withName("aservice")
                        .endMetadata()
                        .withNewSpec()
                        .withSessionAffinity("ClientIP")
                        .withType("NodePort")
                        .addNewPort()
                        .withProtocol("TCP")
                        .withName("client")
                        .withPort(8008)
                        .withNodePort(8080)
                        .withTargetPort(new IntOrString(8080))
                        .endPort()
                        .endSpec()
                        .build();
        System.out.println(Yaml.dump(svc));

        // Read yaml configuration file, and deploy it
        ApiClient client = null;
        try {
            client = Config.defaultClient();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Configuration.setDefaultApiClient(client);

        //  See issue #474. Not needed at most cases, but it is needed if you are using war
        //  packging or running this on JUnit.
        Yaml.addModelMap("v1", "Service", V1Service.class);

        // Example yaml file can be found in $REPO_DIR/test-svc.yaml
        File file = new File("test-svc.yaml");
        V1Service yamlSvc = null;
        try {
            yamlSvc = (V1Service) Yaml.load(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Deployment and StatefulSet is defined in apps/v1, so you should use AppsV1Api instead of
        // CoreV1API
        CoreV1Api api = new CoreV1Api();
        V1Service createResult = null;
        try {
            createResult = api.createNamespacedService("default", yamlSvc, null, null, null);
        } catch (ApiException e) {
            e.printStackTrace();
        }

        System.out.println(createResult);

        V1Status deleteResult =
                null;
        try {
            deleteResult = api.deleteNamespacedService(
                    yamlSvc.getMetadata().getName(),
                    "default",
                    null,
                    new V1DeleteOptions(),
                    null,
                    null,
                    null,
                    null);
        } catch (ApiException e) {
            e.printStackTrace();
        }
        System.out.println(deleteResult);
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
                        ? new String[] {"sh"}
                        : commandsList.toArray(new String[commandsList.size()]),
                true,
                tty);

        Thread in =
                new Thread(
                        new Runnable() {
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
