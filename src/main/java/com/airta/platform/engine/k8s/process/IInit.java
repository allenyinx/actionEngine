package com.airta.platform.engine.k8s.process;

import io.kubernetes.client.models.V1Service;

public interface IInit {

    boolean createPod();

    V1Service createService();
}
