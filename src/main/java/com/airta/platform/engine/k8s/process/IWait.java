package com.airta.platform.engine.k8s.process;

import io.kubernetes.client.models.V1Pod;

public interface IWait {

    void waitForPodReady(V1Pod agentPod);

    void waitForServiceReady();

    void waitForCmdFinish();
}
