package com.airta.action.engine.k8s.process;

public interface IWait {

    void waitForPodReady();

    void waitForServiceReady();

    void waitForCmdFinish();
}
