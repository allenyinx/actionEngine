package com.airta.platform.engine.model;

import com.airta.platform.engine.entity.action.ActionResult;

import java.io.IOException;

public interface Operatable {

    void proceed() throws IOException;

    ActionResult result();
}
