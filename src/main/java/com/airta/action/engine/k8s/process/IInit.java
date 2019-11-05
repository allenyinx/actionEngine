package com.airta.action.engine.k8s.process;

public interface IInit {

    boolean createPod();

    boolean createService();
}
