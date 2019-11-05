package com.airta.action.engine.k8s.process;

import io.kubernetes.client.ApiException;
import org.apache.commons.cli.ParseException;

import java.io.IOException;

public interface IExec {

    boolean runCMD(String[] commands) throws IOException, ApiException, InterruptedException, ParseException;

    void readStdout();
}
