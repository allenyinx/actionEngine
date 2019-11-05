package com.airta.action.engine.k8s;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class PodInit {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    public boolean scheduleInitAgent() {
        return true;
    };
}
